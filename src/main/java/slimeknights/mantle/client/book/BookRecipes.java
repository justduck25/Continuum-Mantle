package slimeknights.mantle.client.book;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;

/**
 * File: BookRecipes.java.
 *
 * Recipes the server synced to this client, for book pages that show a recipe by ID
 * ({@code mantle:crafting} and {@code mantle:smelting}).
 *
 * Why: before Minecraft 26.1 every client had the full recipe manager, so books simply looked the recipe
 * up in the client level. Since 26.1 a client level only holds recipe displays; the server sends the
 * recipes of the types that some mod requested through {@code OnDatapackSyncEvent#sendRecipes}, and
 * NeoForge hands them to the client in {@link RecipesReceivedEvent}. Pages used to fall back to the
 * integrated server only, so in multiplayer they drew an empty grid. This keeps the received map for
 * the connection and drops it on logout. Mods whose books show recipes must request those recipe types
 * on the server (Continuum Construct requests the crafting type for its books).
 */
public final class BookRecipes {
  private BookRecipes() {}

  /** Recipes received for the current connection, empty when not connected */
  private static volatile RecipeMap recipes = RecipeMap.EMPTY;

  /** Registers the client listeners, called once during client setup */
  public static void init() {
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RecipesReceivedEvent.class, event -> recipes = event.getRecipeMap());
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, ClientPlayerNetworkEvent.LoggingOut.class, event -> recipes = RecipeMap.EMPTY);
  }

  /**
   * Gets a synced recipe by key.
   * @param key  Recipe key
   * @return  Recipe holder, or null if the server did not send it
   */
  @Nullable
  public static RecipeHolder<?> byKey(ResourceKey<Recipe<?>> key) {
    return recipes.byKey(key);
  }
}
