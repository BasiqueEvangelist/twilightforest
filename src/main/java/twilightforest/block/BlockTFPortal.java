package twilightforest.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twilightforest.TFConfig;
import twilightforest.TFTeleporter;
import twilightforest.TwilightForestMod;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BlockTFPortal extends BlockBreakable {
	private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.8125F, 1.0F);

	public BlockTFPortal() {
		super(Material.PORTAL, false);
		this.setHardness(-1F);
		this.setSoundType(SoundType.GLASS);
		this.setLightLevel(0.75F);
	}

	@Override
	@Deprecated
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return AABB;
	}

	@Override
	@Deprecated
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	public boolean tryToCreatePortal(World world, BlockPos pos, EntityItem activationItem) {
		IBlockState state = world.getBlockState(pos);

		if (state.getBlock() == Blocks.WATER) {
			HashMap<BlockPos, Boolean> blocksChecked = new HashMap<>();
			blocksChecked.put(pos, true);

			PassableNumber number = new PassableNumber(64);

			if (recursivelyValidatePortal(world, pos, blocksChecked, number) && number.getNumber() > 3) {
				activationItem.getItem().shrink(1);
				world.addWeatherEffect(new EntityLightningBolt(world, pos.getX(), pos.getY(), pos.getZ(), false));

				for (Map.Entry<BlockPos, Boolean> checkedPos : blocksChecked.entrySet())
					if (checkedPos.getValue()) world.setBlockState(checkedPos.getKey(), TFBlocks.portal.getDefaultState(), 2);

				return true;
			}
		}

		return false;
	}

	private static boolean recursivelyValidatePortal(World world, BlockPos pos, HashMap<BlockPos, Boolean> blocksChecked, PassableNumber waterLimit) {
		boolean isPoolProbablyEnclosed = true;

		waterLimit.decrement();
		if (waterLimit.getNumber() < 0) return false;

		for (int i = 0; i < EnumFacing.HORIZONTALS.length && waterLimit.getNumber() >= 0; i++) {
			BlockPos positionCheck = pos.offset(EnumFacing.HORIZONTALS[i]);

			if (!blocksChecked.containsKey(positionCheck)) {
				IBlockState state = world.getBlockState(positionCheck);

				if (state == Blocks.WATER.getDefaultState() && world.getBlockState(positionCheck.down()).isFullCube()) {
					blocksChecked.put(positionCheck, true);

					isPoolProbablyEnclosed = isPoolProbablyEnclosed && recursivelyValidatePortal(world, positionCheck, blocksChecked, waterLimit);
				} else if (isGrassOrDirt(state) && isNatureBlock(world.getBlockState(positionCheck.up()))) {
					blocksChecked.put(positionCheck, false);
				} else return false;
			}
		}

		return isPoolProbablyEnclosed;
	}

	private static class PassableNumber {
		private int number;

		PassableNumber(int number) {
			this.number = number;
		}

		int getNumber() {
			return number;
		}

		void decrement() {
			this.number = this.number - 1;
		}

		void setNumber(int number) {
			this.number = number;
		}
	}

	private static boolean isNatureBlock(IBlockState state) {
		Material mat = state.getMaterial();
		return state.isFullCube() && (mat == Material.PLANTS || mat == Material.VINE || mat == Material.LEAVES);
	}

	private static boolean isGrassOrDirt(IBlockState state) {
		Material mat = state.getMaterial();
		return state.isFullCube() && (mat == Material.GRASS || mat == Material.GROUND);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block notUsed, BlockPos fromPos) {
		boolean good = world.getBlockState(pos.down()).isFullCube();

		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			if (!good) break;

			IBlockState neighboringState = world.getBlockState(pos.offset(facing));

			good = isGrassOrDirt(neighboringState) || neighboringState.getBlock() == TFBlocks.portal;
		}

		if (!good) {
			world.playEvent(2001, pos, Block.getStateId(state));
			world.setBlockState(pos, Blocks.WATER.getDefaultState(), 0b11);
		}
	}

	@Override
	public int quantityDropped(Random random) {
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		if (!entity.isRiding() && entity.getPassengers().isEmpty() && entity.timeUntilPortal <= 0) {
			if (entity instanceof EntityPlayerMP) {
				EntityPlayerMP playerMP = (EntityPlayerMP) entity;

				if (playerMP.timeUntilPortal > 0) {
					// do not switch dimensions if the player has any time on this thinger
					playerMP.timeUntilPortal = 10;
				} else {

					// send to twilight
					if (playerMP.dimension != TFConfig.dimension.dimensionID) {
						if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(playerMP, TFConfig.dimension.dimensionID)) return;

						//PlayerHelper.grantAdvancement(playerMP, new ResourceLocation(TwilightForestMod.ID, "twilight_portal"));
						TwilightForestMod.LOGGER.debug("Player touched the portal block.  Sending the player to dimension {}", TFConfig.dimension.dimensionID);

						playerMP.mcServer.getPlayerList().transferPlayerToDimension(playerMP, TFConfig.dimension.dimensionID, TFTeleporter.getTeleporterForDim(playerMP.mcServer, TFConfig.dimension.dimensionID));

						// set respawn point for TF dimension to near the arrival portal
						playerMP.setSpawnChunk(new BlockPos(playerMP), true, TFConfig.dimension.dimensionID);
					} else {
						if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(playerMP, 0)) return;

						playerMP.mcServer.getPlayerList().transferPlayerToDimension(playerMP, 0, TFTeleporter.getTeleporterForDim(playerMP.mcServer, 0));
					}
				}
			} else {
				if (entity.dimension != TFConfig.dimension.dimensionID) {
					changeDimension(entity, TFConfig.dimension.dimensionID);
				} else {
					changeDimension(entity, 0);
				}
			}
		}
	}

	/**
	 * [VanillaCopy] Entity.changeDimension. Relevant edits noted.
	 * `this` -> `toTeleport`
	 * return value Entity -> void
	 */
	@SuppressWarnings("unused")
	private void changeDimension(Entity toTeleport, int dimensionIn) {
		if (!toTeleport.world.isRemote && !toTeleport.isDead) {
			if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(toTeleport, dimensionIn)) return;
			toTeleport.world.profiler.startSection("changeDimension");
			MinecraftServer minecraftserver = toTeleport.getServer();
			int i = toTeleport.dimension;
			WorldServer worldserver = minecraftserver.getWorld(i);
			WorldServer worldserver1 = minecraftserver.getWorld(dimensionIn);
			toTeleport.dimension = dimensionIn;

			if (i == 1 && dimensionIn == 1) {
				worldserver1 = minecraftserver.getWorld(0);
				toTeleport.dimension = 0;
			}

			toTeleport.world.removeEntity(toTeleport);
			toTeleport.isDead = false;
			toTeleport.world.profiler.startSection("reposition");
			BlockPos blockpos;

			if (dimensionIn == 1) {
				blockpos = worldserver1.getSpawnCoordinate();
			} else {
				double d0 = toTeleport.posX;
				double d1 = toTeleport.posZ;
				double d2 = 8.0D;

				// Tf - remove 8x scaling for nether
				d0 = MathHelper.clamp(d0, worldserver1.getWorldBorder().minX() + 16.0D, worldserver1.getWorldBorder().maxX() - 16.0D);
				d1 = MathHelper.clamp(d1, worldserver1.getWorldBorder().minZ() + 16.0D, worldserver1.getWorldBorder().maxZ() - 16.0D);

				d0 = (double) MathHelper.clamp((int) d0, -29999872, 29999872);
				d1 = (double) MathHelper.clamp((int) d1, -29999872, 29999872);
				float f = toTeleport.rotationYaw;
				toTeleport.setLocationAndAngles(d0, toTeleport.posY, d1, 90.0F, 0.0F);
				Teleporter teleporter = TFTeleporter.getTeleporterForDim(minecraftserver, dimensionIn); // TF - custom teleporter
				teleporter.placeInExistingPortal(toTeleport, f);
				blockpos = new BlockPos(toTeleport);
			}

			worldserver.updateEntityWithOptionalForce(toTeleport, false);
			toTeleport.world.profiler.endStartSection("reloading");
			Entity entity = EntityList.newEntity(toTeleport.getClass(), worldserver1);

			if (entity != null) {
				entity.copyDataFromOld(toTeleport);

				if (i == 1 && dimensionIn == 1) {
					BlockPos blockpos1 = worldserver1.getTopSolidOrLiquidBlock(worldserver1.getSpawnPoint());
					entity.moveToBlockPosAndAngles(blockpos1, entity.rotationYaw, entity.rotationPitch);
				} else {
					// TF - inline moveToBlockPosAndAngles without +0.5 offsets, since teleporter already took care of it
					entity.setLocationAndAngles((double) blockpos.getX(), (double) blockpos.getY(), (double) blockpos.getZ(), entity.rotationYaw, entity.rotationPitch);
				}

				boolean flag = entity.forceSpawn;
				entity.forceSpawn = true;
				worldserver1.spawnEntity(entity);
				entity.forceSpawn = flag;
				worldserver1.updateEntityWithOptionalForce(entity, false);
			}

			toTeleport.isDead = true;
			toTeleport.world.profiler.endSection();
			worldserver.resetUpdateEntityTick();
			worldserver1.resetUpdateEntityTick();
			toTeleport.world.profiler.endSection();
		}
	}

	// Full [VanillaCopy] of BlockPortal.randomDisplayTick
	// TODO Eeeh... Let's look at changing this too alongside a new model.
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (rand.nextInt(100) == 0) {
			worldIn.playSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.5F, rand.nextFloat() * 0.4F + 0.8F, false);
		}

		for (int i = 0; i < 4; ++i) {
			double xPos = (double) ((float) pos.getX() + rand.nextFloat());
			double yPos = pos.getY()+1D;
			double zPos = (double) ((float) pos.getZ() + rand.nextFloat());
			double xSpeed = ((double) rand.nextFloat() - 0.5D) * 0.5D;
			double ySpeed = rand.nextFloat();
			double zSpeed = ((double) rand.nextFloat() - 0.5D) * 0.5D;
			//int j = rand.nextInt(2) * 2 - 1;

			//if (worldIn.getBlockState(pos.west()).getBlock() != this && worldIn.getBlockState(pos.east()).getBlock() != this) {
			//	xPos = (double) pos.getX() + 0.5D + 0.25D * (double) j;
			//	xSpeed = (double) (rand.nextFloat() * 2.0F * (float) j);
			//} else {
			//	zPos = (double) pos.getZ() + 0.5D + 0.25D * (double) j;
			//	zSpeed = (double) (rand.nextFloat() * 2.0F * (float) j);
			//}

			worldIn.spawnParticle(EnumParticleTypes.PORTAL, xPos, yPos, zPos, xSpeed, ySpeed, zSpeed);
		}
	}
}
