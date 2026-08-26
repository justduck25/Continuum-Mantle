package slimeknights.mantle.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.client.book.BookScreenOpener;

import java.util.function.Consumer;

public abstract class AbstractBookItem extends LecternBookItem {
  private static final Component CLICK_TO_OPEN = Mantle.makeComponent("item", "book.click_to_open").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC);

  public AbstractBookItem(Properties properties) {
    super(properties);
  }

  public abstract BookScreenOpener getBook(ItemStack stack);

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
    Player player = SafeClientAccess.getPlayer();
    if (player != null) {
      tooltip.accept(CLICK_TO_OPEN);
    }
    super.appendHoverText(stack, context, display, tooltip, flag);
  }

  public void openScreen(Player player, InteractionHand hand, ItemStack stack) {
    getBook(stack).openGui(hand, stack);
  }

  @Override
  public InteractionResult use(Level world, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (world.isClientSide()) {
      openScreen(player, hand, stack);
    }
    return InteractionResult.SUCCESS;
  }

  public void openScreen(Player player, int slotIndex, ItemStack stack) {
    getBook(stack).openGui(slotIndex, stack);
  }

  @Override
  public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack held, Slot slot, ClickAction action, Player player, SlotAccess access) {
    if (action == ClickAction.SECONDARY && held.isEmpty() && slot.allowModification(player)) {
      if (player.level().isClientSide()) {
        openScreen(player, slot.index, stack);
      }
      return true;
    }
    return false;
  }

  @Override
  public void openLecternScreenClient(BlockPos pos, ItemStack book) {
    getBook(book).openGui(pos, book);
  }
}