package slimeknights.mantle.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import slimeknights.mantle.Mantle;

/** Packet to tell a client to swing an entity arm, as the vanilla one resets cooldown. */
public record SwingArmPacket(int entityId, InteractionHand hand) implements CustomPacketPayload {
  public static final Type<SwingArmPacket> TYPE = new Type<>(Mantle.getResource("swing_arm"));
  private static final StreamCodec<RegistryFriendlyByteBuf, InteractionHand> HAND_CODEC = StreamCodec.of(
    (buf, hand) -> buf.writeVarInt(hand.ordinal()),
    buf -> InteractionHand.values()[buf.readVarInt()]
  );
  public static final StreamCodec<RegistryFriendlyByteBuf, SwingArmPacket> STREAM_CODEC =
    StreamCodec.composite(net.minecraft.network.codec.ByteBufCodecs.VAR_INT, SwingArmPacket::entityId,
      HAND_CODEC, SwingArmPacket::hand, SwingArmPacket::new);

  public SwingArmPacket(Entity entity, InteractionHand hand) {
    this(entity.getId(), hand);
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}