package slimeknights.mantle.recipe.data;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Common logic to create a recipe builder class.
 * @param <T> builder type
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class AbstractRecipeBuilder<T extends AbstractRecipeBuilder<T>> {
  /** Advancement builder for this class. */
  protected final Advancement.Builder advancementBuilder = Advancement.Builder.advancement();
  /** Group for this recipe. */
  @Nonnull
  protected String group = "";
  private boolean hasCriteria = false;

  /** Adds a criterion to the recipe. */
  @SuppressWarnings("unchecked")
  public T unlockedBy(String name, Criterion<?> criteria) {
    this.advancementBuilder.addCriterion(name, criteria);
    this.hasCriteria = true;
    return (T)this;
  }

  /** Sets the group for this recipe. */
  @SuppressWarnings("unchecked")
  public T group(String group) {
    this.group = group;
    return (T)this;
  }

  /** Sets the group for this recipe. */
  public T group(Identifier group) {
    if ("minecraft".equals(group.getNamespace())) {
      return group(group.getPath());
    }
    return group(group.toString());
  }

  /** Builds the recipe with a default recipe ID, typically based on the output. */
  public abstract void save(RecipeOutput consumerIn);

  /** Builds the recipe. */
  public abstract void save(RecipeOutput consumerIn, Identifier id);

  /** Builds the recipe using a modern recipe key. */
  public void save(RecipeOutput consumerIn, ResourceKey<Recipe<?>> id) {
    save(consumerIn, id.identifier());
  }

  /** Creates the recipe key for the given identifier. */
  protected ResourceKey<Recipe<?>> recipeKey(Identifier id) {
    return ResourceKey.create(Registries.RECIPE, id);
  }

  private AdvancementHolder buildAdvancementInternal(ResourceKey<Recipe<?>> key, String folder) {
    Identifier id = key.identifier();
    Identifier advancementId = Identifier.fromNamespaceAndPath(id.getNamespace(), "recipes/" + folder + "/" + id.getPath());
    this.advancementBuilder
      .parent(Identifier.withDefaultNamespace("recipes/root"))
      .rewards(AdvancementRewards.Builder.recipe(key))
      .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(key))
      .requirements(AdvancementRequirements.Strategy.OR);
    return this.advancementBuilder.build(advancementId);
  }

  /** Builds and validates the advancement, intended to be called in {@link #save(RecipeOutput, Identifier)}. */
  protected AdvancementHolder buildAdvancement(ResourceKey<Recipe<?>> key, String folder) {
    if (!this.hasCriteria) {
      throw new IllegalStateException("No way of obtaining recipe " + key.identifier());
    }
    return buildAdvancementInternal(key, folder);
  }

  /** Builds an optional advancement, or null if no criterion was defined. */
  @Nullable
  protected AdvancementHolder buildOptionalAdvancement(ResourceKey<Recipe<?>> key, String folder) {
    if (!this.hasCriteria) {
      return null;
    }
    return buildAdvancementInternal(key, folder);
  }
}
