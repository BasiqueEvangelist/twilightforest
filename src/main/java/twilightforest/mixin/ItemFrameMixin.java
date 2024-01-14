package twilightforest.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.ASMHooks;

@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin {
    @Shadow public abstract ItemStack getItem();

    @ModifyExpressionValue(method = "getFramedMapId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean frame(boolean original) {
        return ASMHooks.shouldMapRender(original, getItem());
    }
}
