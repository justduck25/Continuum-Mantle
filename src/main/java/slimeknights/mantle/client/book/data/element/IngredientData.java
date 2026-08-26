package slimeknights.mantle.client.book.data.element;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.StringUtil;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.data.loadable.common.ItemStackLoadable;
import slimeknights.mantle.recipe.ingredient.SizedIngredient;
import slimeknights.mantle.util.typed.TypedMap;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class IngredientData implements IDataElement {
  public SizedIngredient[] ingredients = new SizedIngredient[0];
  public String action;

  private transient String error;
  private transient NonNullList<ItemStack> items;
  private transient boolean customData;

  public NonNullList<ItemStack> getItems() {
    return this.items;
  }

  public static IngredientData getItemStackData(ItemStack stack) {
    IngredientData data = new IngredientData();
    data.customData = true;
    if (stack.isEmpty()) {
      data.items = NonNullList.withSize(1, data.getMissingItem("Empty item stack"));
    } else {
      data.items = NonNullList.withSize(1, stack);
    }

    return data;
  }

  public static IngredientData getItemStackData(NonNullList<ItemStack> items) {
    IngredientData data = new IngredientData();
    data.customData = true;
    NonNullList<ItemStack> filtered = NonNullList.create();
    for (ItemStack stack : items) {
      if (!stack.isEmpty()) {
        filtered.add(stack);
      }
    }
    data.items = filtered.isEmpty() ? NonNullList.withSize(1, data.getMissingItem("No display items resolved")) : filtered;

    return data;
  }

  @Override
  public void load(BookRepository source) {
    if (this.customData) {
      if (this.items == null || this.items.isEmpty()) {
        this.items = NonNullList.withSize(1, getMissingItem("No display items resolved"));
      }
      return;
    }
    ArrayList<ItemStack> stacks = new ArrayList<>();
    for(SizedIngredient ingredient : ingredients) {
      if(ingredient == null) {
        continue;
      }

      stacks.addAll(ingredient.getMatchingStacks());
    }

    if(ingredients == null || stacks.isEmpty() || !StringUtil.isNullOrEmpty(error)) {
      items = NonNullList.withSize(1, getMissingItem());
      return;
    }

    items = NonNullList.of(getMissingItem(), stacks.toArray(new ItemStack[0]));
  }

  private ItemStack getMissingItem() {
    return getMissingItem(this.error);
  }

  private ItemStack getMissingItem(String error) {
    ItemStack missingItem = new ItemStack(Items.BARRIER);

    missingItem.set(DataComponents.CUSTOM_NAME, Component.literal("Error Loading Item"));
    CompoundTag display = new CompoundTag();
    ListTag lore = new ListTag();
    if(!StringUtil.isNullOrEmpty(error)) {
      lore.add(StringTag.valueOf("\u00A7r\u00A7eError:"));
      lore.add(StringTag.valueOf("\u00A7r\u00A7e" + error));
    }
    display.put("Lore", lore);

    return missingItem;
  }

  public static class Deserializer implements JsonDeserializer<IngredientData> {
    @Override
    public IngredientData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      IngredientData data = new IngredientData();
      readAction(data, json);

      ItemStack displayStack = readDisplayStack(json);
      if (displayStack != null) {
        data.customData = true;
        data.items = NonNullList.withSize(1, displayStack);
        return data;
      }

      if(json.isJsonArray()) {
        JsonArray array = json.getAsJsonArray();
        data.ingredients = new SizedIngredient[array.size()];

        for(int i = 0; i < array.size(); i++) {
          try {
            data.ingredients[i] = readIngredient(array.get(i));
          } catch (Exception e) {
            data.ingredients[i] = SizedIngredient.of(Ingredient.of(Items.BARRIER));
          }
        }

        return data;
      }

      try {
        data.ingredients = new SizedIngredient[]{ readIngredient(json) };
      } catch (Exception e) {
        data.error = e.getMessage();
        return data;
      }

      return data;
    }

    private static void readAction(IngredientData data, JsonElement json) {
      if (!json.isJsonObject()) {
        return;
      }
      JsonObject object = json.getAsJsonObject();
      if (object.has("action")) {
        JsonElement action = object.get("action");
        if (action.isJsonPrimitive()) {
          JsonPrimitive primitive = action.getAsJsonPrimitive();
          if (primitive.isString()) {
            data.action = primitive.getAsString();
          }
        }
      }
    }

    private static ItemStack readDisplayStack(JsonElement json) {
      if (!json.isJsonObject()) {
        return null;
      }
      JsonObject object = json.getAsJsonObject();
      if (object.has("item") && object.get("item").isJsonObject()) {
        return readDisplayStack(object.get("item"));
      }
      if (object.has("type") && object.has("item") && object.get("item").isJsonPrimitive()) {
        JsonObject stackJson = object.deepCopy();
        stackJson.remove("type");
        return ItemStackLoadable.REQUIRED_STACK_NBT.deserialize(stackJson, TypedMap.EMPTY);
      }
      if (object.has("nbt") && object.has("item") && object.get("item").isJsonPrimitive()) {
        return ItemStackLoadable.REQUIRED_STACK_NBT.deserialize(object, TypedMap.EMPTY);
      }
      return null;
    }

    private SizedIngredient readIngredient(JsonElement json) {
      if(json.isJsonPrimitive()) {
        JsonPrimitive primitive = json.getAsJsonPrimitive();

        if(primitive.isString()) {
          Identifier itemId = Identifier.parse(primitive.getAsString());
          Item item = BuiltInRegistries.ITEM.get(itemId).map(holder -> holder.value()).orElse(Items.BARRIER);
          return SizedIngredient.fromItems(item);
        }
      }

      if(!json.isJsonObject()) {
        throw new JsonParseException("Must be an array, string or JSON object");
      }

      JsonObject object = json.getAsJsonObject();
      return SizedIngredient.deserialize(object);
    }
  }
}
