package slimeknights.mantle.registration.deferred;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.mantle.registration.object.EntityObject;

import java.util.function.Supplier;

/** Deferred register for an entity, building the type from a builder instance and adding an egg. */
@SuppressWarnings("unused")
public class EntityTypeDeferredRegister extends DeferredRegisterWrapper<EntityType<?>> {
  private final SynchronizedDeferredRegister<Item> itemRegistry;

  public EntityTypeDeferredRegister(String modID) {
    super(Registries.ENTITY_TYPE, modID);
    itemRegistry = SynchronizedDeferredRegister.create(Registries.ITEM, modID);
  }

  @Override
  public void register(IEventBus bus) {
    super.register(bus);
    itemRegistry.register(bus);
  }

  public <T extends Entity> DeferredHolder register(String name, Supplier<EntityType.Builder<T>> sup) {
    return register.register(name, () -> sup.get().build(ResourceKey.create(Registries.ENTITY_TYPE, resource(name))));
  }

  public <T extends Mob> EntityObject<T> registerWithEgg(String name, Supplier<EntityType.Builder<T>> sup, int primary, int secondary) {
    DeferredHolder object = register(name, sup);
    ResourceKey<Item> eggKey = ResourceKey.create(Registries.ITEM, resource(name + "_spawn_egg"));
    return new EntityObject<>(object, itemRegistry.register(name + "_spawn_egg", () -> ItemDeferredRegister.withCurrentItemKey(eggKey, () -> new SpawnEggItem(ItemDeferredRegister.setIdFromCurrentKey(new Item.Properties().spawnEgg(((EntityType<T>) object.get())))))));
  }
}