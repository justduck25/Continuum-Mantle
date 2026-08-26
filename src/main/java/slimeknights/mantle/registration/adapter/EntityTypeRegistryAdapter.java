package slimeknights.mantle.registration.adapter;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.BiConsumer;

/**
 * Registry adapter for registering entity types.
 */
@SuppressWarnings("unused")
public class EntityTypeRegistryAdapter extends RegistryAdapter<EntityType<?>> {
  public EntityTypeRegistryAdapter(BiConsumer<Identifier, EntityType<?>> register, String modId) {
    super(register, modId);
  }

  public EntityTypeRegistryAdapter(BiConsumer<Identifier, EntityType<?>> register) {
    super(register);
  }

  public <T extends Entity> EntityType<T> register(EntityType.Builder<T> builder, String name) {
    return register(builder.build(ResourceKey.create(Registries.ENTITY_TYPE, getResource(name))), name);
  }
}