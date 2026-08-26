package slimeknights.mantle.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/** Small compatibility helper around NeoForge's CustomPacketPayload distributor. */
public final class NetworkWrapper {
  private NetworkWrapper() {}

  public static void sendToServer(CustomPacketPayload payload) {
    ClientPacketBridge.sendToServer(payload);
  }

  public static void sendTo(CustomPacketPayload payload, ServerPlayer player) {
    if (!(player instanceof FakePlayer)) {
      PacketDistributor.sendToPlayer(player, payload);
    }
  }

  public static void sendTo(CustomPacketPayload payload, Player player) {
    if (player instanceof ServerPlayer serverPlayer) {
      sendTo(payload, serverPlayer);
    }
  }

  public static void sendToClientsAround(CustomPacketPayload payload, ServerLevel level, BlockPos pos) {
    PacketDistributor.sendToPlayersTrackingChunk(level, level.getChunkAt(pos).getPos(), payload);
  }

  public static void sendToTrackingAndSelf(CustomPacketPayload payload, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
  }

  public static void sendToTracking(CustomPacketPayload payload, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
  }

  public static void sendVanillaPacket(Packet<?> packet, Entity entity) {
    if (entity instanceof ServerPlayer player) {
      player.connection.send(packet);
    }
  }
}