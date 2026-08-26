package slimeknights.mantle.registration.object;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

/** Object holding an entity and its egg */
public class EntityObject<T extends Entity> implements Supplier<EntityType<T>>, ItemLike, IdAwareObject {
  private final Identifier id;
  private final Supplier<? extends EntityType<T>> type;
  private final Supplier<? extends SpawnEggItem> spawnEgg;

  public EntityObject(DeferredHolder type, Supplier<? extends SpawnEggItem> spawnEgg) {
    this.id = type.getId();
    this.type = type;
    this.spawnEgg = spawnEgg;
  }

  @Override
  public EntityType<T> get() {
    return type.get();
  }

  @Override
  public Item asItem() {
    return spawnEgg.get();
  }

  @Override
  public Identifier getId() {
    return id;
  }
}