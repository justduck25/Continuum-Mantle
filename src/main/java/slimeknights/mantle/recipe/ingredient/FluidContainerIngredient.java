package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.LoadableCodec;
import slimeknights.mantle.registration.object.FluidObject;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;

/** Ingredient that matches a container of fluid */
@SuppressWarnings("unused")  // API
public class FluidContainerIngredient implements ICustomIngredient {
  public static final Identifier ID = Mantle.getResource("fluid_container");
  public static final IngredientType<FluidContainerIngredient> TYPE = new IngredientType<>(Serializer.MAP_CODEC, Serializer.STREAM_CODEC);

  /** Ingredient to use for matching */
  private final FluidIngredient fluidIngredient;
  /** Internal ingredient to display the ingredient recipe viewers */
  @Nullable
  private final Ingredient display;
  private ItemStack[] displayStacks;
  protected FluidContainerIngredient(FluidIngredient fluidIngredient, @Nullable Ingredient display) {
    this.fluidIngredient = fluidIngredient;
    this.display = display;
  }

  /** Creates an instance from a fluid ingredient with a display container */
  public static FluidContainerIngredient fromIngredient(FluidIngredient ingredient, Ingredient display) {
    return new FluidContainerIngredient(ingredient, display);
  }

  /** Creates an instance from a fluid ingredient with no display, not recommended */
  public static FluidContainerIngredient fromIngredient(FluidIngredient ingredient) {
    return new FluidContainerIngredient(ingredient, null);
  }

  /** Creates an instance from a fluid ingredient with a display container */
  public static FluidContainerIngredient fromFluid(FluidObject<?> fluid) {
    return fromIngredient(FluidIngredient.of(fluid.get(), FluidType.BUCKET_VOLUME), Ingredient.of(fluid.getBucket()));
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    // first, must have a fluid capability
    return stack != null && !stack.isEmpty() && FluidUtil.getFluidHandler(stack).flatMap(cap -> {
      // second, must contain enough fluid
      if (cap.getTanks() == 1) {
        FluidStack contained = cap.getFluidInTank(0);
        if (!contained.isEmpty() && fluidIngredient.getAmount(contained.getFluid()) == contained.getAmount() && fluidIngredient.test(contained.getFluid())) {
          // so far so good, from this point on we are forced to make copies as we need to try draining, so copy and fetch the copy's cap
          ItemStack copy = stack.copyWithCount(1);
          return FluidUtil.getFluidHandler(copy);
        }
      }
      return Optional.empty();
    }).filter(cap -> {
      // alright, we know it has the fluid, the question is just whether draining the fluid will give us the desired result
      Fluid fluid = cap.getFluidInTank(0).getFluid();
      int amount = fluidIngredient.getAmount(fluid);
      FluidStack drained = cap.drain(amount, FluidAction.EXECUTE);
      // we need an exact match, and we need the resulting container item to be the same as the item stack's container item
      return drained.getFluid() == fluid && drained.getAmount() == amount && ItemStack.matches(stack.getItem().getCraftingRemainder(stack) == null ? ItemStack.EMPTY : stack.getItem().getCraftingRemainder(stack).create(), cap.getContainer());
    }).isPresent();
  }

  @Override
  public Stream<Holder<Item>> items() {
    return display != null ? display.items() : Stream.empty();
  }

  public ItemStack[] getItems() {
    if (displayStacks == null) {
      if (display == null) {
        displayStacks = new ItemStack[0];
      } else {
        displayStacks = display.items().map(Holder::value).map(ItemStack::new).toArray(ItemStack[]::new);
      }
    }
    return displayStacks;
  }

  @Override
  public SlotDisplay display() {
    return display != null ? display.display() : SlotDisplay.Empty.INSTANCE;
  }

  public JsonElement toJson() {
    return TYPE.codec().codec().encodeStart(JsonOps.INSTANCE, this).getOrThrow();
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  /** Serializer logic */
  private static class Serializer {
    public static final MapCodec<FluidContainerIngredient> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
      instance.group(
        new LoadableCodec<>(FluidIngredient.LOADABLE).fieldOf("fluid").forGetter(i -> i.fluidIngredient),
        Ingredient.CODEC.optionalFieldOf("display").forGetter(i -> Optional.ofNullable(i.display))
      ).apply(instance, (fluid, display) -> new FluidContainerIngredient(fluid, display.orElse(null)))
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidContainerIngredient> STREAM_CODEC = StreamCodec.composite(
      StreamCodec.of(FluidIngredient.LOADABLE::encode, FluidIngredient.LOADABLE::decode), i -> i.fluidIngredient,
      Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, i -> Optional.ofNullable(i.display),
      (fluid, display) -> new FluidContainerIngredient(fluid, display.orElse(null))
    );
  }
}

