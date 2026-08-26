package slimeknights.mantle.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.mantle.util.BookPageHelper;

/** Updates the saved page in a book placed on a lectern. */
public record UpdateLecternPagePacket(BlockPos pos, String page) implements CustomPacketPayload {
  public static final Type<UpdateLecternPagePacket> TYPE = new Type<>(Mantle.getResource("update_lectern_page"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateLecternPagePacket> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, UpdateLecternPagePacket::pos, ByteBufCodecs.stringUtf8(100), UpdateLecternPagePacket::page, UpdateLecternPagePacket::new);
  @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
  public void handle(IPayloadContext context) { context.enqueueWork(() -> { if(context.player() != null && page != null) { BlockEntityHelper.get(LecternBlockEntity.class, context.player().level(), pos).ifPresent(te -> { ItemStack stack=te.getBook(); if(!stack.isEmpty()) BookPageHelper.writeSavedPageToBook(stack,page); }); } }); }
}