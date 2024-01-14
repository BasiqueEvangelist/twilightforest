package twilightforest.mixin;

import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.ASMHooks;

import java.util.List;

@Mixin(ChunkStatus.class)
public class ChunkStatusMixin {
    @Inject(method = "getStatusList", at = @At("HEAD"))
    private static void assertBlanketing(CallbackInfoReturnable<List<ChunkStatus>> cir) {
        ASMHooks.assertChunkBlanketing();
    }
}
