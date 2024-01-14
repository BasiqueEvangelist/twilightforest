package twilightforest.mixin;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.ASMHooks;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {
    @Shadow @Final private Entity entity;

    @Inject(method = "sendDirtyEntityData", at = @At("HEAD"))
    private void sync(CallbackInfo ci) {
        ASMHooks.updateMultiparts(this.entity);
    }
}
