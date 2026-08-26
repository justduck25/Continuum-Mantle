package slimeknights.mantle.recipe.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Ingredient for a non-NBT sensitive item from another mod, should never be used outside datagen
 */
public class ItemNameIngredient implements ICustomIngredient {
  public static final MapCodec<ItemNameIngredient> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
    instance.group(
      Identifier.CODEC.listOf().fieldOf("names").forGetter(i -> i.names)
    ).apply(instance, ItemNameIngredient::new)
  );
  public static final StreamCodec<RegistryFriendlyByteBuf, ItemNameIngredient> STREAM_CODEC = StreamCodec.composite(
    Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), i -> i.names,
    ItemNameIngredient::new
  );
  public static final IngredientType<ItemNameIngredient> TYPE = new IngredientType<>(MAP_CODEC, STREAM_CODEC);

  private final List<Identifier> names;
  protected ItemNameIngredient(List<Identifier> names) {
    this.names = names;
  }

  /** Creates a new ingredient from a list of names */
  public static Ingredient from(List<Identifier> names) {
    return new Ingredient(new ItemNameIngredient(names));
  }

  /** Creates a new ingredient from a list of names */
  public static Ingredient from(Identifier... names) {
    return from(Arrays.asList(names));
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    if (stack == null || stack.isEmpty()) {
      return false;
    }
    Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
    return key != null && names.contains(key);
  }

  @Override
  public Stream<Holder<Item>> items() {
    return names.stream().map(BuiltInRegistries.ITEM::get).flatMap(java.util.Optional::stream).map(holder -> (Holder<Item>)holder);
  }

  /** Creates a JSON object for a name */
  private static JsonObject forName(Identifier name) {
    JsonObject json = new JsonObject();
    json.addProperty("item", name.toString());
    return json;
  }

  public JsonElement toJson() {
    if (names.size() == 1) {
      return forName(names.get(0));
    }
    JsonArray array = new JsonArray();
    for (Identifier name : names) {
      array.add(forName(name));
    }
    return array;
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || (obj instanceof ItemNameIngredient that && names.equals(that.names));
  }

  @Override
  public int hashCode() {
    return Objects.hash(names);
  }
}