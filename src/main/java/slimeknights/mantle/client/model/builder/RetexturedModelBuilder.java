package slimeknights.mantle.client.model.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import slimeknights.mantle.Mantle;

public class RetexturedModelBuilder extends ColoredModelBuilder {
  private final JsonArray retextured = new JsonArray();

  public RetexturedModelBuilder() {
    super(Mantle.getResource("retextured"), false);
  }

  /** Marks the given texture as retextured. Uses the texture name, not path. */
  public RetexturedModelBuilder retexture(String name) {
    this.retextured.add(name);
    return this;
  }

  @Override
  protected CustomLoaderBuilder copyInternal() {
    RetexturedModelBuilder copy = new RetexturedModelBuilder();
    retextured.forEach(e -> copy.retextured.add(e.deepCopy()));
    return copy;
  }

  @Override
  public JsonObject toJson(JsonObject json) {
    json = super.toJson(json);
    json.add("retextured", retextured);
    return json;
  }
}
