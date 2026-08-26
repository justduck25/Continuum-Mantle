package slimeknights.mantle.recipe.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.function.Function;

/** Common logic for {@link TagEmptyCondition} and {@link TagFilledCondition}. */
@RequiredArgsConstructor
public abstract class TagCondition<T> implements ICondition {
  @Getter
  protected final TagKey<T> tag;

  protected static <C extends TagCondition<?>> MapCodec<C> codec(Function<TagKey<?>,C> constructor) {
    return RecordCodecBuilder.mapCodec(instance -> instance.group(
      Identifier.CODEC.optionalFieldOf("registry", Registries.ITEM.identifier()).forGetter(condition -> condition.getTag().registry().identifier()),
      Identifier.CODEC.fieldOf("tag").forGetter(condition -> condition.getTag().location())
    ).apply(instance, (registry, tag) -> constructor.apply(TagKey.create(ResourceKey.createRegistryKey(registry), tag))));
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(\"" + tag + "\")";
  }
}
