package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.recipe.ingredient.EmptyIngredient;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.ArrayList;
import java.util.List;

/** Loadable for ingredients, handling Forge ingredients */
public enum IngredientLoadable implements Loadable<Ingredient> {
  ALLOW_EMPTY,
  DISALLOW_EMPTY;

  @Override
  public Ingredient convert(JsonElement element, String key, TypedMap context) {
    if (this == ALLOW_EMPTY && (element == null || element.isJsonNull() || (element.isJsonArray() && element.getAsJsonArray().isEmpty()))) {
      return EmptyIngredient.VANILLA;
    }
    try {
      return Ingredient.CODEC.parse(JsonHelper.REGISTRY_OPS, element).getOrThrow(JsonParseException::new);
    } catch (JsonParseException e) {
      Ingredient item = itemIngredient(element);
      if (item != null) {
        return item;
      }
      Ingredient custom = legacyCustomIngredient(element);
      if (custom != null) {
        return custom;
      }
      Ingredient tagIngredient = tagIngredient(element);
      if (tagIngredient != null) {
        return tagIngredient;
      }
      throw e;
    }
  }

  /** Supports legacy ingredient objects using {"item":"namespace:path"}. */
  private static Ingredient itemIngredient(JsonElement element) {
    if (!(element instanceof JsonObject json) || !json.has("item")) {
      return null;
    }
    JsonElement itemElement = json.get("item");
    if (!itemElement.isJsonPrimitive() || !itemElement.getAsJsonPrimitive().isString()) {
      return null;
    }
    Identifier itemName = Identifier.parse(itemElement.getAsString());
    Item item = BuiltInRegistries.ITEM.get(itemName).map(holder -> holder.value()).orElse(null);
    if (item == null) {
      throw new JsonParseException("Unknown item '" + itemName + "'");
    }
    return Ingredient.of(item);
  }

  @Override
  public JsonElement serialize(Ingredient object) {
    if (this == DISALLOW_EMPTY && object == EmptyIngredient.VANILLA) {
      throw new IllegalArgumentException("Ingredient cannot be empty");
    }
    return Ingredient.CODEC.encodeStart(JsonHelper.REGISTRY_OPS, object).getOrThrow(JsonSyntaxException::new);
  }

  @Override
  public Ingredient decode(FriendlyByteBuf buffer, TypedMap context) {
    return Ingredient.CONTENTS_STREAM_CODEC.decode(registryBuffer(buffer));
  }

  @Override
  public void encode(FriendlyByteBuf buffer, Ingredient object) {
    Ingredient.CONTENTS_STREAM_CODEC.encode(registryBuffer(buffer), object);
  }

  /** Supports NeoForge custom ingredient JSON generated in the older ingredient_type/children shape. */
  private static Ingredient legacyCustomIngredient(JsonElement element) {
    if (!(element instanceof JsonObject json) || !json.has("neoforge:ingredient_type")) {
      return null;
    }
    String type = json.get("neoforge:ingredient_type").getAsString();
    return switch (type) {
      case "neoforge:compound" -> CompoundIngredient.of(parseChildren(json));
      case "neoforge:intersection" -> IntersectionIngredient.of(parseChildren(json));
      case "neoforge:difference" -> DifferenceIngredient.of(parseChild(json, "base"), parseChild(json, "subtracted"));
      default -> null;
    };
  }

  private static Ingredient[] parseChildren(JsonObject json) {
    JsonArray array = json.getAsJsonArray("children");
    if (array == null) {
      throw new JsonParseException("Missing children for custom ingredient");
    }
    List<Ingredient> children = new ArrayList<>(array.size());
    for (JsonElement child : array) {
      children.add(DISALLOW_EMPTY.convert(child, "children", TypedMap.EMPTY));
    }
    return children.toArray(Ingredient[]::new);
  }

  private static Ingredient parseChild(JsonObject json, String key) {
    JsonElement child = json.get(key);
    if (child == null || child.isJsonNull()) {
      throw new JsonParseException("Missing " + key + " for difference ingredient");
    }
    return DISALLOW_EMPTY.convert(child, key, TypedMap.EMPTY);
  }

  /** Creates a tag ingredient without requiring the tag to already exist in the registry lookup. */
  private static Ingredient tagIngredient(JsonElement element) {
    String tagName = null;
    if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
      String value = element.getAsString();
      if (value.startsWith("#")) {
        tagName = value.substring(1);
      }
    } else if (element instanceof JsonObject json && json.has("tag")) {
      tagName = json.get("tag").getAsString();
    }

    if (tagName != null) {
      TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(tagName));
      return Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, tag));
    }
    return null;
  }

  private static RegistryFriendlyByteBuf registryBuffer(FriendlyByteBuf buffer) {
    if (buffer instanceof RegistryFriendlyByteBuf registryBuffer) {
      return registryBuffer;
    }
    throw new IllegalArgumentException("Ingredient serialization requires RegistryFriendlyByteBuf");
  }
}
