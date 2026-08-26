package slimeknights.mantle.recipe.crafting;

import lombok.RequiredArgsConstructor;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Builder for a shaped recipe with fallbacks. */
@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "fallback")
public class ShapedFallbackRecipeBuilder {
  private final ShapedRecipeBuilder base;
  private final List<Identifier> alternatives = new ArrayList<>();

  public ShapedFallbackRecipeBuilder addAlternative(Identifier location) {
    this.alternatives.add(location);
    return this;
  }

  public ShapedFallbackRecipeBuilder addAlternatives(Collection<Identifier> locations) {
    this.alternatives.addAll(locations);
    return this;
  }

  private RecipeOutput wrap(RecipeOutput consumer) {
    return new RecipeOutput() {
      @Override
      public void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, AdvancementHolder advancement, ICondition... conditions) {
        if (recipe instanceof ShapedRecipe shaped) {
          consumer.accept(key, new ShapedFallbackRecipe(shaped, alternatives), advancement, conditions);
        } else {
          consumer.accept(key, recipe, advancement, conditions);
        }
      }

      @Override
      public Advancement.Builder advancement() {
        return consumer.advancement();
      }

      @Override
      public void includeRootAdvancement() {
        consumer.includeRootAdvancement();
      }
    };
  }

  public void build(RecipeOutput consumer) {
    base.save(wrap(consumer));
  }

  public void build(RecipeOutput consumer, Identifier id) {
    base.save(wrap(consumer), ResourceKey.create(Registries.RECIPE, id));
  }

  public void build(RecipeOutput consumer, ResourceKey<Recipe<?>> id) {
    base.save(wrap(consumer), id);
  }
}