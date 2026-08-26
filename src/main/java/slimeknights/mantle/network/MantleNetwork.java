package slimeknights.mantle.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferPacket;
import slimeknights.mantle.network.ClientPacketBridge;
import slimeknights.mantle.network.packet.DropLecternBookPacket;
import slimeknights.mantle.network.packet.OpenLecternBookPacket;
import slimeknights.mantle.network.packet.OpenNamedBookPacket;
import slimeknights.mantle.network.packet.SwingArmPacket;
import slimeknights.mantle.network.packet.UpdateHeldPagePacket;
import slimeknights.mantle.network.packet.UpdateInventoryPagePacket;
import slimeknights.mantle.network.packet.UpdateLecternPagePacket;

/** NeoForge payload registration for Mantle. */
public final class MantleNetwork {
  private MantleNetwork() {}

  public static void registerPackets(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar registrar = event.registrar("2");
    registrar.playToServer(DropLecternBookPacket.TYPE, DropLecternBookPacket.STREAM_CODEC, (payload, context) -> payload.handle(context));
    registrar.playToClient(OpenLecternBookPacket.TYPE, OpenLecternBookPacket.STREAM_CODEC, ClientPacketBridge::handle);
    registrar.playToClient(OpenNamedBookPacket.TYPE, OpenNamedBookPacket.STREAM_CODEC, ClientPacketBridge::handle);
    registrar.playToClient(SwingArmPacket.TYPE, SwingArmPacket.STREAM_CODEC, ClientPacketBridge::handle);
    registrar.playToClient(FluidContainerTransferPacket.TYPE, FluidContainerTransferPacket.STREAM_CODEC, (payload, context) -> payload.handle(context));
    registrar.playToServer(UpdateHeldPagePacket.TYPE, UpdateHeldPagePacket.STREAM_CODEC, (payload, context) -> payload.handle(context));
    registrar.playToServer(UpdateInventoryPagePacket.TYPE, UpdateInventoryPagePacket.STREAM_CODEC, (payload, context) -> payload.handle(context));
    registrar.playToServer(UpdateLecternPagePacket.TYPE, UpdateLecternPagePacket.STREAM_CODEC, (payload, context) -> payload.handle(context));
  }
}