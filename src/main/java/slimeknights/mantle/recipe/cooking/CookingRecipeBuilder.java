package slimeknights.mantle.recipe.cooking;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.datafixers.util.Function7;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;

/** Builder for {@link SmeltingResultRecipe}, {@link BlastingResultRecipe}, {@link SmokingResultRecipe}, and {@link CampfireResultRecipe}. */
@SuppressWarnings({"unchecked", "unused"})
@CanIgnoreReturnValue
public class CookingRecipeBuilder<T extends CookingRecipeBuilder<T>> extends AbstractRecipeBuilder<T> {
  protected final ItemOutput result;
  protected float experience = 1.0f;
  protected int cookingTime = 200;
  protected Ingredient ingredient = null;
  protected CookingBookCategory category = CookingBookCategory.MISC;
  protected CookingType type = CookingType.SMELTING;

  protected CookingRecipeBuilder(ItemOutput result) {
    this.result = result;
  }

  public static CookingRecipeBuilder<?> builder(ItemOutput result) {
    return new CookingRecipeBuilder<>(result);
  }

  public static CookingRecipeBuilder<?> builder(ItemLike output, int amount) {
    return builder(ItemOutput.fromItem(output, amount));
  }

  public static CookingRecipeBuilder<?> builder(ItemLike output) {
    return builder(output, 1);
  }

  public static CookingRecipeBuilder<?> builder(TagKey<Item> result, int amount) {
    return builder(ItemOutput.fromTag(result, amount));
  }

  public static CookingRecipeBuilder<?> builder(TagKey<Item> result) {
    return builder(result, 1);
  }

  public T type(CookingType type) {
    this.type = type;
    return (T) this;
  }

  public T requires(Ingredient ingredient) {
    this.ingredient = ingredient;
    return (T) this;
  }

  public T requires(ItemLike item) {
    return requires(Ingredient.of(item));
  }

  public T requires(TagKey<Item> tag) {
    return requires(Ingredient.of(BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ITEM).getOrThrow(tag)));
  }

  public T experience(float experience) {
    this.experience = experience;
    return (T) this;
  }

  public T cookingTime(int cookingTime) {
    this.cookingTime = cookingTime;
    return (T) this;
  }

  private <R extends Recipe<?>> T save(RecipeOutput consumer, Identifier id, Function7<Identifier,String,CookingBookCategory,Ingredient,ItemOutput,Float,Integer,R> constructor, int cookingTime) {
    if (ingredient == null) {
      throw new IllegalStateException("Ingredient must be set");
    }
    ResourceKey<Recipe<?>> key = recipeKey(id);
    AdvancementHolder advancement = buildOptionalAdvancement(key, "cooking");
    consumer.accept(key, constructor.apply(id, group, category, ingredient, result, experience, cookingTime), advancement);
    return (T) this;
  }

  public T saveSmelting(RecipeOutput consumer, Identifier id) {
    return save(consumer, id, SmeltingResultRecipe::new, cookingTime);
  }

  public T saveBlasting(RecipeOutput consumer, Identifier id) {
    return save(consumer, id, BlastingResultRecipe::new, cookingTime / 2);
  }

  public T saveSmoking(RecipeOutput consumer, Identifier id) {
    return save(consumer, id, SmokingResultRecipe::new, cookingTime / 2);
  }

  public T saveCampfire(RecipeOutput consumer, Identifier id) {
    return save(consumer, id, CampfireResultRecipe::new, cookingTime * 3);
  }

  @Override
  public void save(RecipeOutput consumer) {
    save(consumer, Loadables.ITEM.getKey(result.get().getItem()));
  }

  @Override
  public void save(RecipeOutput consumer, Identifier id) {
    switch (type) {
      case SMELTING -> saveSmelting(consumer, id);
      case BLASTING -> saveBlasting(consumer, id);
      case SMOKING -> saveSmoking(consumer, id);
      case CAMPFIRE -> saveCampfire(consumer, id);
    }
  }

  public enum CookingType { SMELTING, BLASTING, SMOKING, CAMPFIRE }
}
