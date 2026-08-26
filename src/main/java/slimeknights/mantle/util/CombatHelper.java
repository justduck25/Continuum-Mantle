package slimeknights.mantle.util;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Helpers for attacking with weapons. */
public class CombatHelper {
  private static final float TO_RADIAN = (float)Math.PI / 180f;
  private static final AttributeModifier ANTI_KNOCKBACK_MODIFIER = new AttributeModifier(Mantle.getResource("anti_knockback"), 1f, Operation.ADD_VALUE);
  public static final ItemAbility NO_BASE_KNOCKBACK = ItemAbility.get("no_base_knockback");

  private CombatHelper() {}

  /** Gets the item stack in the main hand that contributes to attributes. */
  public static ItemStack getMainhandAttributeStack(LivingEntity entity) {
    return entity.getMainHandItem();
  }

  /** Gets a modifiable map that is a copy of the modifiers from the given attribute instance. */
  public static Map<Operation, Set<AttributeModifier>> copyModifiers(AttributeInstance instance) {
    Map<Operation, Set<AttributeModifier>> modifiers = new EnumMap<>(Operation.class);
    for (Operation operation : Operation.values()) {
      Set<AttributeModifier> copy = new HashSet<>();
      for (AttributeModifier modifier : instance.getModifiers()) {
        if (modifier.operation() == operation) {
          copy.add(modifier);
        }
      }
      modifiers.put(operation, copy);
    }
    return modifiers;
  }

  /** Gets the attribute for the offhand by subtracting mainhand attributes and adding in offhand stack attributes. */
  public static float getOffhandAttribute(ItemStack stack, LivingEntity entity, Holder<Attribute> attribute) {
    AttributeInstance instance = entity.getAttribute(attribute);
    double base = instance == null ? entity.getAttributeBaseValue(attribute) : instance.getBaseValue();
    return (float)stack.getAttributeModifiers().compute(attribute, base, EquipmentSlot.MAINHAND);
  }

  /** Computes the value for the given attribute. */
  public static double computeAttribute(Holder<Attribute> attribute, double base, Map<Operation,Set<AttributeModifier>> modifiers) {
    for (AttributeModifier modifier : modifiers.getOrDefault(Operation.ADD_VALUE, Set.of())) {
      base += modifier.amount();
    }
    double value = base;
    for (AttributeModifier modifier : modifiers.getOrDefault(Operation.ADD_MULTIPLIED_BASE, Set.of())) {
      value += base * modifier.amount();
    }
    for (AttributeModifier modifier : modifiers.getOrDefault(Operation.ADD_MULTIPLIED_TOTAL, Set.of())) {
      value *= 1.0 + modifier.amount();
    }
    return attribute.value().sanitizeValue(value);
  }

  /** Checks if the given entity can be attacked. */
  public static boolean isAttackable(Entity attacker, Entity target) {
    return target.isAttackable() && !target.skipAttackInteraction(attacker);
  }

  public static boolean attack(ItemStack stack, Player player, Entity target, @Nullable LivingEntity targetLiving, InteractionHand hand) {
    return attack(stack, player, target, targetLiving, hand, player.damageSources().playerAttack(player));
  }

  /** Performs an attack, mimicking modern vanilla attack behavior where possible. */
  public static boolean attack(ItemStack stack, Player player, Entity target, @Nullable LivingEntity targetLiving, InteractionHand hand, DamageSource damageSource) {
    if (!isAttackable(player, target)) {
      return false;
    }

    float damage = hand == InteractionHand.OFF_HAND ? getOffhandAttribute(stack, player, Attributes.ATTACK_DAMAGE) : (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
    float cooldown = hand == InteractionHand.OFF_HAND ? OffhandCooldownTracker.getCooldown(player) : player.getAttackStrengthScale(0.5F);
    damage *= 0.2F + cooldown * cooldown * 0.8F;
    boolean fullyCharged = cooldown > 0.9F;

    float knockback = hand == InteractionHand.OFF_HAND ? getOffhandAttribute(stack, player, Attributes.ATTACK_KNOCKBACK) : (float)player.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
    boolean sprinting = false;
    if (player.isSprinting() && fullyCharged) {
      player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, player.getSoundSource(), 1.0F, 1.0F);
      knockback += 1;
      sprinting = true;
    }

    boolean critical = fullyCharged && player.fallDistance > 0.0F && !player.onGround() && !player.onClimbable() && !player.isSprinting() && !player.isInWater() && !player.hasEffect(MobEffects.BLINDNESS) && !player.isPassenger() && targetLiving != null;
    CriticalHitEvent hitResult = CommonHooks.fireCriticalHit(player, target, critical, critical ? 1.5f : 1f);
    critical = hitResult.isCriticalHit();
    if (critical) {
      damage *= hitResult.getDamageMultiplier();
    }

    if (player.level() instanceof ServerLevel serverLevel) {
      damage = EnchantmentHelper.modifyDamage(serverLevel, stack, target, damageSource, damage);
      knockback = EnchantmentHelper.modifyKnockback(serverLevel, stack, target, damageSource, knockback);
    }

    boolean canSweep = fullyCharged && !critical && !sprinting && player.onGround() && stack.canPerformAction(ItemAbilities.SWORD_SWEEP);
    float health = targetLiving == null ? 0 : targetLiving.getHealth();
    Vec3 movement = target.getDeltaMovement();

    boolean hit;
    if (stack.canPerformAction(NO_BASE_KNOCKBACK) && targetLiving != null) {
      AttributeInstance knockbackAttribute = targetLiving.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
      if (knockbackAttribute != null && !knockbackAttribute.hasModifier(ANTI_KNOCKBACK_MODIFIER.id())) {
        knockbackAttribute.addTransientModifier(ANTI_KNOCKBACK_MODIFIER);
        hit = target.hurtOrSimulate(damageSource, damage);
        knockbackAttribute.removeModifier(ANTI_KNOCKBACK_MODIFIER);
      } else {
        hit = target.hurtOrSimulate(damageSource, damage);
      }
    } else {
      hit = target.hurtOrSimulate(damageSource, damage);
    }

    if (hit) {
      if (knockback > 0) {
        if (targetLiving != null) {
          targetLiving.knockback(knockback * 0.5f, Mth.sin(player.getYRot() * TO_RADIAN), -Mth.cos(player.getYRot() * TO_RADIAN));
        } else {
          target.push(-Mth.sin(player.getYRot() * TO_RADIAN) * knockback * 0.5F, 0.1D, Mth.cos(player.getYRot() * TO_RADIAN) * knockback * 0.5f);
        }
        player.setDeltaMovement(player.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
        player.setSprinting(false);
      }

      if (canSweep) {
        float sweepDamage = 1 + (float)player.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) * damage;
        for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, stack.getSweepHitBox(player, target))) {
          double entityReachSq = Mth.square(player.entityInteractionRange());
          if (living != player && living != targetLiving && !player.isAlliedTo(living) && (!(living instanceof ArmorStand armorStand) || !armorStand.isMarker()) && player.distanceToSqr(living) < entityReachSq) {
            living.knockback(0.4f, Mth.sin(player.getYRot() * TO_RADIAN), -Mth.cos(player.getYRot() * TO_RADIAN));
            living.hurt(player.damageSources().playerAttack(player), sweepDamage);
          }
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
      }

      if (target instanceof ServerPlayer serverTarget && target.hurtMarked) {
        serverTarget.connection.send(new ClientboundSetEntityMotionPacket(target));
        target.hurtMarked = false;
        target.setDeltaMovement(movement);
      }

      if (critical) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, player.getSoundSource(), 1.0F, 1.0F);
        player.crit(target);
      } else if (fullyCharged) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, player.getSoundSource(), 1.0F, 1.0F);
      } else {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, player.getSoundSource(), 1.0F, 1.0F);
      }

      player.setLastHurtMob(target);
      if (player.level() instanceof ServerLevel serverLevel) {
        EnchantmentHelper.doPostAttackEffects(serverLevel, target, damageSource);
      }

      Entity parent = target instanceof PartEntity<?> part ? part.getParent() : target;
      if (!player.level().isClientSide() && !stack.isEmpty() && parent instanceof LivingEntity living) {
        boolean itemHurtEnemy = stack.hurtEnemy(living, player);
        if (itemHurtEnemy) {
          stack.postHurtEnemy(living, player);
        }
        if (stack.isEmpty()) {
          player.setItemInHand(hand, ItemStack.EMPTY);
        }
      }

      if (targetLiving != null) {
        float damageDealt = health - targetLiving.getHealth();
        player.awardStat(Stats.DAMAGE_DEALT, Math.round(damageDealt * 10f));
        if (player.level() instanceof ServerLevel server && damageDealt > 2f) {
          server.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5D), target.getZ(), (int)((double)damageDealt * 0.5D), 0.1D, 0.0D, 0.1D, 0.2D);
        }
      }
      player.causeFoodExhaustion(0.1F);
    } else {
      player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, player.getSoundSource(), 1.0F, 1.0F);
    }

    if (hand == InteractionHand.OFF_HAND) {
      OffhandCooldownTracker.applyCooldown(player, getOffhandAttribute(stack, player, Attributes.ATTACK_SPEED), 20);
    } else {
      player.resetAttackStrengthTicker();
    }
    return true;
  }

  public static Holder<DamageType> damageType(RegistryAccess access, ResourceKey<DamageType> key) {
    return access.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key);
  }

  public static DamageSource damageSource(RegistryAccess access, ResourceKey<DamageType> key) {
    return new DamageSource(damageType(access, key));
  }

  public static DamageSource damageSource(Level level, ResourceKey<DamageType> key) {
    return new DamageSource(damageType(level.registryAccess(), key));
  }

  public static DamageSource damageSource(ResourceKey<DamageType> key, Entity entity) {
    return new DamageSource(damageType(entity.level().registryAccess(), key), entity);
  }

  public static DamageSource damageSource(ResourceKey<DamageType> key, Entity direct, @Nullable Entity causing) {
    return new DamageSource(damageType(direct.level().registryAccess(), key), direct, causing);
  }
}