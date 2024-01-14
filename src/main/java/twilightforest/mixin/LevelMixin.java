package twilightforest.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.ASMHooks;

@Mixin(Level.class)
public class LevelMixin {
    @ModifyReturnValue(method = "isRainingAt", at = @At("RETURN"))
    private boolean cloud(boolean original, BlockPos pos) {
        return ASMHooks.cloud(original, (Level)(Object) this, pos);
    }
}
