package slimeknights.mantle.client.book.data.element;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import slimeknights.mantle.client.book.repository.BookRepository;

public class DataLocation implements IDataElement {

  public String file;
  public transient Identifier location;

  @Override
  public void load(BookRepository source) {
    this.location = "$BLOCK_ATLAS".equals(this.file) ? TextureAtlas.LOCATION_BLOCKS : source.getIdentifier(this.file, true);
  }
}
