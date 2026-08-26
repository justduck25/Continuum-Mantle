package slimeknights.mantle.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Opens a Mantle lectern book on the client. */
public record OpenLecternBookPacket(BlockPos pos, ItemStack book) implements CustomPacketPayload {
  public static final Type<OpenLecternBookPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("mantle", "open_lectern_book"));
  public static final StreamCodec<RegistryFriendlyByteBuf, OpenLecternBookPacket> STREAM_CODEC =
    StreamCodec.composite(BlockPos.STREAM_CODEC, OpenLecternBookPacket::pos,
      ItemStack.OPTIONAL_STREAM_CODEC, OpenLecternBookPacket::book, OpenLecternBookPacket::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}