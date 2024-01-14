package twilightforest.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import twilightforest.ASMHooks;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(MapItem.class)
public class MapItemMixin {
    @ModifyVariable(method = "appendHoverText", at = @At("STORE"))
    private MapItemSavedData iteminfo(MapItemSavedData value, ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        return ASMHooks.renderMapData(value, pStack, pLevel);
    }
}
