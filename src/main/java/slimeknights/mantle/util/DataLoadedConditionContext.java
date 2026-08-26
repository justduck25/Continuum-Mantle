package slimeknights.mantle.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/** Condition context to use when data has already been loaded, used in books for processing their conditions for instance. */
public enum DataLoadedConditionContext implements ICondition.IContext {
  INSTANCE;

  @Override
  public <T> boolean isTagLoaded(TagKey<T> key) {
    return !getTag(key).isEmpty();
  }

  @Override
  public <T> Collection<Holder<T>> getTag(TagKey<T> key) {
    Registry<T> registry = RegistryHelper.getRegistry(key.registry());
    if (registry != null) {
      return RegistryHelper.getTagStream(registry, key).map(holder -> (Holder<T>)holder).toList();
    }
    return Set.of();
  }

  public <T> Map<Identifier,Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> key) {
    return Map.of();
  }
}