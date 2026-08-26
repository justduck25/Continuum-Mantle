package slimeknights.mantle.registration.object;

import lombok.Getter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Object containing registry entries for a fluid with no flowing form.
 * @param <F>  Fluid class
 * @see FlowingFluidObject
 */
@SuppressWarnings("WeakerAccess")
public class FluidObject<F extends Fluid> implements Supplier<F>, ItemLike, IdAwareObject {
  /** Fluid name, used for tag creation */
  @Getter @Nonnull
  protected final Identifier id;

  /** Tag in the forge namespace, crafting equivalence */
  @Getter @Nullable
  protected final TagKey<Fluid> commonTag;
  private final Supplier<? extends FluidType> type;
  private final Supplier<? extends F> still;

  /** Main constructor */
  public FluidObject(Identifier id, @Nullable String tagName, Supplier<? extends FluidType> type, Supplier<? extends F> still) {
    this.id = id;
    this.commonTag = tagName == null ? null : TagKey.create(Registries.FLUID, Mantle.commonResource(tagName));
    this.type = type;
    this.still = still;
  }

  /** Gets the fluid type for this object */
  public FluidType getType() {
    return type.get();
  }

  /**
   * Gets the still form of this fluid
   * @return  Still form
   */
  @Override
  public F get() {
    return Objects.requireNonNull(still.get(), "Fluid object missing still fluid");
  }

  /**
   * Gets the bucket form of this fluid.
   * @return  Bucket form, or null if no bucket
   * @see #asItem()
   */
  /** Gets an output matching this fluid object for data generation. */
  public FluidOutput result(int amount) {
    return commonTag != null ? FluidOutput.fromTag(commonTag, amount) : FluidOutput.fromFluid(get(), amount);
  }

  /** Gets an ingredient matching this fluid object for data generation. */
  public FluidIngredient ingredient(int amount) {
    return commonTag != null ? FluidIngredient.of(commonTag, amount) : FluidIngredient.of(get(), amount);
  }
  @Nullable
  public Item getBucket() {
    Item bucket = still.get().getBucket();
    if (bucket == Items.AIR) {
      return null;
    }
    return bucket;
  }

  /**
   * Gets the bucket form of this fluid
   * @return  Bucket form, or air if no bucket
   * @see #getBucket()
   */
  @Override
  public Item asItem() {
    return still.get().getBucket();
  }
}