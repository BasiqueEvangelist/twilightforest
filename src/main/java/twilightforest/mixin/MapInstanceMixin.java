package twilightforest.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import twilightforest.ASMHooks;

@Mixin(targets = "net/minecraft/client/gui/MapRenderer$MapInstance")
public class MapInstanceMixin {
    @Shadow private MapItemSavedData data;

    @ModifyVariable(method = "draw", at = @At("STORE"), ordinal = 1)
    private int decorations(int original, PoseStack pPoseStack, MultiBufferSource pBufferSource, boolean pActive, int pPackedLight) {
        return ASMHooks.mapRenderDecorations(original, data, pPoseStack, pBufferSource, pPackedLight);
    }
}
