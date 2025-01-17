package twilightforest.compat.rei;

import dev.architectury.event.CompoundEventResult;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.entry.renderer.EntryRendererRegistry;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import org.jetbrains.annotations.Nullable;
import twilightforest.TFConfig;
import twilightforest.TwilightForestMod;
import twilightforest.client.UncraftingScreen;
import twilightforest.compat.rei.categories.REICrumbleHornCategory;
import twilightforest.compat.rei.categories.REITransformationPowderCategory;
import twilightforest.compat.rei.categories.REIUncraftingCategory;
import twilightforest.compat.rei.displays.REICrumbleHornDisplay;
import twilightforest.compat.rei.displays.REITransformationPowderDisplay;
import twilightforest.compat.rei.displays.REIUncraftingDisplay;
import twilightforest.compat.rei.entries.EntityEntryDefinition;
import twilightforest.data.tags.ItemTagGenerator;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFItems;
import twilightforest.init.TFRecipes;
import twilightforest.item.recipe.CrumbleRecipe;
import twilightforest.item.recipe.TransformPowderRecipe;
import twilightforest.item.recipe.UncraftingRecipe;
import twilightforest.util.EntityRenderingUtil;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.stream.Stream;

@REIPluginClient
public class TFREIClientPlugin implements REIClientPlugin {

	public static final EntityEntryDefinition ENTITY_DEFINITION = new EntityEntryDefinition();
	public Map<EntryStack<Entity>, EntryRenderer<Entity>> RENDER_CACHE = new WeakHashMap<>();

	@Override
	public void registerCategories(CategoryRegistry registry) {
		if (!TFConfig.COMMON_CONFIG.UNCRAFTING_STUFFS.disableEntireTable.get()) {
			registry.addWorkstations(BuiltinPlugin.CRAFTING, EntryStacks.of(TFBlocks.UNCRAFTING_TABLE));
			registry.addWorkstations(TFREIServerPlugin.UNCRAFTING, EntryStacks.of(TFBlocks.UNCRAFTING_TABLE));
		}
		registry.addWorkstations(REICrumbleHornCategory.CRUMBLE_HORN, EntryStacks.of(TFItems.CRUMBLE_HORN));
		registry.addWorkstations(REITransformationPowderCategory.TRANSFORMATION, EntryStacks.of(TFItems.TRANSFORMATION_POWDER));

		if (!TFConfig.COMMON_CONFIG.UNCRAFTING_STUFFS.disableEntireTable.get()) {
			registry.add(new REIUncraftingCategory());
		}
		registry.add(new REICrumbleHornCategory());
		registry.add(new REITransformationPowderCategory());
	}

	@Override
	public void registerDisplays(DisplayRegistry registry) {
		RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();

		if (!TFConfig.COMMON_CONFIG.UNCRAFTING_STUFFS.disableEntireTable.get()) {
			registry.registerRecipeFiller(UncraftingRecipe.class, TFRecipes.UNCRAFTING_RECIPE.get(), REIUncraftingDisplay::ofUncrafting);
		}
		if (!TFConfig.COMMON_CONFIG.UNCRAFTING_STUFFS.disableUncraftingOnly.get()) {
			registry.registerRecipeFiller(CraftingRecipe.class, RecipeType.CRAFTING, recipe -> {
				if (recipe.value().getResultItem(registryAccess).isEmpty() ||
						recipe.value().getResultItem(registryAccess).is(ItemTagGenerator.BANNED_UNCRAFTABLES) ||
						TFConfig.COMMON_CONFIG.UNCRAFTING_STUFFS.disableUncraftingRecipes.get().contains(recipe.id().toString()) ||
						TFConfig.COMMON_CONFIG.UNCRAFTING_STUFFS.flipUncraftingModIdList.get() != TFConfig.COMMON_CONFIG.UNCRAFTING_STUFFS.blacklistedUncraftingModIds.get().contains(recipe.id().getNamespace())) {
					return null;
				}
				if (recipe.value() instanceof ShapelessRecipe && !TFConfig.COMMON_CONFIG.UNCRAFTING_STUFFS.allowShapelessUncrafting.get()) {
					return null;
				}

				return REIUncraftingDisplay.of(recipe);
			});
		}

		registry.registerRecipeFiller(CrumbleRecipe.class, TFRecipes.CRUMBLE_RECIPE.get(), REICrumbleHornDisplay::of);
		registry.registerRecipeFiller(TransformPowderRecipe.class, TFRecipes.TRANSFORM_POWDER_RECIPE.get(), REITransformationPowderDisplay::of);
	}

	@Override
	public void registerScreens(ScreenRegistry registry) {
		if (!TFConfig.COMMON_CONFIG.UNCRAFTING_STUFFS.disableEntireTable.get()) {
			registry.registerClickArea(screen -> new Rectangle(34, 33, 27, 20), UncraftingScreen.class, TFREIServerPlugin.UNCRAFTING);
			registry.registerClickArea(screen -> new Rectangle(115, 33, 27, 20), UncraftingScreen.class, BuiltinPlugin.CRAFTING);
		}
	}

	@Override
	@SuppressWarnings("all") //I dont care if this is experimental
	public void registerEntryRenderers(EntryRendererRegistry registry) {
		RENDER_CACHE.clear();

		registry.register(EntityEntryDefinition.ENTITY_TYPE, (entry, last) -> {
			if (entry.getValue() instanceof ItemEntity) {
				return RENDER_CACHE.computeIfAbsent(entry, stack -> new EntityEntryDefinition.ItemEntityRenderer());
			}

			return last;
		});
	}

	@Override
	public void registerEntryTypes(EntryTypeRegistry registry) {
		registry.register(EntityEntryDefinition.ENTITY_TYPE, ENTITY_DEFINITION);

		registry.registerBridge(VanillaEntryTypes.ITEM, EntityEntryDefinition.ENTITY_TYPE, object -> {
			Optional<Stream<EntryStack<Entity>>> stream;

			ItemStack stack = object.getValue();

			try {
				Entity entity;

				if (stack.getItem() instanceof DeferredSpawnEggItem spawnEggItem) {
					EntityType<?> type = spawnEggItem.getType(stack.getTag());

					entity = EntityRenderingUtil.fetchEntity(type, Minecraft.getInstance().level);
				} else {
					entity = createItemEntity(stack);
				}

				stream = Optional.of(Stream.of(EntryStack.of(ENTITY_DEFINITION, entity)));
			} catch (Exception e) {
				stream = Optional.empty();
				TwilightForestMod.LOGGER.error("Caught an error assigning an entity to a stack!", e);
			}

			return stream.map(CompoundEventResult::interruptTrue).orElseGet(CompoundEventResult::pass);
		});

		registry.registerBridge(EntityEntryDefinition.ENTITY_TYPE, VanillaEntryTypes.ITEM, object -> {
			Optional<Stream<EntryStack<ItemStack>>> stream = Optional.empty();

			Entity entity = EntityRenderingUtil.fetchEntity(object.getValue().getType(), Minecraft.getInstance().level);

			ItemStack stack = null;

			if (entity instanceof ItemEntity itemEntity) {
				stack = itemEntity.getItem();
			} else if (entity != null) {
				Item spawnEggItem = DeferredSpawnEggItem.byId(entity.getType());

				if (spawnEggItem != null) stack = spawnEggItem.getDefaultInstance();
			}

			if (stack != null) {
				stream = Optional.of(Stream.of(EntryStacks.of(stack)));
			}

			return stream.map(CompoundEventResult::interruptTrue).orElseGet(CompoundEventResult::pass);
		});
	}

	@Nullable
	public static ItemEntity createItemEntity(ItemLike item) {
		return createItemEntity(item.asItem().getDefaultInstance());
	}

	@Nullable
	public static ItemEntity createItemEntity(ItemStack stack) {
		//unfortunately entity creation is required here.
		//If I pull from my cache the items all render as the same block/item, depending on what was last rendered on screen
		ItemEntity entity = EntityType.ITEM.create(Minecraft.getInstance().level);

		if (entity != null) {
			entity.setItem(stack);
			return entity;
		}
		return null;
	}
}
