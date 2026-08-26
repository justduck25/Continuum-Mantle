package slimeknights.mantle.util;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class RegistryHelper {
  private RegistryHelper() {}

  @Nullable
  private static HolderLookup.Provider fallbackRegistryAccess;

  @SuppressWarnings("unchecked")
  public static <T> Registry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
    Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(key.identifier());
    if (registry == null && fallbackRegistryAccess != null) {
      try {
        var lookup = fallbackRegistryAccess.lookupOrThrow((ResourceKey) key);
        return (Registry<T>) lookup;
      } catch (Exception e) {
        return null;
      }
    }
    return registry;
  }

  public static void setFallbackRegistryAccess(@Nullable HolderLookup.Provider provider) {
    fallbackRegistryAccess = provider;
  }

  @Nullable
  public static HolderLookup.Provider getFallbackRegistryAccess() {
    return fallbackRegistryAccess;
  }

  public static <T> Stream<Holder<T>> getTagStream(Registry<T> registry, TagKey<T> key) {
    return StreamSupport.stream(registry.getTagOrEmpty(key).spliterator(), false);
  }

  public static <T> Stream<T> getTagValueStream(Registry<T> registry, TagKey<T> key) {
    return getTagStream(registry, key).map(Holder::value);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T> Stream<T> getTagValueStream(TagKey<T> key) {
    Registry<T> registry = getRegistry(key.registry());
    if (registry != null) {
      return getTagValueStream(registry, key);
    }
    if (fallbackRegistryAccess != null) {
      try {
        HolderLookup.RegistryLookup<T> lookup = fallbackRegistryAccess.lookupOrThrow((ResourceKey) key.registry());
        return lookup.get(key).stream().flatMap(named -> named.stream()).map(Holder::value);
      } catch (Exception ignored) {
        return Stream.empty();
      }
    }
    return Stream.empty();
  }

  public static <T> Supplier<T> getHolder(DefaultedRegistry<T> registry, T entry) {
    return () -> entry;
  }

  public static <T> boolean contains(Registry<T> registry, TagKey<T> tag, T value) {
    return getTagValueStream(registry, tag).anyMatch(entry -> entry == value);
  }

  public static <T> boolean contains(TagKey<T> tag, T value) {
    return getTagValueStream(tag).anyMatch(entry -> entry == value);
  }

  @SuppressWarnings("deprecation")
  public static boolean contains(TagKey<Block> tag, Block value) {
    return value.builtInRegistryHolder().is(tag);
  }

  @SuppressWarnings("deprecation")
  public static boolean contains(TagKey<Item> tag, Item value) {
    return value.builtInRegistryHolder().is(tag);
  }

  @SuppressWarnings("deprecation")
  public static boolean contains(TagKey<EntityType<?>> tag, EntityType<?> value) {
    return value.builtInRegistryHolder().is(tag);
  }

  @SuppressWarnings("deprecation")
  public static boolean contains(TagKey<Fluid> tag, Fluid value) {
    return value.builtInRegistryHolder().is(tag);
  }
}