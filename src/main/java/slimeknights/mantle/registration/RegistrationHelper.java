package slimeknights.mantle.registration;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.properties.WoodType;
import slimeknights.mantle.registration.deferred.ItemDeferredRegister;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Minimal registration helper kept for NeoForge compile. */
public final class RegistrationHelper {
  private RegistrationHelper() {}

  public static final Item.Properties BUCKET_PROPS = ItemDeferredRegister.registerSharedProperties(new Item.Properties().stacksTo(1));

  public static <T> Supplier<T> getHolder(DefaultedRegistry<T> registry, T entry) {
    return () -> entry;
  }

  public static <T> Supplier<T> getCastedHolder(DefaultedRegistry<T> registry, T entry) {
    return () -> entry;
  }

  public static void registerWoodType(WoodType type) {
  }

  public static void forEachWoodType(Consumer<WoodType> consumer) {
    consumer.accept(WoodType.OAK);
  }

  @SuppressWarnings("unchecked")
  public static <A extends ArgumentType<?>> Class<A> genericArgumentType(Class<? super A> argumentClass) {
    return (Class<A>) argumentClass;
  }

  @SuppressWarnings("unchecked")
  public static <T> T injected() {
    return null;
  }
}