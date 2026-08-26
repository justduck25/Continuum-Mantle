package slimeknights.mantle.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.BookPageHelper;

/** Updates the saved page in a book held by the player. */
public record UpdateHeldPagePacket(InteractionHand hand, String page) implements CustomPacketPayload {
  public static final Type<UpdateHeldPagePacket> TYPE = new Type<>(Mantle.getResource("update_held_page"));
  private static final StreamCodec<RegistryFriendlyByteBuf, InteractionHand> HAND_CODEC = StreamCodec.of(
    (buf, hand) -> buf.writeVarInt(hand.ordinal()),
    buf -> InteractionHand.values()[buf.readVarInt()]
  );
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateHeldPagePacket> STREAM_CODEC = StreamCodec.composite(HAND_CODEC, UpdateHeldPagePacket::hand, ByteBufCodecs.stringUtf8(100), UpdateHeldPagePacket::page, UpdateHeldPagePacket::new);
  @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
  public void handle(IPayloadContext context) { context.enqueueWork(() -> { Player player=context.player(); if(player != null && page != null) { ItemStack stack=player.getItemInHand(hand); if(!stack.isEmpty()) BookPageHelper.writeSavedPageToBook(stack,page); } }); }
}