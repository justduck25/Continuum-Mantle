package slimeknights.mantle.recipe.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ICondition;

/** Condition that checks when a tag is empty. Same as NeoForge's item tag condition but works for any registry. */
public class TagEmptyCondition<T> extends TagCondition<T> {
  public static final MapCodec<TagEmptyCondition<?>> CODEC = TagCondition.codec(TagEmptyCondition::new);

  public TagEmptyCondition(TagKey<T> tag) {
    super(tag);
  }

  public TagEmptyCondition(ResourceKey<? extends Registry<T>> registry, Identifier name) {
    this(TagKey.create(registry, name));
  }

  @Override
  public boolean test(IContext context) {
    return context.getTag(tag).isEmpty();
  }

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }
}
