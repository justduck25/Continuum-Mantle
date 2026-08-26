package slimeknights.mantle.client.model.builder;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import slimeknights.mantle.Mantle;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link slimeknights.mantle.client.model.util.ColoredBlockModel}, used as a base for other model builders.
 */
public class ColoredModelBuilder extends CustomLoaderBuilder {
  private final List<ColorData> colors = new ArrayList<>();

  public ColoredModelBuilder() {
    this(Mantle.getResource("colored_block"), false);
  }

  protected ColoredModelBuilder(Identifier loaderId, boolean allowInlineElements) {
    super(loaderId, allowInlineElements);
  }

  /** Adds a full color data for the next element */
  public ColoredModelBuilder colorData(ColorData data) {
    colors.add(data);
    return this;
  }

  /** Sets the color for the next element */
  public ColoredModelBuilder color(int color) {
    return colorData(new ColorData(color, -1, null));
  }

  /** Sets the luminosity for the next element */
  public ColoredModelBuilder luminosity(int luminosity) {
    return colorData(new ColorData(-1, luminosity, null));
  }

  @Override
  protected CustomLoaderBuilder copyInternal() {
    ColoredModelBuilder copy = new ColoredModelBuilder(loaderId, allowInlineElements);
    copy.colors.addAll(this.colors);
    return copy;
  }

  @Override
  public JsonObject toJson(JsonObject json) {
    json = super.toJson(json);
    if (!colors.isEmpty()) {
      json.add("colors", ColorData.LIST_LOADABLE.serialize(colors));
    }
    return json;
  }
}
