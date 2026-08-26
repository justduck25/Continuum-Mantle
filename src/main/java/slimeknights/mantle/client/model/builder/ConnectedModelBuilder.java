package slimeknights.mantle.client.model.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import slimeknights.mantle.Mantle;

import java.util.EnumSet;
import java.util.Set;

/** Builder for {@link slimeknights.mantle.client.model.connected.ConnectedModel} */
public class ConnectedModelBuilder extends ColoredModelBuilder {
  private final JsonObject connectedTextures = new JsonObject();
  private Set<Direction> sides = null;
  private String predicate = null;

  public ConnectedModelBuilder() {
    super(Mantle.getResource("connected"), false);
  }

  /**
   * Makes the given texture connected using the given connection type.
   * @param name  Name of the texture from the textures list, not the full path.
   * @param type  Connection type, see {@link slimeknights.mantle.client.model.connected.ConnectedModelRegistry}
   */
  public ConnectedModelBuilder connected(String name, String type) {
    connectedTextures.addProperty(name, type);
    return this;
  }

  /** Sets the sides of the block that receive connected textures, used to simplify some logic */
  public ConnectedModelBuilder setSides(Direction first, Direction... other) {
    this.sides = EnumSet.of(first, other);
    return this;
  }

  /** Sets the connection predicate, must be registered with the {@link slimeknights.mantle.client.model.connected.ConnectedModelRegistry} */
  public ConnectedModelBuilder setPredicate(String predicate) {
    this.predicate = predicate;
    return this;
  }

  @Override
  protected CustomLoaderBuilder copyInternal() {
    ConnectedModelBuilder copy = new ConnectedModelBuilder();
    connectedTextures.entrySet().forEach(entry -> copy.connectedTextures.add(entry.getKey(), entry.getValue().deepCopy()));
    if (this.sides != null) {
      copy.sides = EnumSet.copyOf(this.sides);
    }
    copy.predicate = this.predicate;
    return copy;
  }

  @Override
  public JsonObject toJson(JsonObject json) {
    json = super.toJson(json);
    JsonObject data = new JsonObject();
    json.add("connection", data);
    data.add("textures", connectedTextures);
    if (sides != null) {
      JsonArray sides = new JsonArray();
      for (Direction side : this.sides) {
        sides.add(side.getSerializedName());
      }
      data.add("sides", sides);
    }
    if (predicate != null) {
      data.addProperty("predicate", predicate);
    }
    return json;
  }
}
