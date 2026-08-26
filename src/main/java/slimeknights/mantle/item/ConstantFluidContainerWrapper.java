package slimeknights.mantle.item;

import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConstantFluidContainerWrapper implements IFluidHandlerItem, ICapabilityProvider<Object, Object, IFluidHandlerItem> {
  private final FluidStack fluid;
  private boolean empty = false;
  @Getter
  @Nonnull
  protected ItemStack container;
  private final ItemStack emptyStack;

  public ConstantFluidContainerWrapper(FluidStack fluid, ItemStack container, ItemStack emptyStack) {
    this.fluid = fluid;
    this.container = container;
    this.emptyStack = emptyStack;
  }

  public ConstantFluidContainerWrapper(FluidStack fluid, ItemStack container) {
    this(fluid, container, container.getItem().getCraftingRemainder().create());
  }

  @Override
  public IFluidHandlerItem getCapability(Object capability, Object context) { return this; }

  public int getTanks() { return 1; }
  public int getTankCapacity(int tank) { return fluid.getAmount(); }
  public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return stack.isEmpty() || stack.getFluid() == fluid.getFluid(); }
  @Nonnull public FluidStack getFluidInTank(int tank) { return empty ? FluidStack.EMPTY : fluid; }
  public int fill(FluidStack resource, FluidAction action) { return 0; }
  @Nonnull public FluidStack drain(FluidStack resource, FluidAction action) {
    if (empty || resource.getFluid() != fluid.getFluid() || resource.getAmount() < fluid.getAmount()) { return FluidStack.EMPTY; }
    if (action == FluidAction.EXECUTE) { container = emptyStack; empty = true; }
    return fluid.copy();
  }
  @Nonnull public FluidStack drain(int maxDrain, FluidAction action) {
    if (empty || maxDrain < fluid.getAmount()) { return FluidStack.EMPTY; }
    if (action == FluidAction.EXECUTE) { container = emptyStack; empty = true; }
    return fluid.copy();
  }
}