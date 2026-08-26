package slimeknights.mantle.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.stream.Stream;

/** Custom empty ingredient replacement for NeoForge 26.1, where vanilla empty ingredients are rejected. */
public enum EmptyIngredient implements ICustomIngredient {
  INSTANCE;

  public static final Ingredient VANILLA = INSTANCE.toVanilla();
  public static final IngredientType<EmptyIngredient> TYPE = new IngredientType<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

  @Override
  public boolean test(ItemStack stack) {
    return stack.isEmpty();
  }

  @Override
  public Stream<Holder<Item>> items() {
    return Stream.empty();
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
  public SlotDisplay display() {
    return SlotDisplay.Empty.INSTANCE;
  }
}
