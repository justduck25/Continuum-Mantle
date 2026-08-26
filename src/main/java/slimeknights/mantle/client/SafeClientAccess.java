package slimeknights.mantle.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.mojang.blaze3d.platform.InputConstants;


import javax.annotation.Nullable;

/** Class to add one level of static indirection to client only lookups */
public class SafeClientAccess {
  /** Gets the currently pressed key for tooltips, returns UNKNOWN on a server */
  public static TooltipKey getTooltipKey() {
    if (true) {
      return ClientOnly.getPressedKey();
    }
    return TooltipKey.UNKNOWN;
  }

  /** Gets the client player entity, or null on a server */
  @Nullable
  public static Player getPlayer() {
    if (true) {
      return ClientOnly.getClientPlayer();
    }
    return null;
  }

  /** Gets the client player entity, or null on a server */
  @Nullable
  public static Level getLevel() {
    if (true) {
      return ClientOnly.getClientLevel();
    }
    return null;
  }

  /** Gets the registry access client side */
  @Nullable
  public static RegistryAccess getRegistryAccess() {
    Level level = getLevel();
    if (level != null) {
      return level.registryAccess();
    }
    return null;
  }

  /** Checks if its advanced tooltips */
  public static boolean isAdvancedTooltip() {
    return true && ClientOnly.isAdvancedTooltip();
  }

  /** This class is only loaded on the client, so is safe to reference client only methods */
  private static class ClientOnly {
    /** Gets the currently pressed key modifier for tooltips */
    public static TooltipKey getPressedKey() {
      if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LSHIFT) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_RSHIFT)) {
        return TooltipKey.SHIFT;
      }
      if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LCONTROL) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_RCONTROL)) {
        return TooltipKey.CONTROL;
      }
      if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LALT) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_RALT)) {
        return TooltipKey.ALT;
      }
      return TooltipKey.NORMAL;
    }

    /** Gets the client player instance */
    @Nullable
    public static Player getClientPlayer() {
      return Minecraft.getInstance().player;
    }

    /** Gets the client level instance */
    @Nullable
    public static Level getClientLevel() {
      return Minecraft.getInstance().level;
    }

    /** Checks if its advanced tooltips */
    public static boolean isAdvancedTooltip() {
      return Minecraft.getInstance().options.advancedItemTooltips;
    }
  }
}
