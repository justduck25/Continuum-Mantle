package slimeknights.mantle.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Requests the server to remove and drop the book from a lectern. */
public record DropLecternBookPacket(BlockPos pos) implements CustomPacketPayload {
  public static final Type<DropLecternBookPacket> TYPE = new Type<>(slimeknights.mantle.Mantle.getResource("drop_lectern_book"));
  public static final StreamCodec<RegistryFriendlyByteBuf, DropLecternBookPacket> STREAM_CODEC =
      StreamCodec.composite(BlockPos.STREAM_CODEC, DropLecternBookPacket::pos, DropLecternBookPacket::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public void handle(IPayloadContext context) {
    context.enqueueWork(() -> {
      if (!(context.player() instanceof ServerPlayer player)) {
        return;
      }
      if (!player.level().hasChunkAt(pos)) {
        return;
      }
      BlockState state = player.level().getBlockState(pos);
      if (state.getBlock() instanceof LecternBlock && state.getValue(LecternBlock.HAS_BOOK)) {
        BlockEntity entity = player.level().getBlockEntity(pos);
        if (entity instanceof LecternBlockEntity lectern) {
          ItemStack book = lectern.getBook().copy();
          if (!book.isEmpty()) {
            if (!player.addItem(book)) {
              player.drop(book, false, false);
            }
            lectern.clearContent();
            player.level().setBlock(pos, state.setValue(LecternBlock.POWERED, false).setValue(LecternBlock.HAS_BOOK, false), 3);
            player.level().updateNeighborsAt(pos.below(), state.getBlock());
          }
        }
      }
    });
  }
}