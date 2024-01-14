package twilightforest.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.ASMHooks;

@Mixin(WrittenBookItem.class)
public class WrittenBookItemMixin {
    @ModifyReturnValue(method = "getName", at = @At(value = "RETURN", ordinal = 0))
    private Component book(Component original, ItemStack pStack) {
        return ASMHooks.book(original, pStack.getTag());
    }
}
