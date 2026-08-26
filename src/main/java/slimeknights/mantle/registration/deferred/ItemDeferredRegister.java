package slimeknights.mantle.registration.deferred;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.ItemObject;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Deferred register that registers items with wrappers
 */
@SuppressWarnings("unused")
public class ItemDeferredRegister extends DeferredRegisterWrapper<Item> {
  private static final ThreadLocal<ResourceKey<Item>> CURRENT_ITEM_KEY = new ThreadLocal<>();
  private static final Set<Item.Properties> SHARED_PROPERTIES = Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

  /** Marks an old shared Item.Properties instance that needs its ID refreshed for every item supplier. */
  public static Item.Properties registerSharedProperties(Item.Properties props) {
    SHARED_PROPERTIES.add(props);
    return props;
  }

  private static void setSharedIds(ResourceKey<Item> key) {
    synchronized (SHARED_PROPERTIES) {
      for (Item.Properties props : SHARED_PROPERTIES) {
        props.setId(key);
      }
    }
  }


  /** Applies the currently registering item ID to item properties created inside an item supplier. */
  public static Item.Properties setIdFromCurrentKey(Item.Properties props) {
    ResourceKey<Item> key = CURRENT_ITEM_KEY.get();
    return key == null ? props : props.setId(key);
  }

  /** Runs an item supplier while exposing the item key for property helpers. */
  public static <I extends Item> I withCurrentItemKey(ResourceKey<Item> key, Supplier<? extends I> supplier) {
    CURRENT_ITEM_KEY.set(key);
    setSharedIds(key);
    try {
      return supplier.get();
    } finally {
      CURRENT_ITEM_KEY.remove();
    }
  }
  public ItemDeferredRegister(String modID) {
    super(Registries.ITEM, modID);
  }

  /**
   * Adds a new item to the list to be registered, using the given supplier
   * @param name   Item name
   * @param sup    Supplier returning an item
   * @return  Item registry object
   */
  public <I extends Item> ItemObject<I> register(String name, Supplier<? extends I> sup) {
    ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, resource(name));
    return new ItemObject<>(register.register(name, () -> withCurrentItemKey(key, sup)));
  }

  /**
   * Adds a new item to the list to be registered, based on the given item properties
   * @param name   Item name
   * @param props  Item properties
   * @return  Item registry object
   */
  public ItemObject<Item> register(String name, Item.Properties props) {
    return register(name, () -> new Item(setIdFromCurrentKey(props)));
  }

  /**
   * Adds a new item to the list to be registered, with default item properties
   * @param name   Item name
   * @return  Item registry object
   */
  public ItemObject<Item> register(String name) {
    return register(name, new Item.Properties());
  }


  /* Specialty */

  /**
   * Registers an item with multiple variants, prefixing the name with the value name
   * @param values   Enum values to use for this item
   * @param name     Name of the block
   * @param mapper   Function to get a item for the given enum value
   * @return  EnumObject mapping between different item types
   */
  public <T extends Enum<T>, I extends Item> EnumObject<T,I> registerEnum(T[] values, String name, Function<T,? extends I> mapper) {
    return registerEnum(values, name, (fullName, type) -> register(fullName, () -> mapper.apply(type)));
  }

  /**
   * Registers an item with multiple variants, suffixing the name with the value name
   * @param values   Enum values to use for this item
   * @param name     Name of the block
   * @param mapper   Function to get a item for the given enum value
   * @return  EnumObject mapping between different item types
   */
  public <T extends Enum<T>, I extends Item> EnumObject<T,I> registerEnum(String name, T[] values, Function<T,? extends I> mapper) {
    return registerEnum(name, values, (fullName, type) -> register(fullName, () -> mapper.apply(type)));
  }
}
