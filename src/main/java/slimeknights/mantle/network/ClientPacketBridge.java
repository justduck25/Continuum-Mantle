package slimeknights.mantle.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.packet.OpenLecternBookPacket;
import slimeknights.mantle.network.packet.OpenNamedBookPacket;
import slimeknights.mantle.network.packet.SwingArmPacket;

import java.lang.reflect.Method;

/** Keeps client-only packet handlers out of the dedicated-server classloader. */
public final class ClientPacketBridge {
  private static final String HANDLER_CLASS = "slimeknights.mantle.client.ClientPacketHandlers";

  private ClientPacketBridge() {}

  public static void sendToServer(CustomPacketPayload payload) {
    invoke("sendToServer", CustomPacketPayload.class, payload);
  }

  public static void handle(OpenLecternBookPacket payload, IPayloadContext context) {
    invoke("handleOpenLecternBook", OpenLecternBookPacket.class, payload, context);
  }

  public static void handle(OpenNamedBookPacket payload, IPayloadContext context) {
    invoke("handleOpenNamedBook", OpenNamedBookPacket.class, payload, context);
  }

  public static void handle(SwingArmPacket payload, IPayloadContext context) {
    invoke("handleSwingArm", SwingArmPacket.class, payload, context);
  }

  private static <T> void invoke(String methodName, Class<T> payloadClass, T payload) {
    if (!"CLIENT".equals(FMLEnvironment.getDist().name())) {
      return;
    }
    try {
      Class<?> handlers = Class.forName(HANDLER_CLASS);
      Method method = handlers.getMethod(methodName, payloadClass);
      method.invoke(null, payload);
    } catch (ReflectiveOperationException e) {
      Mantle.logger.error("Failed to dispatch client packet {}", methodName, e);
    }
  }

  private static <T> void invoke(String methodName, Class<T> payloadClass, T payload, IPayloadContext context) {
    if (!"CLIENT".equals(FMLEnvironment.getDist().name())) {
      return;
    }
    try {
      Class<?> handlers = Class.forName(HANDLER_CLASS);
      Method method = handlers.getMethod(methodName, payloadClass, IPayloadContext.class);
      method.invoke(null, payload, context);
    } catch (ReflectiveOperationException e) {
      Mantle.logger.error("Failed to dispatch client packet {}", methodName, e);
    }
  }
}