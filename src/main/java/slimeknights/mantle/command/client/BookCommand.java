package slimeknights.mantle.command.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.command.MantleCommand;

/** A command for different book operations. */
public class BookCommand {
  private static final String BOOK_NOT_FOUND = "command.mantle.book_test.not_found";
  private static final String EXPORT_UNAVAILABLE = "Mantle book export is not yet ported to Minecraft 26.1 render pipelines";
  private static final int DEFAULT_SCALE = 2;
  private static final String DEFAULT_BOOK_VERSION = "20";

  /**
   * Registers this sub command with the root command.
   * @param subCommand Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(source -> MantleCommand.hasPermission(source, MantleCommand.PERMISSION_GAME_COMMANDS) && source.getEntity() instanceof AbstractClientPlayer)
      .then(Commands.literal("open")
        .then(Commands.argument("id", IdentifierArgument.id()).suggests(MantleClientCommand.REGISTERED_BOOKS)
          .executes(BookCommand::openBook)))
      .then(Commands.literal("export_images")
        .then(Commands.argument("domain", StringArgumentType.word()).suggests(MantleClientCommand.REGISTERED_BOOK_DOMAINS)
          .then(Commands.argument("scale", IntegerArgumentType.integer(1, 16)).executes(BookCommand::exportUnavailable))
          .executes(context -> exportUnavailable(context, DEFAULT_SCALE)))
        .then(Commands.argument("id", IdentifierArgument.id()).suggests(MantleClientCommand.REGISTERED_BOOKS)
          .then(Commands.argument("scale", IntegerArgumentType.integer(1, 16)).executes(BookCommand::exportUnavailable))
          .executes(context -> exportUnavailable(context, DEFAULT_SCALE))))
      .then(Commands.literal("export_html")
        .then(Commands.argument("domain", StringArgumentType.word()).suggests(MantleClientCommand.REGISTERED_BOOK_DOMAINS)
          .then(Commands.argument("version", StringArgumentType.word()).executes(BookCommand::exportUnavailable))
          .executes(context -> exportUnavailable(context, DEFAULT_BOOK_VERSION)))
        .then(Commands.argument("id", IdentifierArgument.id()).suggests(MantleClientCommand.REGISTERED_BOOKS)
          .then(Commands.argument("version", StringArgumentType.word()).executes(BookCommand::exportUnavailable))
          .executes(context -> exportUnavailable(context, DEFAULT_BOOK_VERSION))));
  }

  private static int openBook(CommandContext<CommandSourceStack> context) {
    Identifier book = IdentifierArgument.getId(context, "id");
    BookData bookData = BookLoader.getBook(book);
    if (bookData != null) {
      Minecraft.getInstance().execute(() -> bookData.openGui(Component.literal("Book"), "", null, null));
      return 0;
    }
    bookNotFound(book);
    return 1;
  }

  private static int exportUnavailable(CommandContext<CommandSourceStack> context) {
    context.getSource().sendFailure(Component.literal(EXPORT_UNAVAILABLE).withStyle(ChatFormatting.RED));
    return 1;
  }

  private static int exportUnavailable(CommandContext<CommandSourceStack> context, Object ignored) {
    return exportUnavailable(context);
  }

  public static void bookNotFound(Identifier book) {
    Player player = Minecraft.getInstance().player;
    if (player != null) {
      player.sendSystemMessage(Component.translatable(BOOK_NOT_FOUND, book).withStyle(ChatFormatting.RED));
    }
  }
}
