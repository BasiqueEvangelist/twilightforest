package twilightforest.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.ASMHooks;

@Mixin(LeashFenceKnotEntity.class)
public class LeashFenceKnotEntityMixin {
    @ModifyReturnValue(method = "survives", at = @At("RETURN"))
    private boolean lead(boolean original) {
        return ASMHooks.lead(original, (LeashFenceKnotEntity)(Object) this);
    }
}
