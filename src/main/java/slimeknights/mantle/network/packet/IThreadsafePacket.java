package slimeknights.mantle.network.packet;

import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Legacy packet interface that dispatches work onto the main thread.
 */
@Deprecated(forRemoval = false)
public interface IThreadsafePacket extends ISimplePacket {
  @Override
  default void handle(IPayloadContext context) {
    context.enqueueWork(() -> handleThreadsafe(context));
  }

  /**
   * Handles receiving the packet on the correct thread.
   * @param context Packet context
   */
  void handleThreadsafe(IPayloadContext context);
}