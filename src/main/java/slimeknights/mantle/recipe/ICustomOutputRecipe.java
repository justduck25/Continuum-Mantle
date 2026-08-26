package slimeknights.mantle.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Recipe that has an output other than an {@link ItemStack}.
 * @param <C>  Input type
 */
public interface ICustomOutputRecipe<C extends RecipeInput> extends ICommonRecipe<C> {
  @Override
  default ItemStack assemble(C inv) {
    return ItemStack.EMPTY;
  }
}