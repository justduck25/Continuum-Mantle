package slimeknights.mantle.loot.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.FluidResourceHandlerItemAdapter;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import java.util.List;

/** Loot function to set the fluid on a dropped item. */
public class SetFluidLootFunction extends LootItemConditionalFunction {
  public static final MapCodec<SetFluidLootFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
    instance.group(
      commonFields(instance).t1(),
      BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(function -> function.fluid),
      ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(function -> function.amount)
    ).apply(instance, SetFluidLootFunction::new));

  /** Fluid and amount to add to the item. */
  private final Fluid fluid;
  private final int amount;

  protected SetFluidLootFunction(List<LootItemCondition> conditions, FluidStack fluid) {
    this(conditions, fluid.getFluid(), fluid.getAmount());
  }

  protected SetFluidLootFunction(List<LootItemCondition> conditions, Fluid fluid, int amount) {
    super(conditions);
    this.fluid = fluid;
    this.amount = amount;
  }

  @Override
  protected ItemStack run(ItemStack stack, LootContext context) {
    ItemAccess access = ItemAccess.forStack(stack).oneByOne();
    ResourceHandler<FluidResource> resourceHandler = access.getCapability(Capabilities.Fluid.ITEM);
    if (resourceHandler == null) {
      return stack;
    }
    FluidResourceHandlerItemAdapter handler = new FluidResourceHandlerItemAdapter(resourceHandler, access);
    FluidStack fluidStack = new FluidStack(fluid.builtInRegistryHolder(), amount);
    handler.fill(fluidStack, FluidAction.EXECUTE);
    return handler.getContainer();
  }

  @Override
  public MapCodec<SetFluidLootFunction> codec() {
    return MAP_CODEC;
  }

  /** Creates a new builder with the given fluid. */
  public static Builder<?> builder(FluidStack fluid) {
    return simpleBuilder(conditions -> new SetFluidLootFunction(conditions, fluid));
  }
}