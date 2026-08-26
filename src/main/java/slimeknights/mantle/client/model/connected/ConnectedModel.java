package slimeknights.mantle.client.model.connected;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.ExtendedUnbakedGeometry;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import slimeknights.mantle.client.model.builder.ColorData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ConnectedModel extends AbstractUnbakedModel {
  private final List<CuboidModelElement> elements;
  private final Map<String, String[]> connectedTextures;
  private final Set<Direction> sides;
  private final String predicate;
  private final List<ColorData> colorData;

  private ConnectedModel(StandardModelParameters parameters, List<CuboidModelElement> elements, Map<String, String[]> connectedTextures, Set<Direction> sides, String predicate, List<ColorData> colorData) {
    super(parameters);
    this.elements = elements;
    this.connectedTextures = connectedTextures;
    this.sides = sides;
    this.predicate = predicate;
    this.colorData = colorData;
  }

  @Override
  public void resolveDependencies(Resolver resolver) {
    super.resolveDependencies(resolver);
  }

  @Override
  public ExtendedUnbakedGeometry geometry() {
    return new ConnectedGeometry(elements, connectedTextures, sides, predicate, colorData, parameters.textures());
  }

  public static final UnbakedModelLoader<ConnectedModel> LOADER = ConnectedModel::deserialize;

  public static ConnectedModel deserialize(JsonObject json, JsonDeserializationContext context) {
    StandardModelParameters parameters = StandardModelParameters.parse(json, context);
    List<CuboidModelElement> elements = parseElements(json, context);
    JsonObject connection = GsonHelper.getAsJsonObject(json, "connection");
    Map<String, String[]> connectedTextures = parseConnectedTextures(connection);
    Set<Direction> sides = parseSides(connection);
    String predicate = GsonHelper.getAsString(connection, "predicate", null);
    List<ColorData> colorData = ColorData.LIST_LOADABLE.getOrDefault(json, "colors", List.of());
    return new ConnectedModel(parameters, elements, connectedTextures, sides, predicate, colorData);
  }

  private static List<CuboidModelElement> parseElements(JsonObject json, JsonDeserializationContext context) {
    if (!json.has("elements")) {
      return List.of();
    }
    JsonArray elementsJson = GsonHelper.getAsJsonArray(json, "elements");
    List<CuboidModelElement> elements = new ArrayList<>(elementsJson.size());
    for (int i = 0; i < elementsJson.size(); i++) {
      elements.add(context.deserialize(elementsJson.get(i), CuboidModelElement.class));
    }
    return Collections.unmodifiableList(elements);
  }

  private static Map<String, String[]> parseConnectedTextures(JsonObject connection) {
    JsonObject textures = GsonHelper.getAsJsonObject(connection, "textures");
    Map<String, String[]> result = new java.util.HashMap<>();
    for (Map.Entry<String, JsonElement> entry : textures.entrySet()) {
      String typeName = GsonHelper.convertToString(entry.getValue(), "connection/textures/" + entry.getKey());
      result.put(entry.getKey(), ConnectedModelRegistry.deserializeType(entry.getValue(), "connection/textures/" + entry.getKey()));
    }
    return Collections.unmodifiableMap(result);
  }

  private static Set<Direction> parseSides(JsonObject connection) {
    if (!connection.has("sides")) {
      return Set.of(Direction.values());
    }
    JsonArray sidesArray = GsonHelper.getAsJsonArray(connection, "sides");
    Set<Direction> sides = EnumSet.noneOf(Direction.class);
    for (int i = 0; i < sidesArray.size(); i++) {
      Direction dir = Direction.byName(GsonHelper.convertToString(sidesArray.get(i), "sides[" + i + "]"));
      if (dir != null) {
        sides.add(dir);
      }
    }
    return Collections.unmodifiableSet(sides);
  }

  private record ConnectedGeometry(
    List<CuboidModelElement> elements,
    Map<String, String[]> connectedTextures,
    Set<Direction> sides,
    String predicate,
    List<ColorData> colorData,
    TextureSlots.Data textures
  ) implements ExtendedUnbakedGeometry {
    @Override
    public QuadCollection bake(TextureSlots textureSlots, ModelBaker baker, ModelState state, ModelDebugName debugName, ContextMap additionalProperties) {
      MaterialBaker materialBaker = baker.materials();
      Function<String, Material.Baked> materialGetter = name -> {
        String lookup = name.startsWith("#") ? name.substring(1) : name;
        Material material = textureSlots.getMaterial(lookup);
        return material != null ? materialBaker.get(material, debugName) : null;
      };

      QuadCollection.Builder quadBuilder = new QuadCollection.Builder();
      if (!elements.isEmpty()) {
        UnbakedElementsHelper.bakeElements(baker, quadBuilder, elements, materialGetter, state);
      }
      return quadBuilder.build();
    }
  }
}
