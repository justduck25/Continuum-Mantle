package slimeknights.mantle.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import slimeknights.mantle.util.BlockEntityHelper;

public abstract class LecternBookItem extends TooltipItem implements ILecternBookItem {
  public LecternBookItem(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Level level = context.getLevel();
    BlockPos pos = context.getClickedPos();
    BlockState state = level.getBlockState(pos);
    if (state.is(Blocks.LECTERN) && LecternBlock.tryPlaceBook(context.getPlayer(), level, pos, state, context.getItemInHand())) {
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }

  public static void interactWithBlock(PlayerInteractEvent.RightClickBlock event) {
    Level world = event.getLevel();
    if (world.isClientSide() || event.getEntity().isShiftKeyDown()) {
      return;
    }
    BlockPos pos = event.getPos();
    BlockState state = world.getBlockState(pos);
    if (state.is(Blocks.LECTERN)) {
      BlockEntityHelper.get(LecternBlockEntity.class, world, pos).ifPresent(te -> {
        ItemStack book = te.getBook();
        if (!book.isEmpty() && book.getItem() instanceof ILecternBookItem lecternBook && lecternBook.openLecternScreen(world, pos, event.getEntity(), book)) {
          event.setCanceled(true);
        }
      });
    }
  }
}