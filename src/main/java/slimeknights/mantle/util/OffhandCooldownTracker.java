package slimeknights.mantle.util;

import lombok.RequiredArgsConstructor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

/** Logic to handle offhand having its own cooldown. */
@RequiredArgsConstructor
public class OffhandCooldownTracker {
  /** @deprecated use {@link #get(Player)} */
  @Deprecated(forRemoval = true)
  public static final Function<OffhandCooldownTracker,Float> COOLDOWN_TRACKER = OffhandCooldownTracker::getCooldown;
  private static final Map<Player,OffhandCooldownTracker> TRACKERS = new WeakHashMap<>();

  /** Registers event listeners. Player trackers are now stored directly instead of using legacy capabilities. */
  public static void init() {}

  /** Kept for binary/source compatibility with older initialization code. */
  public static void register(RegisterCapabilitiesEvent event) {}

  /** Player receiving cooldowns. */
  @Nullable
  private final Player player;
  /** Scale of the last cooldown. */
  private int lastCooldown = 0;
  /** Time in ticks when the player can next attack for full power. */
  private int attackReady = 0;
  /** Legacy opt-in counter retained for compatibility. */
  private int enabled = 0;

  /** Null safe way to get the player's ticks existed. */
  private int getTicksExisted() {
    return player == null ? 0 : player.tickCount;
  }

  /** If true, the tracker is enabled despite a cooldown item not being held. */
  @Deprecated(forRemoval = true)
  public boolean isEnabled() {
    return enabled > 0;
  }

  /**
   * Call this method when your item causing offhand cooldown to be needed is enabled and disabled.
   * @deprecated No longer required, so callers can remove this.
   */
  @Deprecated(forRemoval = true)
  public void setEnabled(boolean enable) {
    if (enable) {
      enabled++;
    } else {
      enabled--;
    }
  }

  /** Applies the given amount of cooldown. */
  public void applyCooldown(int cooldown) {
    this.lastCooldown = cooldown;
    this.attackReady = getTicksExisted() + cooldown;
  }

  /** Returns a number from 0 to 1 denoting the current cooldown amount. */
  public float getCooldown() {
    int ticksExisted = getTicksExisted();
    if (ticksExisted > this.attackReady || this.lastCooldown == 0) {
      return 1.0f;
    }
    return Mth.clamp((this.lastCooldown + ticksExisted - this.attackReady) / (float)this.lastCooldown, 0f, 1f);
  }

  /** Checks if we can perform another attack yet. */
  public boolean isAttackReady() {
    return getTicksExisted() + this.lastCooldown > this.attackReady;
  }

  /** Gets the tracker instance for the target player. */
  public static OffhandCooldownTracker get(Player player) {
    return TRACKERS.computeIfAbsent(player, OffhandCooldownTracker::new);
  }

  /** Gets the offhand cooldown for the given player. */
  public static float getCooldown(Player player) {
    return get(player).getCooldown();
  }

  /** Applies cooldown to the given player. */
  public static void applyCooldown(Player player, int cooldown) {
    get(player).applyCooldown(cooldown);
  }

  /** Applies cooldown to the given player. */
  public static boolean isAttackReady(Player player) {
    return get(player).isAttackReady();
  }

  /** Applies cooldown using attack speed. */
  public static void applyCooldown(Player player, float attackSpeed, int cooldownTime) {
    applyCooldown(player, Math.round(cooldownTime / attackSpeed));
  }

  /** Swings the entity hand without manually resetting Mantle's offhand cooldown. */
  public static void swingHand(LivingEntity entity, InteractionHand hand, boolean updateSelf) {
    entity.swing(hand, updateSelf);
  }
}