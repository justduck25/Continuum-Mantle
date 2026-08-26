package slimeknights.mantle.registration.deferred;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Deferred register for menu types, automatically mapping a factory argument in {@link IMenuTypeExtension}
 */
@SuppressWarnings("unused")
public class MenuTypeDeferredRegister extends DeferredRegisterWrapper<MenuType<?>> {
  public MenuTypeDeferredRegister(String modID) {
    super(net.minecraft.core.registries.Registries.MENU, modID);
  }

  public <C extends AbstractContainerMenu> DeferredHolder register(String name, IContainerFactory<C> factory) {
    return register.register(name, () -> IMenuTypeExtension.create(factory));
  }
}