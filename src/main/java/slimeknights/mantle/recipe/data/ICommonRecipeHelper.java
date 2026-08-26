package slimeknights.mantle.recipe.data;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.HolderSet;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.MetalItemObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject;

/**
 * Crafting helper for common recipe types, like stairs, slabs, and packing.
 */
@SuppressWarnings("unused") // API
public interface ICommonRecipeHelper extends IRecipeHelper {
  /* Modern recipe helpers */

  default Criterion<TriggerInstance> hasItem(ItemLike item) {
    return TriggerInstance.hasItems(ItemPredicate.Builder.item().of(BuiltInRegistries.ITEM, item));
  }

  default Criterion<TriggerInstance> hasItem(TagKey<Item> tag) {
    return TriggerInstance.hasItems(new ItemPredicate(Optional.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, tag)), MinMaxBounds.Ints.ANY, DataComponentMatchers.ANY));
  }

  /* Metals */

  /**
   * Registers a recipe packing a small item into a large one
   * @param consumer   Recipe consumer
   * @param category   Recipe category
   * @param large      Large item
   * @param small      Small item
   * @param largeName  Large name
   * @param smallName  Small name
   * @param folder     Recipe folder
   */
  default void packingRecipe(RecipeOutput consumer, RecipeCategory category, String largeName, ItemLike large, String smallName, ItemLike small, String folder) {
    // ingot to block
    Identifier largeId = id(large);
    ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, category, large)
                       .define('#', small)
                       .pattern("###")
                       .pattern("###")
                       .pattern("###")
                       .unlockedBy("has_item", hasItem(small))
                       .group(largeId.toString())
                       .save(consumer, recipeKey(wrap(largeId, folder, String.format("_from_%ss", smallName))));
    // block to ingot
    Identifier smallId = id(small);
    ShapelessRecipeBuilder.shapeless(BuiltInRegistries.ITEM, category, small, 9)
                          .requires(large)
                          .unlockedBy("has_item", hasItem(large))
                          .group(smallId.toString())
                          .save(consumer, recipeKey(wrap(smallId, folder, String.format("_from_%s", largeName))));
  }

  /**
   * Registers a recipe packing a small item into a large one
   * @param consumer   Recipe consumer
   * @param largeItem  Large item
   * @param smallItem  Small item
   * @param smallTag   Tag for small item
   * @param largeName  Large name
   * @param smallName  Small name
   * @param folder     Recipe folder
   */
  default void packingRecipe(RecipeOutput consumer, RecipeCategory category, String largeName, ItemLike largeItem, String smallName, ItemLike smallItem, TagKey<Item> smallTag, String folder) {
    // ingot to block
    // note our item is in the center, any mod allowed around the edges
    Identifier largeId = id(largeItem);
    ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, category, largeItem)
                       .define('#', Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, smallTag)))
                       .define('*', smallItem)
                       .pattern("###")
                       .pattern("#*#")
                       .pattern("###")
                       .unlockedBy("has_item", hasItem(smallItem))
                       .group(largeId.toString())
                       .save(consumer, recipeKey(wrap(largeId, folder, String.format("_from_%ss", smallName))));
    // block to ingot
    Identifier smallId = id(smallItem);
    ShapelessRecipeBuilder.shapeless(BuiltInRegistries.ITEM, category, smallItem, 9)
                          .requires(largeItem)
                          .unlockedBy("has_item", hasItem(largeItem))
                          .group(smallId.toString())
                          .save(consumer, recipeKey(wrap(smallId, folder, String.format("_from_%s", largeName))));
  }

  /**
   * Adds recipes to convert a block to ingot, ingot to block, and for nuggets
   * @param consumer  Recipe consumer
   * @param metal     Metal object
   * @param folder    Folder for recipes
   */
  default void metalCrafting(RecipeOutput consumer, MetalItemObject metal, String folder) {
    ItemLike ingot = metal.getIngot();
    packingRecipe(consumer, RecipeCategory.MISC, "block", metal.get(), "ingot", ingot, metal.getIngotTag(), folder);
    packingRecipe(consumer, RecipeCategory.MISC, "ingot", ingot, "nugget", metal.getNugget(), metal.getNuggetTag(), folder);
  }


  /* Building blocks */

  /**
   * Registers generic saveing block recipes for slabs and stairs
   * @param consumer  Recipe consumer
   * @param building  Building object instance
   */
  default void slabStairsCrafting(RecipeOutput consumer, BuildingBlockObject building, String folder, boolean addStonecutter) {
    Item item = building.asItem();
    Identifier itemId = id(item);
    Criterion<TriggerInstance> hasBlock = hasItem(item);
    // slab
    ItemLike slab = building.getSlab();
    ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.BUILDING_BLOCKS, slab, 6)
                       .define('B', item)
                       .pattern("BBB")
                       .unlockedBy("has_item", hasBlock)
                       .group(id(slab).toString())
                       .save(consumer, recipeKey(wrap(itemId, folder, "_slab")));
    // stairs
    ItemLike stairs = building.getStairs();
    ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.BUILDING_BLOCKS, stairs, 4)
                       .define('B', item)
                       .pattern("B  ")
                       .pattern("BB ")
                       .pattern("BBB")
                       .unlockedBy("has_item", hasBlock)
                       .group(id(stairs).toString())
                       .save(consumer, recipeKey(wrap(itemId, folder, "_stairs")));

    // only add stonecutter if relevant
    if (addStonecutter) {
      Ingredient ingredient = Ingredient.of(item);
      SingleItemRecipeBuilder.stonecutting(ingredient, RecipeCategory.BUILDING_BLOCKS, slab, 2)
                             .unlockedBy("has_item", hasBlock)
                             .save(consumer, recipeKey(wrap(itemId, folder, "_slab_stonecutter")));
      SingleItemRecipeBuilder.stonecutting(ingredient, RecipeCategory.BUILDING_BLOCKS, stairs, 1)
                             .unlockedBy("has_item", hasBlock)
                             .save(consumer, recipeKey(wrap(itemId, folder, "_stairs_stonecutter")));
    }
  }

  /**
   * Registers generic saveing block recipes for slabs, stairs, and walls
   * @param consumer  Recipe consumer
   * @param building  Building object instance
   */
  default void stairSlabWallCrafting(RecipeOutput consumer, WallBuildingBlockObject building, String folder, boolean addStonecutter) {
    slabStairsCrafting(consumer, building, folder, addStonecutter);
    // wall
    Item item = building.asItem();
    Identifier itemId = id(item);
    Criterion<TriggerInstance> hasBlock = hasItem(item);
    ItemLike wall = building.getWall();
    ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.BUILDING_BLOCKS, wall, 6)
                       .define('B', item)
                       .pattern("BBB")
                       .pattern("BBB")
                       .unlockedBy("has_item", hasBlock)
                       .group(id(wall).toString())
                       .save(consumer, recipeKey(wrap(itemId, folder, "_wall")));
    // only add stonecutter if relevant
    if (addStonecutter) {
      Ingredient ingredient = Ingredient.of(item);
      SingleItemRecipeBuilder.stonecutting(ingredient, RecipeCategory.BUILDING_BLOCKS, wall, 1)
                             .unlockedBy("has_item", hasBlock)
                             .save(consumer, recipeKey(wrap(itemId, folder, "_wall_stonecutter")));
    }
  }

  /**
   * Registers recipes relevant to wood
   * @param consumer  Recipe consumer
   * @param wood      Wood types
   * @param folder    Wood folder
   */
  default void woodCrafting(RecipeOutput consumer, WoodBlockObject wood, String folder) {
    Criterion<TriggerInstance> hasPlanks = hasItem(wood);

    // planks
    ShapelessRecipeBuilder.shapeless(BuiltInRegistries.ITEM, RecipeCategory.BUILDING_BLOCKS, wood, 4).requires(Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, wood.getLogItemTag())))
                          .group("planks")
                          .unlockedBy("has_log", hasItem(wood.getLogItemTag()))
                          .save(consumer, recipeKey(location(folder + "planks")));
    // slab
    ItemLike slab = wood.getSlab();
    ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.BUILDING_BLOCKS, slab, 6)
                       .define('#', wood)
                       .pattern("###")
                       .unlockedBy("has_planks", hasPlanks)
                       .group("wooden_slab")
                       .save(consumer, recipeKey(location(folder + "slab")));
    // stairs
    ItemLike stairs = wood.getStairs();
    ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.BUILDING_BLOCKS, stairs, 4)
                       .define('#', wood)
                       .pattern("#  ")
                       .pattern("## ")
                       .pattern("###")
                       .unlockedBy("has_planks", hasPlanks)
                       .group("wooden_stairs")
                       .save(consumer, recipeKey(location(folder + "stairs")));

    // log to stripped
    ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.BUILDING_BLOCKS, wood.getWood(), 3)
                       .define('#', wood.getLog())
                       .pattern("##").pattern("##")
                       .group("bark")
                       .unlockedBy("has_log", hasItem(wood.getLog()))
                       .save(consumer, recipeKey(location(folder + "log_to_wood")));
    ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.BUILDING_BLOCKS, wood.getStrippedWood(), 3)
                       .define('#', wood.getStrippedLog())
                       .pattern("##").pattern("##")
                       .group("bark")
                       .unlockedBy("has_log", hasItem(wood.getStrippedLog()))
                       .save(consumer, recipeKey(location(folder + "stripped_log_to_wood")));
    // doors
    ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.DECORATIONS, wood.getFence(), 3)
                       .define('#', Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, Tags.Items.RODS_WOODEN))).define('W', wood)
                       .pattern("W#W").pattern("W#W")
                       .group("wooden_fence")
                       .unlockedBy("has_planks", hasPlanks)
                       .save(consumer, recipeKey(location(folder + "fence")));
    ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.REDSTONE, wood.getFenceGate())
                       .define('#', Items.STICK).define('W', wood)
                       .pattern("#W#").pattern("#W#")
                       .group("wooden_fence_gate")
                       .unlockedBy("has_planks", hasPlanks)
                       .save(consumer, recipeKey(location(folder + "fence_gate")));
    ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.REDSTONE, wood.getDoor(), 3)
                       .define('#', wood)
                       .pattern("##").pattern("##").pattern("##")
                       .group("wooden_door")
                       .unlockedBy("has_planks", hasPlanks)
                       .save(consumer, recipeKey(location(folder + "door")));
    ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.REDSTONE, wood.getTrapdoor(), 2)
                       .define('#', wood)
                       .pattern("###").pattern("###")
                       .group("wooden_trapdoor")
                       .unlockedBy("has_planks", hasPlanks)
                       .save(consumer, recipeKey(location(folder + "trapdoor")));
    // buttons
    ShapelessRecipeBuilder.shapeless(BuiltInRegistries.ITEM, RecipeCategory.REDSTONE, wood.getButton())
                          .requires(wood)
                          .group("wooden_button")
                          .unlockedBy("has_planks", hasPlanks)
                          .save(consumer, recipeKey(location(folder + "button")));
    ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.REDSTONE, wood.getPressurePlate())
                       .define('#', wood)
                       .pattern("##")
                       .group("wooden_pressure_plate")
                       .unlockedBy("has_planks", hasPlanks)
                       .save(consumer, recipeKey(location(folder + "pressure_plate")));
    // signs
    ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.DECORATIONS, wood.getSign(), 3)
                       .group("sign")
                       .define('#', wood).define('X', Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, Tags.Items.RODS_WOODEN)))
                       .pattern("###").pattern("###").pattern(" X ")
                       .unlockedBy("has_planks", hasPlanks)
                       .save(consumer, recipeKey(location(folder + "sign")));
    ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.DECORATIONS, wood.getHangingSign(), 6)
                       .group("hanging_sign")
                       .define('#', wood.getStrippedLog())
                       .define('X', Items.IRON_CHAIN)
                       .pattern("X X").pattern("###").pattern("###")
                       .unlockedBy("has_stripped_logs", hasItem(wood.getStrippedLog()))
                       .save(consumer, recipeKey(location(folder + "hanging_sign")));
  }
}