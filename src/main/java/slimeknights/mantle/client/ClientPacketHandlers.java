package slimeknights.mantle.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.item.ILecternBookItem;
import slimeknights.mantle.network.packet.OpenLecternBookPacket;
import slimeknights.mantle.network.packet.OpenNamedBookPacket;
import slimeknights.mantle.network.packet.SwingArmPacket;
import slimeknights.mantle.util.OffhandCooldownTracker;

/** Client-only packet handlers. Referenced from common code through ClientPacketBridge. */
public final class ClientPacketHandlers {
  private ClientPacketHandlers() {}

  public static void sendToServer(CustomPacketPayload payload) {
    ClientPacketDistributor.sendToServer(payload);
  }
  public static void handleOpenLecternBook(OpenLecternBookPacket payload, IPayloadContext context) {
    context.enqueueWork(() -> {
      if (payload.book().getItem() instanceof ILecternBookItem lecternBook) {
        lecternBook.openLecternScreenClient(payload.pos(), payload.book());
      }
    });
  }

  public static void handleOpenNamedBook(OpenNamedBookPacket payload, IPayloadContext context) {
    context.enqueueWork(() -> {
      BookData bookData = BookLoader.getBook(payload.book());
      if (bookData != null) {
        bookData.openGui(Component.literal("Book"), "", null, null);
      } else {
        Mantle.logger.error("Book not found: {}", payload.book());
      }
    });
  }

  public static void handleSwingArm(SwingArmPacket payload, IPayloadContext context) {
    context.enqueueWork(() -> {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        Entity entity = world.getEntity(payload.entityId());
        if (entity instanceof LivingEntity living) {
          OffhandCooldownTracker.swingHand(living, payload.hand(), false);
        }
      }
    });
  }
}