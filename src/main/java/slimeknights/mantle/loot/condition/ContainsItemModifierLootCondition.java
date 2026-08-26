package slimeknights.mantle.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jspecify.annotations.Nullable;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.JsonHelper;

import java.lang.reflect.Type;
import java.util.List;

/** Loot condition requiring one of the existing items is the given stack */
public class ContainsItemModifierLootCondition implements ILootModifierCondition {
  public static final Identifier ID = Mantle.getResource("contains_item");

  private final @Nullable Ingredient ingredient;
  private final @Nullable TagKey<Item> tag;
  private final int amountNeeded;

  public ContainsItemModifierLootCondition(Ingredient ingredient) {
    this(ingredient, 1);
  }

  public ContainsItemModifierLootCondition(Ingredient ingredient, int amountNeeded) {
    this.ingredient = ingredient;
    this.tag = null;
    this.amountNeeded = amountNeeded;
  }

  private ContainsItemModifierLootCondition(TagKey<Item> tag, int amountNeeded) {
    this.ingredient = null;
    this.tag = tag;
    this.amountNeeded = amountNeeded;
  }

  @Override
  public boolean test(List<ItemStack> generatedLoot, LootContext context) {
    HolderSet<Item> tagSet = null;
    if (tag != null) {
      tagSet = context.getLevel().registryAccess().lookupOrThrow(Registries.ITEM).get(tag).orElse(null);
      if (tagSet == null) {
        return false;
      }
    }

    int matched = 0;
    for (ItemStack stack : generatedLoot) {
      boolean matches = ingredient != null ? ingredient.test(stack) : tagSet.contains(stack.typeHolder());
      if (matches) {
        matched += stack.getCount();
        if (matched >= amountNeeded) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public JsonObject serialize(JsonSerializationContext context) {
    JsonObject json = new JsonObject();
    json.addProperty("type", ID.toString());
    if (tag != null) {
      json.addProperty("ingredient", "#" + tag.location());
    } else {
      json.add("ingredient", Ingredient.CODEC.encodeStart(JsonHelper.REGISTRY_OPS, ingredient).getOrThrow(JsonParseException::new));
    }
    if (amountNeeded != 1) {
      json.addProperty("needed", amountNeeded);
    }
    return json;
  }

  /** Parses this from JSON */
  public static ContainsItemModifierLootCondition deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    JsonObject json = GsonHelper.convertToJsonObject(element, "condition");
    JsonElement ingredientJson = json.get("ingredient");
    int needed = GsonHelper.getAsInt(json, "needed", 1);
    if (ingredientJson != null && ingredientJson.isJsonPrimitive() && ingredientJson.getAsJsonPrimitive().isString()) {
      String value = ingredientJson.getAsString();
      if (value.startsWith("#")) {
        Identifier tagId = Identifier.parse(value.substring(1));
        return new ContainsItemModifierLootCondition(TagKey.create(Registries.ITEM, tagId), needed);
      }
    }
    Ingredient ingredient = Ingredient.CODEC.parse(JsonHelper.REGISTRY_OPS, ingredientJson).getOrThrow(JsonParseException::new);
    return new ContainsItemModifierLootCondition(ingredient, needed);
  }
}
