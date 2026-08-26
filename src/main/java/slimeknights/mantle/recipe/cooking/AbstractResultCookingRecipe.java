package slimeknights.mantle.recipe.cooking;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import slimeknights.mantle.recipe.helper.ItemOutput;

/** Base for cooking recipes that resolve their result through Mantle's ItemOutput. */
public abstract class AbstractResultCookingRecipe extends AbstractCookingRecipe implements CookingResultRecipe {
  private final ItemOutput result;

  protected AbstractResultCookingRecipe(String group, CookingBookCategory category, Ingredient ingredient, ItemOutput result, float experience, int cookingTime) {
    super(new Recipe.CommonInfo(true), new AbstractCookingRecipe.CookingBookInfo(category, group), ingredient, template(result), experience, cookingTime);
    this.result = result;
  }

  private static ItemStackTemplate template(ItemOutput result) {
    ItemStack stack = result.get();
    return stack.isEmpty() ? new ItemStackTemplate(Items.AIR) : ItemStackTemplate.fromNonEmptyStack(stack);
  }

  @Override
  public ItemOutput getResult() {
    return result;
  }

  @Override
  public ItemStack assemble(SingleRecipeInput input) {
    return result.copy();
  }
}
