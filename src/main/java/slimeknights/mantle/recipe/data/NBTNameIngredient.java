package slimeknights.mantle.recipe.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
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
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Ingredient for a NBT sensitive item from another mod, should never be used outside datagen
 */
public class NBTNameIngredient implements ICustomIngredient {
  public static final MapCodec<NBTNameIngredient> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
    instance.group(
      Identifier.CODEC.fieldOf("item").forGetter(i -> i.name),
      CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(i -> Optional.ofNullable(i.nbt))
    ).apply(instance, (name, nbt) -> new NBTNameIngredient(name, nbt.orElse(null)))
  );
  public static final StreamCodec<RegistryFriendlyByteBuf, NBTNameIngredient> STREAM_CODEC = StreamCodec.composite(
    Identifier.STREAM_CODEC, i -> i.name,
    ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs::optional), i -> Optional.ofNullable(i.nbt),
    (name, nbt) -> new NBTNameIngredient(name, nbt.orElse(null))
  );
  public static final IngredientType<NBTNameIngredient> TYPE = new IngredientType<>(MAP_CODEC, STREAM_CODEC);

  private final Identifier name;
  @Nullable
  private final CompoundTag nbt;

  protected NBTNameIngredient(Identifier name, @Nullable CompoundTag nbt) {
    this.name = name;
    this.nbt = nbt;
  }

  /**
   * Creates an ingredient for the given name and NBT
   * @param name  Item name
   * @param nbt   NBT
   * @return  Ingredient
   */
  public static Ingredient from(Identifier name, CompoundTag nbt) {
    return new Ingredient(new NBTNameIngredient(name, nbt));
  }

  /**
   * Creates an ingredient for an item that must have no NBT
   * @param name  Item name
   * @return  Ingredient
   */
  public static Ingredient from(Identifier name) {
    return new Ingredient(new NBTNameIngredient(name, null));
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Stream<Holder<Item>> items() {
    return Stream.empty();
  }

  public JsonElement toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("type", "neoforge:components");
    json.addProperty("item", name.toString());
    if (nbt != null) {
      json.addProperty("nbt", nbt.toString());
    }
    return json;
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }
}

