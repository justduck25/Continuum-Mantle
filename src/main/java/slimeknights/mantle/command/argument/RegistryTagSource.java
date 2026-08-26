package slimeknights.mantle.command.argument;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

/** Tags coming from a registry */
public record RegistryTagSource<T>(Registry<T> registry) implements TagSource<T> {
  @Override
  public ResourceKey<? extends Registry<T>> key() {
    return registry.key();
  }

  @Override
  public String folder() {
    return key().identifier().getPath();
  }

  @Override
  public boolean hasTag(TagKey<T> tag) {
    return false;
  }

  @Override
  public Stream<TagKey<T>> tagKeys() {
    return Stream.empty();
  }

  @Nullable
  @Override
  public List<T> valuesInTag(TagKey<T> tag) {
    return null;
  }

  @Nullable
  @Override
  public List<Identifier> keysInTag(TagKey<T> tag) {
    return null;
  }

  @Nullable
  @Override
  public T getValue(Identifier key) {
    return registry.getValue(key);
  }

  @Override
  public Stream<TagKey<T>> tagsFor(T value) {
    return Stream.empty();
  }

  @Override
  public Stream<Identifier> valueKeys() {
    return registry.keySet().stream();
  }
}