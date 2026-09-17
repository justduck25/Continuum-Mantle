package slimeknights.mantle.recipe.helper;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;

import javax.annotation.Nullable;

/**
 * Ingredient matching that never calls {@code ItemStack#is(HolderSet)} or {@link Ingredient#isEmpty()}.
 * In 26.1 those dereference unbound named tags and crash with
 * "Tag ... can't be dereferenced during construction".
 */
public final class IngredientHelper {
  private IngredientHelper() {}

  /** Tests an ingredient against a stack using holder/tag lookup that does not bind tag HolderSets. */
  public static boolean test(@Nullable Ingredient ingredient, @Nullable ItemStack stack) {
    if (ingredient == null) {
      return stack == null || stack.isEmpty();
    }
    ICustomIngredient custom = ingredient.getCustomIngredient();
    ItemStack tested = stack == null ? ItemStack.EMPTY : stack;
    if (custom != null) {
      return custom.test(tested);
    }
    HolderSet<Item> values;
    try {
      values = ingredient.getValues();
    } catch (RuntimeException e) {
      return false;
    }
    return values.unwrap().map(
      tag -> matchesTag(tested, tag),
      list -> matchesItems(tested, list)
    );
  }

  private static boolean matchesTag(ItemStack stack, TagKey<Item> tag) {
    return !stack.isEmpty() && stack.typeHolder().is(tag);
  }

  private static boolean matchesItems(ItemStack stack, java.util.List<Holder<Item>> items) {
    if (stack.isEmpty()) {
      return items.isEmpty();
    }
    Item item = stack.getItem();
    for (Holder<Item> holder : items) {
      if (holder.value() == item) {
        return true;
      }
    }
    return false;
  }
}
