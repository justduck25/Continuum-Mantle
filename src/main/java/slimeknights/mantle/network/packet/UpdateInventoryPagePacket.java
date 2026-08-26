package slimeknights.mantle.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.BookPageHelper;

/** Updates the saved page in a book in the player's inventory. */
public record UpdateInventoryPagePacket(int slot, String page) implements CustomPacketPayload {
  public static final Type<UpdateInventoryPagePacket> TYPE = new Type<>(Mantle.getResource("update_inventory_page"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateInventoryPagePacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, UpdateInventoryPagePacket::slot, ByteBufCodecs.stringUtf8(100), UpdateInventoryPagePacket::page, UpdateInventoryPagePacket::new);
  @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
  public void handle(IPayloadContext context) { context.enqueueWork(() -> { Player player=context.player(); if(player != null && page != null && slot >= 0 && slot < player.getInventory().getContainerSize()) { ItemStack stack=player.getInventory().getItem(slot); if(!stack.isEmpty()) BookPageHelper.writeSavedPageToBook(stack,page); } }); }
}