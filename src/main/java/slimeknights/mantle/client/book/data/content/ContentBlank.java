package slimeknights.mantle.client.book.data.content;

import net.minecraft.resources.Identifier;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.util.html.HtmlGroup;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ContentBlank extends PageContent {
  public static final Identifier ID = Mantle.getResource("blank");

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
  }

  @Nullable
  @Override
  public HtmlGroup toHTML(BookData book) {
    return null;
  }
}
