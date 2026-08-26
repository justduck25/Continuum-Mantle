package slimeknights.mantle.fluid.transfer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Packet to sync fluid container transfer representative items. */
public record FluidContainerTransferPacket(Set<Item> items) implements CustomPacketPayload {
  public static final Type<FluidContainerTransferPacket> TYPE = new Type<>(Mantle.getResource("fluid_container_transfer"));
  public static final StreamCodec<RegistryFriendlyByteBuf, FluidContainerTransferPacket> STREAM_CODEC = StreamCodec.of(
    (buffer, packet) -> {
      buffer.writeVarInt(packet.items.size());
      for (Item item : packet.items) {
        buffer.writeById(BuiltInRegistries.ITEM::getId, item);
      }
    },
    buffer -> {
      int size = buffer.readVarInt();
      List<Item> builder = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        builder.add(buffer.readById(BuiltInRegistries.ITEM::byId));
      }
      return new FluidContainerTransferPacket(Set.copyOf(builder));
    });

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public void handle(IPayloadContext context) {
    context.enqueueWork(() -> FluidContainerTransferManager.INSTANCE.setContainerItems(items));
  }
}