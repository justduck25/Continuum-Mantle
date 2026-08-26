package slimeknights.mantle.client.model;

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
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class RetexturedModel extends AbstractUnbakedModel {
  private final List<CuboidModelElement> elements;
  private final Set<String> retexturedNames;
  private final List<ColorData> colorData;

  private RetexturedModel(StandardModelParameters parameters, List<CuboidModelElement> elements, Set<String> retexturedNames, List<ColorData> colorData) {
    super(parameters);
    this.elements = elements;
    this.retexturedNames = retexturedNames;
    this.colorData = colorData;
  }

  @Override
  public void resolveDependencies(Resolver resolver) {
    super.resolveDependencies(resolver);
  }

  @Override
  public ExtendedUnbakedGeometry geometry() {
    return new RetexturedGeometry(elements, retexturedNames, colorData, parameters.textures());
  }

  /** Elements parsed from this model. Used by the dynamic blockstate model port. */
  public List<CuboidModelElement> elements() {
    return elements;
  }

  /** Texture slot names that should be replaced by the stored retextured block. */
  public Set<String> retexturedNames() {
    return retexturedNames;
  }

  /** Static color data parsed from the model. */
  public List<ColorData> colorData() {
    return colorData;
  }

  public static final UnbakedModelLoader<RetexturedModel> LOADER = RetexturedModel::deserialize;

  public static RetexturedModel deserialize(JsonObject json, JsonDeserializationContext context) {
    StandardModelParameters parameters = StandardModelParameters.parse(json, context);
    List<CuboidModelElement> elements = parseElements(json, context);
    Set<String> retexturedNames = getRetexturedNames(json);
    List<ColorData> colorData = ColorData.LIST_LOADABLE.getOrDefault(json, "colors", List.of());
    return new RetexturedModel(parameters, elements, retexturedNames, colorData);
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

  public static Set<String> getRetexturedNames(JsonObject json) {
    if (json.has("retextured")) {
      JsonElement retextured = json.get("retextured");
      if (retextured.isJsonArray()) {
        JsonArray array = retextured.getAsJsonArray();
        if (array.isEmpty()) {
          throw new JsonSyntaxException("Must have at least one texture in retextured");
        }
        List<String> builder = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
          builder.add(GsonHelper.convertToString(array.get(i), "retextured[" + i + "]"));
        }
        return Set.copyOf(builder);
      }
      if (retextured.isJsonPrimitive()) {
        return Set.of(retextured.getAsString());
      }
    }
    throw new JsonSyntaxException("Missing retextured, expected to find a String or a JsonArray");
  }

  private record RetexturedGeometry(
    List<CuboidModelElement> elements,
    Set<String> retexturedNames,
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
