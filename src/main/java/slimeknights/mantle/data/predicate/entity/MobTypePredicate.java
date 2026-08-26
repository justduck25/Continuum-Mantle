package slimeknights.mantle.data.predicate.entity;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/** Predicate matching an entity type tag that replaces legacy MobType groups. */
public record MobTypePredicate(TagKey<EntityType<?>> type) implements LivingEntityPredicate {
  /** Loader for a legacy mob type predicate, backed by entity type tags in modern Minecraft. */
  public static RecordLoadable<MobTypePredicate> LOADER = RecordLoadable.create(Loadables.ENTITY_TYPE_TAG.requiredField("mobs", MobTypePredicate::type), MobTypePredicate::new);

  @Override
  public boolean matches(LivingEntity input) {
    return input.typeHolder().is(type);
  }

  @Override
  public RecordLoadable<? extends LivingEntityPredicate> getLoader() {
    return LOADER;
  }
}