package slimeknights.mantle.registration.deferred;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

/** Deferred register instance that synchronizes register calls */
public class SynchronizedDeferredRegister<T> {
  private final DeferredRegister<T> internal;

  private SynchronizedDeferredRegister(DeferredRegister<T> internal) {
    this.internal = internal;
  }

  public static <T> SynchronizedDeferredRegister<T> create(DeferredRegister<T> register) {
    return new SynchronizedDeferredRegister<>(register);
  }

  /** Creates a new instance for the given resource key */
  public static <T> SynchronizedDeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String modid) {
    return create(DeferredRegister.create(key, modid));
  }

  /** Registers the given object, synchronized over the internal register */
  public <I extends T> DeferredHolder<T, I> register(final String name, final Supplier<? extends I> sup) {
    synchronized (internal) {
      return internal.register(name, () -> {
        I value = sup.get();
        if (value == null) {
          throw new NullPointerException("Registry supplier returned null for " + name);
        }
        return value;
      });
    }
  }

  /** Registers the internal register with the event bus */
  public void register(IEventBus bus) {
    internal.register(bus);
  }
}