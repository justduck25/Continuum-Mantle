package slimeknights.mantle.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import slimeknights.mantle.util.TranslationHelper;

import java.util.function.Consumer;

public class ArmorTooltipItem extends TooltipItem {
  public ArmorTooltipItem(Object armorMaterial, Object type, Properties builder) {
    super(builder);
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
    TranslationHelper.addOptionalTooltip(stack, tooltip);
    super.appendHoverText(stack, context, display, tooltip, flag);
  }
}