package slimeknights.mantle.recipe.helper;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Lightweight recipe id/type pair kept for source compatibility with older Mantle datagen helpers. */
public record SimpleFinishedRecipe(Identifier id, RecipeSerializer<?> type) {}
