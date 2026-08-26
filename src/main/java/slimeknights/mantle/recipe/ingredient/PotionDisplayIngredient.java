package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.LoadableIngredientSerializer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/** Ingredient that shows all potion variants on the displayed item list. */
public class PotionDisplayIngredient extends ItemIngredient {
  public static final IngredientType<PotionDisplayIngredient> TYPE = LoadableIngredientSerializer.of(RecordLoadable.create(ItemsField.INSTANCE, TAG_FIELD, PotionDisplayIngredient::new));

  private ItemStack[] lastParentStacks = null;
  private ItemStack[] displayStacks = null;

  protected PotionDisplayIngredient(List<Item> items, @Nullable TagKey<Item> tag) {
    super(items, tag);
  }

  public static PotionDisplayIngredient of(List<ItemLike> items) {
    return new PotionDisplayIngredient(toItem(items), null);
  }

  public static PotionDisplayIngredient of(ItemLike... items) {
    return of(List.of(items));
  }

  public static PotionDisplayIngredient of(TagKey<Item> tag) {
    return new PotionDisplayIngredient(List.of(), tag);
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public ItemStack[] getItems() {
    ItemStack[] parentStacks = super.getItems();
    if (lastParentStacks != parentStacks) {
      lastParentStacks = parentStacks;
      displayStacks = BuiltInRegistries.POTION.entrySet().stream()
        .filter(pot -> !pot.getKey().identifier().getPath().equals("empty"))
        .flatMap(pot -> Arrays.stream(parentStacks).map(item -> PotionContents.createItemStack(item.getItem(), BuiltInRegistries.POTION.wrapAsHolder(pot.getValue()))))
        .toArray(ItemStack[]::new);
    }
    return displayStacks;
  }

  @Override
  public SlotDisplay display() {
    return new SlotDisplay.Composite(Arrays.stream(getItems())
      .filter(s -> !s.isEmpty())
      .map(s -> (SlotDisplay) new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(s)))
      .toList());
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  public JsonElement toJson() {
    return TYPE.codec().codec().encodeStart(JsonOps.INSTANCE, this).getOrThrow();
  }
}