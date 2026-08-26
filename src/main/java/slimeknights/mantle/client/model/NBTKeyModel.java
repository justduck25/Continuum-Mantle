package slimeknights.mantle.client.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.ExtendedUnbakedGeometry;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class NBTKeyModel extends AbstractUnbakedModel {
  private final List<CuboidModelElement> elements;
  private static final Multimap<Identifier, Pair<String, Identifier>> EXTRA_TEXTURES = HashMultimap.create();

  private NBTKeyModel(StandardModelParameters parameters, List<CuboidModelElement> elements) {
    super(parameters);
    this.elements = elements;
  }

  @Override
  public void resolveDependencies(Resolver resolver) {
    super.resolveDependencies(resolver);
  }

  @Override
  public ExtendedUnbakedGeometry geometry() {
    return new NBTKeyGeometry(elements, parameters.textures());
  }

  public static void registerExtraTexture(Identifier key, String textureName, Identifier texture) {
    EXTRA_TEXTURES.put(key, Pair.of(textureName, texture));
  }

  public static final UnbakedModelLoader<NBTKeyModel> LOADER = NBTKeyModel::deserialize;

  public static NBTKeyModel deserialize(JsonObject json, JsonDeserializationContext context) {
    StandardModelParameters parameters = StandardModelParameters.parse(json, context);
    List<CuboidModelElement> elements = parseElements(json, context);
    return new NBTKeyModel(parameters, elements);
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

  private record NBTKeyGeometry(
    List<CuboidModelElement> elements,
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
