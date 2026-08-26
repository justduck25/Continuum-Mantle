package slimeknights.mantle.recipe.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * Condition checking for a combination of tags having any entries.
 * @param match  List of tags that the entry must match
 * @param ignore Entries in this tag will be ignored towards the match. If null, all entries are considered
 * @param <T>  Registry type
 */
@SuppressWarnings("unused")
public record TagCombinationCondition<T>(List<TagKey<T>> match, @Nullable TagKey<T> ignore) implements ICondition {
  public static final Identifier ID = Mantle.getResource("tag_combination_filled");
  private static final Codec<List<Identifier>> TAG_LIST = Codec.withAlternative(
    Identifier.CODEC.listOf(),
    Identifier.CODEC,
    List::of);
  public static final MapCodec<TagCombinationCondition<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    Identifier.CODEC.optionalFieldOf("registry", Registries.ITEM.identifier()).forGetter(condition -> condition.match.getFirst().registry().identifier()),
    TAG_LIST.fieldOf("match").forGetter(condition -> condition.match.stream().map(TagKey::location).toList()),
    Identifier.CODEC.optionalFieldOf("ignore").forGetter(condition -> condition.ignore == null ? java.util.Optional.empty() : java.util.Optional.of(condition.ignore.location()))
  ).apply(instance, TagCombinationCondition::new));

  private TagCombinationCondition(Identifier registryName, List<Identifier> match, java.util.Optional<Identifier> ignore) {
    this(toTags(registryName, match), toIgnore(registryName, ignore));
  }

  @SuppressWarnings("unchecked")
  private static <T> TagKey<T> toIgnore(Identifier registryName, java.util.Optional<Identifier> ignore) {
    return (TagKey<T>) ignore.map(id -> TagKey.create(ResourceKey.createRegistryKey(registryName), id)).orElse(null);
  }

  private static <T> List<TagKey<T>> toTags(Identifier registryName, List<Identifier> names) {
    ResourceKey<Registry<T>> registry = ResourceKey.createRegistryKey(registryName);
    return names.stream().map(name -> TagKey.create(registry, name)).toList();
  }

  public TagCombinationCondition {
    if (match.isEmpty()) {
      throw new IllegalArgumentException("Must match at least 1 tag");
    }
  }

  /** Creates a new instance ignoring the first tag and matching the rest. */
  @SafeVarargs
  public static <T> TagCombinationCondition<T> match(@Nullable TagKey<T> ignore, TagKey<T>... match) {
    return new TagCombinationCondition<>(List.of(match), ignore);
  }

  /** Creates a new instance matching all the passed tags. */
  @SafeVarargs
  public static <T> TagCombinationCondition<T> intersection(TagKey<T>... match) {
    return match(null, match);
  }

  /** Creates a new instance matching all the passed tags except the ignored tag. */
  public static <T> TagCombinationCondition<T> difference(TagKey<T> match, TagKey<T> ignore) {
    return match(ignore, match);
  }

  @Override
  public boolean test(IContext context) {
    List<Collection<Holder<T>>> tags = match.stream().map(context::getTag).toList();
    Collection<Holder<T>> ignored = ignore == null ? List.of() : context.getTag(ignore);
    if (tags.size() == 1 && ignored.isEmpty()) {
      return !tags.get(0).isEmpty();
    }

    int count = tags.size();
    for (int i = 1; i < count; i++) {
      if (tags.get(i).isEmpty()) {
        return false;
      }
    }

    itemLoop:
    for (Holder<T> entry : tags.get(0)) {
      if (ignored.contains(entry)) {
        continue;
      }
      for (int i = 1; i < count; i++) {
        if (!tags.get(i).contains(entry)) {
          continue itemLoop;
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }
}
