package slimeknights.mantle;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/** Handles events for any Mantle driven logic. */
@EventBusSubscriber(modid = Mantle.modId)
public class MantleEvents {
  public static final String SOULBOUND_SLOT = "mantle_soulbound";

  private static boolean keepInventory(LivingEntity entity) {
    return entity.level() instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(GameRules.KEEP_INVENTORY);
  }

  private static void setSoulboundSlot(ItemStack stack, int slot) {
    CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(SOULBOUND_SLOT, slot));
  }

  private static int getSoulboundSlot(ItemStack stack) {
    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
    if (customData == null) {
      return -1;
    }
    return customData.copyTag().getIntOr(SOULBOUND_SLOT, -1);
  }

  private static void clearSoulboundSlot(ItemStack stack) {
    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
    if (customData == null) {
      return;
    }
    CompoundTag tag = customData.copyTag();
    tag.remove(SOULBOUND_SLOT);
    if (tag.isEmpty()) {
      stack.remove(DataComponents.CUSTOM_DATA);
    } else {
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
  }

  @SubscribeEvent
  static void onLivingDeath(LivingDeathEvent event) {
    LivingEntity entity = event.getEntity();
    if (!keepInventory(entity) && entity instanceof Player player && !(player instanceof FakePlayer)) {
      Inventory inventory = player.getInventory();
      for (int i = 0, totalSize = inventory.getContainerSize(); i < totalSize; i++) {
        ItemStack stack = inventory.getItem(i);
        /* if (!stack.isEmpty() && stack.is(MantleTags.Items.SOULBOUND)) {
          setSoulboundSlot(stack, i);
        } */
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGH)
  static void onPlayerDropItems(LivingDropsEvent event) {
    LivingEntity entity = event.getEntity();
    if (!keepInventory(entity) && entity instanceof Player player && !(player instanceof FakePlayer)) {
      Collection<ItemEntity> drops = event.getDrops();
      Iterator<ItemEntity> iter = drops.iterator();
      Inventory inventory = player.getInventory();
      List<ItemEntity> takenSlot = new ArrayList<>();
      while (iter.hasNext()) {
        ItemEntity itemEntity = iter.next();
        ItemStack stack = itemEntity.getItem();
        int slot = getSoulboundSlot(stack);
        if (slot >= 0) {
          if (slot < inventory.getContainerSize() && inventory.getItem(slot).isEmpty()) {
            inventory.setItem(slot, stack);
          } else {
            takenSlot.add(itemEntity);
          }
          iter.remove();
        }
      }
      for (ItemEntity itemEntity : takenSlot) {
        ItemStack stack = itemEntity.getItem();
        if (!inventory.add(stack)) {
          clearSoulboundSlot(stack);
          drops.add(itemEntity);
        }
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGH)
  static void onPlayerClone(PlayerEvent.Clone event) {
    if (!event.isWasDeath()) {
      return;
    }
    Player original = event.getOriginal();
    Player clone = event.getEntity();
    if (keepInventory(clone) || original.isSpectator()) {
      return;
    }
    Inventory originalInv = original.getInventory();
    Inventory cloneInv = clone.getInventory();
    int size = Math.min(originalInv.getContainerSize(), cloneInv.getContainerSize());
    List<ItemStack> takenSlot = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      ItemStack stack = originalInv.getItem(i);
      if (!stack.isEmpty() && getSoulboundSlot(stack) >= 0) {
        if (cloneInv.getItem(i).isEmpty()) {
          cloneInv.setItem(i, stack);
        } else {
          takenSlot.add(stack);
        }
        clearSoulboundSlot(stack);
      }
    }
    for (ItemStack stack : takenSlot) {
      if (!cloneInv.add(stack)) {
        clone.drop(stack, false);
      }
    }
  }
}