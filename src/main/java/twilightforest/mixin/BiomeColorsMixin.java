package twilightforest.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.ASMHooks;

@Mixin(BiomeColors.class)
public class BiomeColorsMixin {
    @ModifyReturnValue(method = "lambda$static$0", at = @At("RETURN"))
    private static int foliage(int original, Biome pBiome, double pX, double pZ) {
        return ASMHooks.foliage(original, pBiome, pX, pZ);
    }
}
