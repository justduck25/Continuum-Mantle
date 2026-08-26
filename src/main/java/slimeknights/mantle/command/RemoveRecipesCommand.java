package slimeknights.mantle.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NeverCondition;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.item.ItemPredicate;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static slimeknights.mantle.util.JsonHelper.DEFAULT_GSON;

/**
 * Command to disable recipes based on various presets or by ID.
 * @see RemoveDataCommand
 */
public class RemoveRecipesCommand {
  /** Translation key for successfully removing recipes */
  private static final String KEY_SUCCESS = Mantle.makeDescriptionId("command", "remove_recipes");

  /** Error on invalid item or tag ID */
  private static final DynamicCommandExceptionType ITEM_NOT_FOUND = new DynamicCommandExceptionType(id -> Mantle.makeComponent("command", "item.not_found", id));
  /** Error on invalid preset */
  private static final DynamicCommandExceptionType PRESET_NOT_FOUND = new DynamicCommandExceptionType(id -> Mantle.makeComponent("command", "remove_recipes.preset_not_found", id));

  /** Loadable for saving a list of recipe types */
  public static final Loadable<List<RecipeType<?>>> RECIPE_TYPES = Loadables.RECIPE_TYPE.list(ArrayLoadable.COMPACT);
  /** Folder containing all preset JSONs */
  public static FileToIdConverter PRESETS = FileToIdConverter.json("mantle/remove_recipes");

  /** Suggestion builder for recipe IDs */
  private static final SuggestionProvider<CommandSourceStack> SUGGESTS_RECIPES = (context, builder) ->
    SharedSuggestionProvider.suggestResource(context.getSource().getServer().getRecipeManager().getRecipes().stream().map(recipe -> recipe.id().identifier()), builder);
  /** Suggests presets for the command */
  private static final SuggestionProvider<CommandSourceStack> SUGGEST_PRESETS = SourcesCommand.suggestFolder(PRESETS);

  /**
   * Registers this sub command with the root command
   * @param subCommand Command builder
   * @param context    Context to fetch the recipe type argument
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand, CommandBuildContext context) {
    subCommand
      .then(Commands.literal("preset")
        .then(Commands.argument("preset", IdentifierArgument.id()).suggests(SUGGEST_PRESETS)
          .executes(RemoveRecipesCommand::runPreset)))
      .then(Commands.literal("result")
        .then(Commands.argument("recipe_type", ResourceArgument.resource(context, Registries.RECIPE_TYPE))
          .then(Commands.argument("result", ResourceOrTagKeyArgument.resourceOrTagKey(Registries.ITEM))
            .executes(RemoveRecipesCommand::runByResult)
            .then(Commands.argument("input", ResourceOrTagKeyArgument.resourceOrTagKey(Registries.ITEM))
              .executes(RemoveRecipesCommand::runResultInput)))))
      .then(Commands.literal("input")
        .then(Commands.argument("recipe_type", ResourceArgument.resource(context, Registries.RECIPE_TYPE))
          .then(Commands.argument("input", ResourceOrTagKeyArgument.resourceOrTagKey(Registries.ITEM))
            .executes(RemoveRecipesCommand::runByInput))))
      .then(Commands.literal("id")
        .then(Commands.argument("recipe", IdentifierArgument.id()).suggests(SUGGESTS_RECIPES)
          .executes(RemoveRecipesCommand::byId)));
  }

  /** Gets the item predicate */
  @SuppressWarnings("deprecation")
  private static Predicate<Item> getPredicate(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
    Either<ResourceKey<Item>, TagKey<Item>> items = ResourceOrTagKeyArgument.getResourceOrTagKey(context, name, Registries.ITEM, ITEM_NOT_FOUND).unwrap();
    return items.map(
      key -> item -> item.builtInRegistryHolder().is(key),
      tag -> item -> item.builtInRegistryHolder().is(tag));
  }

  /** Runs the command for provided arguments */
  private static int runByResult(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    long startTime = System.nanoTime();
    Holder<RecipeType<?>> recipeType = ResourceArgument.getResource(context, "recipe_type", Registries.RECIPE_TYPE);
    return run(context, List.of(recipeType.value()), getPredicate(context, "result"), null, startTime);
  }

  /** Runs the command for provided arguments */
  private static int runByInput(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    long startTime = System.nanoTime();
    Holder<RecipeType<?>> recipeType = ResourceArgument.getResource(context, "recipe_type", Registries.RECIPE_TYPE);
    return run(context, List.of(recipeType.value()), null, getPredicate(context, "input"), startTime);
  }

  /** Runs the command for provided arguments */
  private static int runResultInput(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    long startTime = System.nanoTime();
    Holder<RecipeType<?>> recipeType = ResourceArgument.getResource(context, "recipe_type", Registries.RECIPE_TYPE);
    return run(context, List.of(recipeType.value()), getPredicate(context, "result"), getPredicate(context, "input"), startTime);
  }

  /** Runs the command using a JSON preset */
  private static int runPreset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    long startTime = System.nanoTime();
    Identifier preset = IdentifierArgument.getId(context, "preset");

    Identifier presetLocation = PRESETS.idToFile(preset);
    ServerLevel level = context.getSource().getLevel();
    Optional<Resource> resource = level.getServer().getResourceManager().getResource(presetLocation);
    if (resource.isPresent()) {
      JsonObject json = JsonHelper.getJson(resource.get(), presetLocation);
      if (json != null) {
        try {
          IJsonPredicate<Item> remove = ItemPredicate.LOADER.getOrDefault(json, "result");
          IJsonPredicate<Item> input = ItemPredicate.LOADER.getOrDefault(json, "input");
          List<RecipeType<?>> recipeTypes = RECIPE_TYPES.getIfPresent(json, "recipe_type");

          return run(context, recipeTypes,
            remove == ItemPredicate.ANY ? null : remove::matches,
            input == ItemPredicate.ANY ? null : input::matches,
            startTime);
        } catch (RuntimeException e) {
          Mantle.logger.error("Failed to parse preset {} from {} in pack '{}'", preset, presetLocation, resource.get().sourcePackId(), e);
        }
      }
    } else {
      Mantle.logger.error("Failed to locate preset {} from {}", preset, presetLocation);
    }
    throw PRESET_NOT_FOUND.create(preset);
  }

  /** Runs the command */
  private static int run(CommandContext<CommandSourceStack> context, List<RecipeType<?>> recipeTypes, @Nullable Predicate<Item> removeResult, @Nullable Predicate<Item> removeInput, long startTime) {
    ServerLevel level = context.getSource().getLevel();
    RecipeManager manager = level.getServer().getRecipeManager();
    List<Identifier> recipes = new ArrayList<>();
    for (RecipeHolder<?> holder : manager.getRecipes()) {
      Recipe<?> recipe = holder.value();
      if (!recipeTypes.contains(recipe.getType())) {
        continue;
      }
      if (removeResult == null || matchesResult(recipe, removeResult, level)) {
        if (removeInput == null || matchesInput(recipe, removeInput)) {
          recipes.add(holder.id().identifier());
        }
      }
    }

    Path pack = GeneratePackHelper.getDatapackPath(level.getServer());
    GeneratePackHelper.saveMcmeta(pack);

    JsonObject json = new JsonObject();
    json.add("conditions", ICondition.LIST_CODEC.encodeStart(JsonOps.INSTANCE, List.of(NeverCondition.INSTANCE)).getOrThrow());
    String jsonString = DEFAULT_GSON.toJson(json);

    int successes = 0;
    Path data = pack.resolve(PackType.SERVER_DATA.getDirectory());
    for (Identifier id : recipes) {
      Path path = data.resolve(id.getNamespace() + "/recipes/" + id.getPath() + ".json");
      try {
        Files.createDirectories(path.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
          writer.write(jsonString);
          successes += 1;
        }
      } catch(IOException e) {
        Mantle.logger.error("Couldn't save recipe {}", id, e);
      }
    }

    int successFinal = successes;
    float time = (System.nanoTime() - startTime) / 1000000f;
    context.getSource().sendSuccess(() -> Component.translatable(KEY_SUCCESS, successFinal, time, GeneratePackHelper.getOutputComponent(pack)), true);
    return successes;
  }

  /** Checks if any display result for the recipe matches the requested item predicate. */
  private static boolean matchesResult(Recipe<?> recipe, Predicate<Item> removeResult, ServerLevel level) {
    for (var display : recipe.display()) {
      for (ItemStack stack : display.result().resolveForStacks(SlotDisplayContext.fromLevel(level))) {
        if (!stack.isEmpty() && removeResult.test(stack.getItem())) {
          return true;
        }
      }
    }
    return false;
  }

  /** Checks if any placeable ingredient for the recipe matches the requested item predicate. */
  private static boolean matchesInput(Recipe<?> recipe, Predicate<Item> removeInput) {
    for (Ingredient ingredient : recipe.placementInfo().ingredients()) {
      if (ingredient.items().anyMatch(holder -> removeInput.test(holder.value()))) {
        return true;
      }
    }
    return false;
  }

  /** Removes a recipe by ID */
  private static int byId(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    long startTime = System.nanoTime();
    Identifier id = IdentifierArgument.getId(context, "recipe");

    Path pack = GeneratePackHelper.getDatapackPath(context.getSource().getServer());
    GeneratePackHelper.saveMcmeta(pack);

    Path data = pack.resolve(PackType.SERVER_DATA.getDirectory());
    Path path = data.resolve(id.getNamespace() + "/recipes/" + id.getPath() + ".json");
    if (!GeneratePackHelper.saveConditionRemove(path, "conditions")) {
      throw GeneratePackHelper.FAILED_SAVE.create(id);
    }

    float time = (System.nanoTime() - startTime) / 1000000f;
    context.getSource().sendSuccess(() -> Component.translatable(KEY_SUCCESS, 1, time, GeneratePackHelper.getOutputComponent(pack)), true);
    return 1;
  }
}