package twilightforest.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import twilightforest.ASMHooks;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    @ModifyExpressionValue(method = "renderArmWithItem", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/Items;FILLED_MAP:Lnet/minecraft/world/item/Item;", shift = At.Shift.AFTER), require = 0)
    private boolean render(boolean original, @Local ItemStack pStack) {
        return ASMHooks.shouldMapRender(original, pStack);
    }

    @ModifyVariable(method = "renderMap", at = @At("STORE"))
    private MapItemSavedData renderdata(MapItemSavedData data, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, ItemStack pStack) {
        return ASMHooks.renderMapData(data, pStack, minecraft.level);
    }
}
