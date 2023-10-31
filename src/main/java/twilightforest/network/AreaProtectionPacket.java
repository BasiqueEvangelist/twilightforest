package twilightforest.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.network.NetworkEvent;
import twilightforest.entity.ProtectionBox;
import twilightforest.init.TFParticleType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AreaProtectionPacket {

	private final List<BoundingBox> sbb;
	private final BlockPos pos;

	public AreaProtectionPacket(List<BoundingBox> sbb, BlockPos pos) {
		this.sbb = sbb;
		this.pos = pos;
	}

	public AreaProtectionPacket(FriendlyByteBuf buf) {
		this.sbb = new ArrayList<>();
		int len = buf.readInt();
		for (int i = 0; i < len; i++) {
			this.sbb.add(new BoundingBox(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt()));
		}
		this.pos = buf.readBlockPos();
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(this.sbb.size());
		this.sbb.forEach(box -> {
			buf.writeInt(box.minX());
			buf.writeInt(box.minY());
			buf.writeInt(box.minZ());
			buf.writeInt(box.maxX());
			buf.writeInt(box.maxY());
			buf.writeInt(box.maxZ());
		});
		buf.writeBlockPos(this.pos);
	}

	public static class Handler {

		public static boolean onMessage(AreaProtectionPacket message, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(new Runnable() {
				@Override
				public void run() {
					ClientLevel level = Minecraft.getInstance().level;
					message.sbb.forEach(box -> addProtectionBox(level, box));

					for (int i = 0; i < 20; i++) {

						double vx = level.getRandom().nextGaussian() * 0.02D;
						double vy = level.getRandom().nextGaussian() * 0.02D;
						double vz = level.getRandom().nextGaussian() * 0.02D;

						double x = message.pos.getX() + 0.5D + level.getRandom().nextFloat() - level.getRandom().nextFloat();
						double y = message.pos.getY() + 0.5D + level.getRandom().nextFloat() - level.getRandom().nextFloat();
						double z = message.pos.getZ() + 0.5D + level.getRandom().nextFloat() - level.getRandom().nextFloat();

						level.addParticle(TFParticleType.PROTECTION.get(), x, y, z, vx, vy, vz);
					}
				}

				static void addProtectionBox(ClientLevel level, BoundingBox sbb) {

					for (Entity entity : level.entitiesForRendering()) {
						if (entity instanceof ProtectionBox box) {
							if (box.lifeTime > 0 && box.matches(sbb)) {
								box.resetLifetime();
								return;
							}
						}
					}

					level.addEntity(new ProtectionBox(level, sbb));
				}
			});
			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
