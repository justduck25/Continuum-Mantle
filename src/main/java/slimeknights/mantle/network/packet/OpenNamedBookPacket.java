package slimeknights.mantle.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import slimeknights.mantle.Mantle;

/** Opens a Mantle book by book id on the client. */
public record OpenNamedBookPacket(Identifier book) implements CustomPacketPayload {
  public static final Type<OpenNamedBookPacket> TYPE = new Type<>(Mantle.getResource("open_named_book"));
  public static final StreamCodec<RegistryFriendlyByteBuf, OpenNamedBookPacket> STREAM_CODEC =
    StreamCodec.composite(Identifier.STREAM_CODEC, OpenNamedBookPacket::book, OpenNamedBookPacket::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}