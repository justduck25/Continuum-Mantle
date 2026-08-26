package slimeknights.mantle.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import slimeknights.mantle.Mantle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Command to dump vanilla tool material mining tags. */
public class HarvestTiersCommand {
  protected static final Identifier HARVEST_TIERS = Identifier.fromNamespaceAndPath("minecraft", "item_tier_ordering.json");
  private static final String HARVEST_TIER_PATH = HARVEST_TIERS.getNamespace() + "/" + HARVEST_TIERS.getPath();

  private static final Component SUCCESS_LOG = Component.translatable("command.mantle.harvest_tiers.success_log");
  private static final Component EMPTY = Component.translatable("command.mantle.tag.empty");

  private static final List<Entry> MATERIALS = List.of(
    new Entry("wood", ToolMaterial.WOOD),
    new Entry("stone", ToolMaterial.STONE),
    new Entry("copper", ToolMaterial.COPPER),
    new Entry("iron", ToolMaterial.IRON),
    new Entry("diamond", ToolMaterial.DIAMOND),
    new Entry("gold", ToolMaterial.GOLD),
    new Entry("netherite", ToolMaterial.NETHERITE)
  );

  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(sender -> MantleCommand.hasPermission(sender, MantleCommand.PERMISSION_EDIT_SPAWN))
              .then(Commands.literal("save").executes(source -> run(source, true)))
              .then(Commands.literal("log").executes(source -> run(source, false)))
              .then(Commands.literal("list").executes(HarvestTiersCommand::list));
  }

  private static Object getTagComponent(TagKey<Block> tag) {
    Identifier id = tag.location();
    return Component.literal(id.toString()).withStyle(style -> style.withUnderlined(true).withClickEvent(new ClickEvent.SuggestCommand("/mantle dump_tag " + Registries.BLOCK.identifier() + " " + id + " save")));
  }

  private static int list(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    MutableComponent output = Component.translatable("command.mantle.harvest_tiers.success_list");
    if (MATERIALS.isEmpty()) {
      output.append("\n* ").append(EMPTY);
    } else {
      for (Entry entry : MATERIALS) {
        output.append("\n* ").append(Component.translatable("command.mantle.harvest_tiers.tag", entry.name, getTagComponent(entry.material.incorrectBlocksForDrops())));
      }
    }
    context.getSource().sendSuccess(() -> output, true);
    return MATERIALS.size();
  }

  private static int run(CommandContext<CommandSourceStack> context, boolean saveFile) throws CommandSyntaxException {
    JsonArray entries = new JsonArray();
    for (Entry entry : MATERIALS) {
      entries.add(entry.name);
    }
    JsonObject json = new JsonObject();
    json.add("order", entries);

    if (saveFile) {
      File output = new File(DumpAllTagsCommand.getOutputFile(context), HARVEST_TIER_PATH);
      Path path = output.toPath();
      try {
        Files.createDirectories(path.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
          writer.write(DumpTagCommand.GSON.toJson(json));
        }
      } catch (IOException ex) {
        Mantle.logger.error("Couldn't save harvests tiers to {}", path, ex);
      }
      context.getSource().sendSuccess(() -> Component.translatable("command.mantle.harvest_tiers.success_save", GeneratePackHelper.getOutputComponent(output)), true);
    } else {
      context.getSource().sendSuccess(() -> SUCCESS_LOG, true);
      Mantle.logger.info("Dump of harvests tiers:\n{}", DumpTagCommand.GSON.toJson(json));
    }
    return MATERIALS.size();
  }

  private record Entry(String name, ToolMaterial material) {}
}