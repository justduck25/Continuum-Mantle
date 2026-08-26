package slimeknights.mantle.registration.deferred;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

/** Register for command argument type serializers. */
@SuppressWarnings("UnusedReturnValue")
public class ArgumentTypeDeferredRegister extends DeferredRegisterWrapper<ArgumentTypeInfo<?,?>> {
  public ArgumentTypeDeferredRegister(String modID) {
    super(Registries.COMMAND_ARGUMENT_TYPE, modID);
  }

  /**
   * Registers an argument type
   * @param name           Name of the argument
   * @param argumentClass  Class of the argument
   * @param supplier       Supplier to the argument info
   * @param <A>  Argument type
   * @param <T>  Argument info template type
   * @param <I>  Argument info type
   * @return  Registry object
   */
  @SuppressWarnings("unchecked")
  public <A extends ArgumentType<?>,T extends ArgumentTypeInfo.Template<A>,I extends ArgumentTypeInfo<A,T>> DeferredHolder register(String name, Class<? super A> argumentClass, Supplier<I> supplier) {
    return register.register(name, () -> ArgumentTypeInfos.registerByClass((Class<A>) argumentClass, supplier.get()));
  }

  /**
   * Registers a context free singleton argument
   * @param name           Name of the argument
   * @param argumentClass  Class of the argument
   * @param supplier       Supplier to the argument default
   * @param <A>  Argument type
   * @return  Registry object
   */
  public <A extends ArgumentType<?>> DeferredHolder registerSingleton(String name, Class<A> argumentClass, Supplier<A> supplier) {
    return register(name, argumentClass, () -> SingletonArgumentInfo.contextFree(supplier));
  }
}