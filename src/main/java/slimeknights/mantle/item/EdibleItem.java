package slimeknights.mantle.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import slimeknights.mantle.util.TranslationHelper;
import slimeknights.mantle.registration.deferred.ItemDeferredRegister;

import java.util.function.Consumer;

public class EdibleItem extends Item {
  public EdibleItem(FoodProperties foodIn) {
    this(ItemDeferredRegister.setIdFromCurrentKey(new Properties()).food(foodIn));
  }

  public EdibleItem(Item.Properties properties) {
    super(properties);
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
    TranslationHelper.addOptionalTooltip(stack, tooltip);
    super.appendHoverText(stack, context, display, tooltip, flag);
  }
}