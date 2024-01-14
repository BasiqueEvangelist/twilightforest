package twilightforest.mixin;

import net.minecraft.world.level.levelgen.WorldOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.ASMHooks;

import java.util.Optional;

@Mixin(WorldOptions.class)
public class WorldOptionsMixin {
    @Shadow @Final private long seed;

    @Inject(method = "<init>(JZZLjava/util/Optional;)V", at = @At("TAIL"))
    private void seed(long p_249191_, boolean p_250927_, boolean p_249013_, Optional<String> p_250735_, CallbackInfo ci) {
        ASMHooks.seed(this.seed);
    }
}
