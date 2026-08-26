package slimeknights.mantle.recipe.crafting;

import lombok.RequiredArgsConstructor;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.common.conditions.ICondition;

@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "fromShaped")
public class ShapedRetexturedRecipeBuilder {
  private final ShapedRecipeBuilder parent;
  private Ingredient texture = null;
  private char textureKey = '\0';
  private boolean matchAll = false;

  public ShapedRetexturedRecipeBuilder setSource(Ingredient texture) {
    this.texture = texture;
    this.textureKey = '\0';
    return this;
  }

  public ShapedRetexturedRecipeBuilder setSource(TagKey<Item> tag) {
    return setSource(Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, tag)));
  }

  /** Sets the texture source to a key from the texture map. */
  public ShapedRetexturedRecipeBuilder setSource(char textureKey) {
    this.textureKey = textureKey;
    this.texture = null;
    return this;
  }

  public ShapedRetexturedRecipeBuilder setMatchAll() {
    this.matchAll = true;
    return this;
  }

  private RecipeOutput wrap(RecipeOutput consumer) {
    return new RecipeOutput() {
      @Override
      public void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, AdvancementHolder advancement, ICondition... conditions) {
        if (recipe instanceof ShapedRecipe shaped) {
          Ingredient tex = texture;
          if (textureKey != '\0') {
            tex = ShapedRetexturedRecipe.emptyIngredient();
          }
          consumer.accept(key, new ShapedRetexturedRecipe(shaped, tex, matchAll), advancement, conditions);
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
    this.validate();
    parent.save(wrap(consumer));
  }

  public void build(RecipeOutput consumer, Identifier location) {
    this.validate();
    parent.save(wrap(consumer), ResourceKey.create(Registries.RECIPE, location));
  }

  public void build(RecipeOutput consumer, ResourceKey<Recipe<?>> location) {
    this.validate();
    parent.save(wrap(consumer), location);
  }

  private void validate() {
    if (texture == null && textureKey == '\0') {
      throw new IllegalStateException("No texture defined for texture recipe");
    }
  }
}