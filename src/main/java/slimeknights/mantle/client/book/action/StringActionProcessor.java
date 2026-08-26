package slimeknights.mantle.client.book.action;


import net.minecraft.resources.Identifier;
import slimeknights.mantle.client.book.action.protocol.ActionProtocol;
import net.minecraft.resources.Identifier;
import slimeknights.mantle.client.screen.book.BookScreen;

import net.minecraft.resources.Identifier;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import java.util.HashMap;

public class StringActionProcessor {

  public static final String PROTOCOL_SEPARATOR = " ";

  private static final HashMap<Identifier, ActionProtocol> protocols = new HashMap<>();

  public static void registerProtocol(Identifier id, ActionProtocol protocol) {
    if (protocols.containsKey(id)) {
      throw new IllegalArgumentException("Protocol " + id + " already registered.");
    }

    protocols.put(id, protocol);
  }

  //Format: modid:action param
  public static void process(@Nullable String action, BookScreen book) {
    if (action == null || !action.contains(PROTOCOL_SEPARATOR)) {
      return;
    }

    String id = action.substring(0, action.indexOf(PROTOCOL_SEPARATOR));
    if(!id.contains(":"))
      id = "mantle:" + id;

    Identifier protoId = Identifier.parse(id);
    String protoParam = action.substring(action.indexOf(PROTOCOL_SEPARATOR) + PROTOCOL_SEPARATOR.length());

    if (protocols.containsKey(protoId)) {
      protocols.get(protoId).processCommand(book, protoParam);
    }
  }
}
