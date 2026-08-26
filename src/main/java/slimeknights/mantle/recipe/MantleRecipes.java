package slimeknights.mantle.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.condition.TagCombinationCondition;
import slimeknights.mantle.recipe.condition.TagEmptyCondition;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.mantle.recipe.cooking.BlastingResultRecipe;
import slimeknights.mantle.recipe.cooking.CampfireResultRecipe;
import slimeknights.mantle.recipe.cooking.SmeltingResultRecipe;
import slimeknights.mantle.recipe.cooking.SmokingResultRecipe;
import slimeknights.mantle.recipe.crafting.ShapedFallbackRecipe;
import slimeknights.mantle.recipe.crafting.ShapedRetexturedRecipe;
import slimeknights.mantle.recipe.data.ItemNameIngredient;
import slimeknights.mantle.recipe.data.NBTNameIngredient;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.recipe.ingredient.EmptyIngredient;
import slimeknights.mantle.recipe.ingredient.FluidContainerIngredient;
import slimeknights.mantle.recipe.ingredient.PotionDisplayIngredient;
import slimeknights.mantle.recipe.ingredient.PotionIngredient;

/** Handles any custom recipes added by Mantle. */
public class MantleRecipes {
  private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Mantle.modId);
  private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Mantle.modId);
  private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Mantle.modId);

  private MantleRecipes() {}

  /** Registers this to the bus. */
  public static void init(IEventBus bus) {
    RECIPES.register(bus);
    INGREDIENT_TYPES.register(bus);
    CONDITION_CODECS.register(bus);
  }

  // crafting
  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapedFallbackRecipe>> CRAFTING_SHAPED_FALLBACK = RECIPES.register("crafting_shaped_fallback", () -> new RecipeSerializer<>(ShapedFallbackRecipe.MAP_CODEC, ShapedFallbackRecipe.STREAM_CODEC));
  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapedRetexturedRecipe>> CRAFTING_SHAPED_RETEXTURED = RECIPES.register("crafting_shaped_retextured", () -> new RecipeSerializer<>(ShapedRetexturedRecipe.MAP_CODEC, ShapedRetexturedRecipe.STREAM_CODEC));

  // cooking
  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SmeltingResultRecipe>> SMELTING = RECIPES.register("smelting", () -> LoadableRecipeSerializer.of(SmeltingResultRecipe.LOADABLE));
  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BlastingResultRecipe>> BLASTING = RECIPES.register("blasting", () -> LoadableRecipeSerializer.of(BlastingResultRecipe.LOADABLE));
  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SmokingResultRecipe>> SMOKING = RECIPES.register("smoking", () -> LoadableRecipeSerializer.of(SmokingResultRecipe.LOADABLE));
  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CampfireResultRecipe>> CAMPFIRE = RECIPES.register("campfire", () -> LoadableRecipeSerializer.of(CampfireResultRecipe.LOADABLE));

  // conditions
  public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<TagEmptyCondition<?>>> TAG_EMPTY_CONDITION = CONDITION_CODECS.register("tag_empty", () -> TagEmptyCondition.CODEC);
  public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<TagFilledCondition<?>>> TAG_FILLED_CONDITION = CONDITION_CODECS.register("tag_filled", () -> TagFilledCondition.CODEC);
  public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<TagCombinationCondition<?>>> TAG_COMBINATION_FILLED_CONDITION = CONDITION_CODECS.register("tag_combination_filled", () -> TagCombinationCondition.CODEC);

  // ingredients
  public static final DeferredHolder<IngredientType<?>, IngredientType<EmptyIngredient>> EMPTY_INGREDIENT = INGREDIENT_TYPES.register("empty", () -> EmptyIngredient.TYPE);
  public static final DeferredHolder<IngredientType<?>, IngredientType<PotionDisplayIngredient>> POTION_DISPLAY_INGREDIENT = INGREDIENT_TYPES.register("potion_display", () -> PotionDisplayIngredient.TYPE);
  public static final DeferredHolder<IngredientType<?>, IngredientType<PotionIngredient>> POTION_INGREDIENT = INGREDIENT_TYPES.register("potion", () -> PotionIngredient.TYPE);
  public static final DeferredHolder<IngredientType<?>, IngredientType<FluidContainerIngredient>> FLUID_CONTAINER_INGREDIENT = INGREDIENT_TYPES.register("fluid_container", () -> FluidContainerIngredient.TYPE);
  public static final DeferredHolder<IngredientType<?>, IngredientType<ItemNameIngredient>> ITEM_NAME_INGREDIENT = INGREDIENT_TYPES.register("item_name", () -> ItemNameIngredient.TYPE);
  public static final DeferredHolder<IngredientType<?>, IngredientType<NBTNameIngredient>> NBT_NAME_INGREDIENT = INGREDIENT_TYPES.register("nbt_name", () -> NBTNameIngredient.TYPE);
}
