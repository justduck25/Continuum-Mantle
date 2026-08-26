package slimeknights.mantle.recipe.data;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.mantle.registration.object.IdAwareObject;

import java.util.Objects;

/**
 * Interface for common resource location and condition methods
 */
@SuppressWarnings("unused")
public interface IRecipeHelper {
  /* Location helpers */

  /** Gets the ID of the mod adding recipes */
  String getModId();

  /**
   * Gets a resource location for the mod
   * @param name  Location path
   * @return  Location for the mod
   */
  default Identifier location(String name) {
    return Identifier.fromNamespaceAndPath(getModId(), name);
  }

  /**
   * Gets a resource location string for your mod
   * @param id  Location path
   * @return  Location for your mod as a string
   */
  default String prefix(String id) {
    return getModId() + ":" + id;
  }

  /**
   * Gets a registry ID for the given item
   * @param item  Item to fetch ID
   * @return  ID for the item put in your namespace
   */
  @SuppressWarnings("deprecation")  // won't be for long
  default Identifier id(ItemLike item) {
    return id(BuiltInRegistries.ITEM, item.asItem());
  }

  /**
   * Gets a registry ID for the given item
   * @param registry  Registry to fetch IDs
   * @param value     Registry value
   * @return  ID for the item put in your namespace
   */
  default <T> Identifier id(Registry<T> registry, T value) {
    return location(Objects.requireNonNull(registry.getKey(value)).getPath());
  }


  /** Converts a recipe identifier to the ResourceKey used by modern recipe datagen. */
  default ResourceKey<Recipe<?>> recipeKey(Identifier id) {
    return ResourceKey.create(Registries.RECIPE, id);
  }


  /* Location extending with namespace */

  /** Wraps the given path under our ID */
  default Identifier wrap(Identifier location, String prefix, String suffix) {
    return location(prefix + location.getPath() + suffix);
  }

  /** Prefixes the given path under our ID */
  default Identifier prefix(Identifier location, String prefix) {
    return location(prefix + location.getPath());
  }

  /** Suffixes the given path under our ID */
  default Identifier suffix(Identifier location, String suffix) {
    return location(location.getPath() + suffix);
  }


  /* Registry object location helpers */

  /**
   * Wraps the registry object ID in the given prefix and suffix
   * @param location  Object to use for location
   * @param prefix    Path prefix
   * @param suffix    Path suffix
   * @return  Location with the given prefix and suffix
   */
  default Identifier wrap(DeferredHolder<?, ?> location, String prefix, String suffix) {
    return wrap(location.getId(), prefix, suffix);
  }

  default Identifier wrap(Holder<?> location, String prefix, String suffix) {
    return wrap(location.unwrapKey().map(ResourceKey::identifier).orElseThrow(), prefix, suffix);
  }

  /**
   * Prefixes the registry object ID
   * @param location  Object to use for location
   * @param prefix    Path prefix
   * @return  Location with the given prefix
   */
  default Identifier prefix(DeferredHolder<?, ?> location, String prefix) {
    return prefix(location.getId(), prefix);
  }

  default Identifier prefix(Holder<?> location, String prefix) {
    return prefix(location.unwrapKey().map(ResourceKey::identifier).orElseThrow(), prefix);
  }

  /**
   * Suffixes the registry object ID
   * @param location  Object to use for location
   * @param suffix    Path suffix
   * @return  Location with the given suffix
   */
  default Identifier suffix(DeferredHolder<?, ?> location, String suffix) {
    return suffix(location.getId(), suffix);
  }

  default Identifier suffix(Holder<?> location, String suffix) {
    return suffix(location.unwrapKey().map(ResourceKey::identifier).orElseThrow(), suffix);
  }


  /* Other named object location helpers */

  /**
   * Wraps the registry object ID in the given prefix and suffix
   * @param location  Object to use for location
   * @param prefix    Path prefix
   * @param suffix    Path suffix
   * @return  Location with the given prefix and suffix
   */
  default Identifier wrap(IdAwareObject location, String prefix, String suffix) {
    return wrap(location.getId(), prefix, suffix);
  }

  /**
   * Prefixes the registry object ID
   * @param location  Object to use for location
   * @param prefix    Path prefix
   * @return  Location with the given prefix
   */
  default Identifier prefix(IdAwareObject location, String prefix) {
    return prefix(location.getId(), prefix);
  }

  /**
   * Suffixes the registry object ID
   * @param location  Object to use for location
   * @param suffix    Path suffix
   * @return  Location with the given suffix
   */
  default Identifier suffix(IdAwareObject location, String suffix) {
    return suffix(location.getId(), suffix);
  }


  /* Tags and conditions */

  /**
   * Gets a tag by name
   * @param modId  Mod ID for tag
   * @param name   Tag name
   * @return  Tag instance
   */
  default TagKey<Item> getItemTag(String modId, String name) {
    return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(modId, name));
  }

  /**
   * Gets a tag by name
   * @param modId  Mod ID for tag
   * @param name   Tag name
   * @return  Tag instance
   */
  default TagKey<Fluid> getFluidTag(String modId, String name) {
    return TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath(modId, name));
  }

  /**
   * Creates a condition for a tag existing
   * @param name  Forge tag name
   * @return  Condition for tag existing
   */
  default ICondition tagCondition(String name) {
    return new TagFilledCondition<>(ItemTags.create(Mantle.commonResource(name)));
  }

  /**
   * Creates a consumer instance with the added conditions
   * @param consumer    Base consumer
   * @param conditions  Extra conditions
   * @return  Wrapped consumer
   */
  default RecipeOutput withCondition(RecipeOutput consumer, ICondition... conditions) {
    return consumer.withConditions(conditions);
  }
}

