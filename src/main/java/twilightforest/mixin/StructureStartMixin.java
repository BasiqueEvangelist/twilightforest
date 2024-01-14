package twilightforest.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.ASMHooks;

@Mixin(StructureStart.class)
public class StructureStartMixin {
    @ModifyExpressionValue(method = "loadStaticStart", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/levelgen/structure/Structure;Lnet/minecraft/world/level/ChunkPos;ILnet/minecraft/world/level/levelgen/structure/pieces/PiecesContainer;)Lnet/minecraft/world/level/levelgen/structure/StructureStart;"))
    private static StructureStart conquered(StructureStart original, StructurePieceSerializationContext pContext, CompoundTag pTag, long pSeed, @Local PiecesContainer piecesContainer) {
        return ASMHooks.conquered(original, piecesContainer, pTag);
    }
}
