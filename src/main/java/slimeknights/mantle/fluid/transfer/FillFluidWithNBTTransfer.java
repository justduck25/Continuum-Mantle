package slimeknights.mantle.fluid.transfer;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;

/** Fluid transfer info that fills a fluid into an item, copying its NBT */
public class FillFluidWithNBTTransfer extends FillFluidContainerTransfer {
  public static final Identifier ID = Mantle.getResource("fill_nbt");
  public FillFluidWithNBTTransfer(Ingredient input, ItemOutput filled, FluidIngredient fluid) {
    super(input, filled, fluid);
  }

  @Override
  protected ItemStack getFilled(FluidStack drained) {
    ItemStack filled = super.getFilled(drained);
    CustomData data = drained.get(DataComponents.CUSTOM_DATA);
    if (data != null) {
      filled.set(DataComponents.CUSTOM_DATA, CustomData.of(data.copyTag()));
    }
    PotionContents contents = drained.get(DataComponents.POTION_CONTENTS);
    if (contents != null) {
      filled.set(DataComponents.POTION_CONTENTS, contents);
    } else if (data != null) {
      String potion = data.copyTag().getString("Potion").orElse("");
      if (!potion.isEmpty()) {
        filled.set(DataComponents.POTION_CONTENTS, BuiltInRegistries.POTION.get(Identifier.parse(potion)).map(PotionContents::new).orElse(PotionContents.EMPTY));
      }
    }
    return filled;
  }

  @Override
  public JsonObject serialize(JsonSerializationContext context) {
    JsonObject json = super.serialize(context);
    json.addProperty("type", ID.toString());
    return json;
  }

  /**
   * Unique loader instance
   */
  public static final JsonDeserializer<FillFluidWithNBTTransfer> DESERIALIZER = new Deserializer<>(FillFluidWithNBTTransfer::new);
}
