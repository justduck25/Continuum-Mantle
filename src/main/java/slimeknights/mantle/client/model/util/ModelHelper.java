package slimeknights.mantle.client.model.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import org.joml.Vector3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Utilities to help in custom models
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ModelHelper {
  private static final Map<Block,Identifier> TEXTURE_NAME_CACHE = new ConcurrentHashMap<>();
  /** Listener instance to clear cache */
  public static final ResourceManagerReloadListener LISTENER = manager -> TEXTURE_NAME_CACHE.clear();

  /**
   * Gets the texture name for a block from the model manager
   * @param block  Block to fetch
   * @return Texture name for the block
   */
  @SuppressWarnings("deprecation")
  private static Identifier getParticleTextureInternal(Block block) {
    TextureAtlasSprite particle = Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(block.defaultBlockState()).sprite();
    //noinspection ConstantConditions  dumb mods returning null particle icons
    if (particle != null) {
      return particle.contents().name();
    }
    return MissingTextureAtlasSprite.getLocation();
  }

  /**
   * Gets the name of a particle texture for a block, using the cached value if present
   * @param block Block to fetch
   * @return Texture name for the block
   */
  public static Identifier getParticleTexture(Block block) {
    return TEXTURE_NAME_CACHE.computeIfAbsent(block, ModelHelper::getParticleTextureInternal);
  }

  /* JSON */

  /**
   * Converts a JSON float array to the specified object
   * @param json    JSON object
   * @param name    Name of the array in the object to fetch
   * @param size    Expected array size
   * @param mapper  Functon to map from the array to the output type
   * @param <T> Output type
   * @return  Vector3f of data
   * @throws JsonParseException  If there is no array or the length is wrong
   * @deprecated use {@link slimeknights.mantle.data.loadable.array.FloatArrayLoadable}
   */
  @Deprecated(forRemoval = true)
  public static <T> T arrayToObject(JsonObject json, String name, int size, Function<float[], T> mapper) {
    JsonArray array = GsonHelper.getAsJsonArray(json, name);
    if (array.size() != size) {
      throw new JsonParseException("Expected " + size + " " + name + " values, found: " + array.size());
    }
    float[] vec = new float[size];
    for(int i = 0; i < size; ++i) {
      vec[i] = GsonHelper.convertToFloat(array.get(i), name + "[" + i + "]");
    }
    return mapper.apply(vec);
  }

  /**
   * Converts a JSON array with 3 elements into a Vector3f
   * @param json  JSON object
   * @param name  Name of the array in the object to fetch
   * @return  Vector3f of data
   * @throws JsonParseException  If there is no array or the length is wrong
   * @deprecated use {@link slimeknights.mantle.data.loadable.common.Vector3fLoadable}
   */
  @Deprecated(forRemoval = true)
  public static Vector3f arrayToVector(JsonObject json, String name) {
    return arrayToObject(json, name, 3, arr -> new Vector3f(arr[0], arr[1], arr[2]));
  }

  /** @deprecated use {@link slimeknights.mantle.data.loadable.common.Vector3fLoadable} */
  @Deprecated(forRemoval = true)
  public static JsonArray vectorToJson(Vector3f vector) {
    JsonArray array = new JsonArray();
    array.add(vector.x());
    array.add(vector.y());
    array.add(vector.z());
    return array;
  }

  /** Validates the given rotation is in 90 degree increments */
  public static boolean checkRotation(float rotation) {
    return rotation >= 0 && rotation % 90 == 0 && rotation <= 270;
  }

  /**
   * Gets a rotation from JSON
   * @param json  JSON parent
   * @return  Integer of 0, 90, 180, or 270
   */
  public static int getRotation(JsonObject json, String key) {
    int i = GsonHelper.getAsInt(json, key, 0);
    if (checkRotation(i)) {
      return i;
    } else {
      throw new JsonParseException("Invalid '" + key + "' " + i + " found, only 0/90/180/270 allowed");
    }
  }

  /**
   * Bakes elements with per-element color data and luminosity support.
   */
  public static void bakeElements(
      net.minecraft.client.resources.model.ModelBaker baker,
      net.minecraft.client.resources.model.geometry.QuadCollection.Builder builder,
      java.util.List<net.minecraft.client.resources.model.cuboid.CuboidModelElement> elements,
      java.util.List<slimeknights.mantle.client.model.builder.ColorData> colorData,
      Function<String, net.minecraft.client.resources.model.sprite.Material.Baked> materialGetter,
      net.minecraft.client.renderer.block.dispatch.ModelState modelState) {
    for (int i = 0; i < elements.size(); i++) {
      net.minecraft.client.resources.model.cuboid.CuboidModelElement element = elements.get(i);
      slimeknights.mantle.client.model.builder.ColorData data = colorData.size() == 1 ? colorData.get(0) : slimeknights.mantle.util.LogicHelper.getOrDefault(colorData, i, slimeknights.mantle.client.model.builder.ColorData.DEFAULT);
      int color = data.color();
      int luminosity = data.luminosity();

      element.faces().forEach((side, face) -> {
        net.minecraft.client.resources.model.sprite.Material.Baked material = materialGetter.apply(face.texture());
        if (material == null) return;
        int lightEmission = luminosity >= 0 ? luminosity : element.lightEmission();
        net.minecraft.client.resources.model.geometry.BakedQuad quad = net.minecraft.client.resources.model.cuboid.FaceBakery.bakeQuad(
            baker,
            element.from(),
            element.to(),
            face,
            material,
            side,
            modelState,
            element.rotation(),
            element.shade(),
            lightEmission);
        if (color != -1) {
          quad = withColor(quad, color, baker);
        }
        if (face.cullForDirection() == null) {
          builder.addUnculledFace(quad);
        } else {
          builder.addCulledFace(net.minecraft.core.Direction.rotate(modelState.transformation().getMatrix(), face.cullForDirection()), quad);
        }
      });
    }
  }

  /**
   * Applies a vertex color to a BakedQuad.
   */
  public static net.minecraft.client.resources.model.geometry.BakedQuad withColor(net.minecraft.client.resources.model.geometry.BakedQuad quad, int color, net.minecraft.client.resources.model.ModelBaker baker) {
    net.neoforged.neoforge.client.model.quad.BakedColors bakedColors = net.neoforged.neoforge.client.model.quad.BakedColors.of(color);
    if (baker != null && baker.interner() != null) {
      bakedColors = baker.interner().colors(bakedColors);
    }
    return new net.minecraft.client.resources.model.geometry.BakedQuad(
        quad.position0(),
        quad.position1(),
        quad.position2(),
        quad.position3(),
        quad.packedUV0(),
        quad.packedUV1(),
        quad.packedUV2(),
        quad.packedUV3(),
        quad.direction(),
        quad.materialInfo(),
        quad.bakedNormals(),
        bakedColors);
  }
}
