package twilightforest.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.ASMHooks;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @ModifyExpressionValue(method = "onResourceManagerReload", at = @At(value = "NEW", target = "(Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;Lnet/minecraft/client/renderer/entity/ItemRenderer;Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;Lnet/minecraft/client/renderer/ItemInHandRenderer;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/model/geom/EntityModelSet;Lnet/minecraft/client/gui/Font;)Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;"))
    private EntityRendererProvider.Context bake(EntityRendererProvider.Context original) {
        return ASMHooks.bakeMultipartRenders(original);
    }

    @ModifyReturnValue(method = "getRenderer", at = @At(value = "RETURN", ordinal = 1))
    private EntityRenderer<?> renderer(EntityRenderer<?> original, Entity entity) {
        return ASMHooks.getMultipartRenderer(original, entity);
    }
}
