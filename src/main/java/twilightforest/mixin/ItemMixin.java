package twilightforest.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.ASMHooks;

@Mixin(Item.class)
public class ItemMixin {
    @ModifyReturnValue(method = "getPlayerPOVHitResult", at = @At("RETURN"))
    private static BlockHitResult reach(BlockHitResult original, Level pLevel, Player pPlayer, ClipContext.Fluid pFluidMode) {
        return ASMHooks.reach(original, pLevel, pPlayer, pFluidMode);
    }
}
