package slimeknights.mantle.client.model.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.ExtendedUnbakedGeometry;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.neoforged.neoforge.client.model.pipeline.TransformingVertexPipeline;
import slimeknights.mantle.client.model.builder.LayerData;
import slimeknights.mantle.util.ItemLayerPixels;
import slimeknights.mantle.util.LogicHelper;

public class MantleItemLayerModel extends AbstractUnbakedModel {
  private final List<LayerData> layers;

  private MantleItemLayerModel(StandardModelParameters parameters, List<LayerData> layers) {
    super(parameters);
    this.layers = layers;
  }

  private LayerData getLayer(int index) {
    return LogicHelper.getOrDefault(this.layers, index, LayerData.DEFAULT);
  }

  @Override
  public void resolveDependencies(Resolver resolver) {
    Identifier parent = parameters.parent();
    if (parent != null) {
      resolver.markDependency(parent);
    }
  }

  @Override
  public ExtendedUnbakedGeometry geometry() {
    return new MantleItemLayerGeometry(layers);
  }

  public static final UnbakedModelLoader<MantleItemLayerModel> LOADER = MantleItemLayerModel::deserialize;

  public static MantleItemLayerModel deserialize(JsonObject json, JsonDeserializationContext context) {
    StandardModelParameters parameters = StandardModelParameters.parse(json, context);
    List<LayerData> layers = LayerData.LIST_LOADABLE.getOrDefault(json, "layers", List.of());
    return new MantleItemLayerModel(parameters, layers);
  }

  private record MantleItemLayerGeometry(List<LayerData> layers) implements ExtendedUnbakedGeometry {
    @Override
    public QuadCollection bake(TextureSlots textureSlots, ModelBaker baker, ModelState state, ModelDebugName debugName, ContextMap additionalProperties) {
      QuadCollection.Builder quadBuilder = new QuadCollection.Builder();
      ModelBaker.Interner interner = baker.interner();

      List<TextureAtlasSprite> sprites = new ArrayList<>();
      MaterialBaker materialBaker = baker.materials();
      for (String layer : ItemModelGenerator.LAYERS) {
        Material material = textureSlots.getMaterial(layer);
        if (material == null) break;
        sprites.add(materialBaker.get(material, debugName).sprite());
      }
      if (sprites.isEmpty()) {
        return quadBuilder.build();
      }

      ItemLayerPixels pixels = sprites.size() == 1 ? null : new ItemLayerPixels();
      Transformation transform = state.transformation();

      for (int i = sprites.size() - 1; i >= 0; i--) {
        TextureAtlasSprite sprite = sprites.get(i);
        LayerData data = LogicHelper.getOrDefault(layers, i, LayerData.DEFAULT);
        int color = data.color();
        int tint = data.noTint() ? -1 : i;
        int luminosity = data.luminosity();

        ChunkSectionLayer chunkLayer = luminosity > 0 ? ChunkSectionLayer.TRANSLUCENT : ChunkSectionLayer.SOLID;
        RenderType renderType = atlasRenderType(sprite);

        List<BakedQuad> quads = getQuadsForSprite(color, tint, sprite, transform, luminosity, pixels, interner, chunkLayer, renderType);
        for (BakedQuad quad : quads) {
          quadBuilder.addUnculledFace(quad);
        }
      }

      return quadBuilder.build();
    }

    private static RenderType atlasRenderType(TextureAtlasSprite sprite) {
      if (sprite.atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS)) {
        return Sheets.cutoutBlockItemSheet();
      }
      return Sheets.cutoutItemSheet();
    }
  }

  public static List<BakedQuad> getQuadsForSprite(int color, int tint, TextureAtlasSprite sprite, Transformation transform, int emissivity) {
    return getQuadsForSprite(color, tint, sprite, transform, emissivity, null, null, ChunkSectionLayer.SOLID, atlasRenderType(sprite));
  }

  public static List<BakedQuad> getQuadsForSprite(int color, int tint, TextureAtlasSprite sprite, Transformation transform, int emissivity, @Nullable ItemLayerPixels pixels) {
    return getQuadsForSprite(color, tint, sprite, transform, emissivity, pixels, null, ChunkSectionLayer.SOLID, atlasRenderType(sprite));
  }

  private static RenderType atlasRenderType(TextureAtlasSprite sprite) {
    if (sprite.atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS)) {
      return Sheets.cutoutBlockItemSheet();
    }
    return Sheets.cutoutItemSheet();
  }

  private static List<BakedQuad> getQuadsForSprite(int color, int tint, TextureAtlasSprite sprite, Transformation transform, int emissivity, @Nullable ItemLayerPixels pixels, @Nullable ModelBaker.Interner interner, ChunkSectionLayer chunkLayer, RenderType renderType) {
    List<BakedQuad> builder = new ArrayList<>();
    SpriteContents contents = sprite.contents();
    int uMax = contents.width();
    int vMax = contents.height();
    FaceData faceData = new FaceData(uMax, vMax);
    boolean[] translucent = {false};

    contents.getUniqueFrames().forEach(frame -> {
      boolean[] ptv = new boolean[uMax];
      Arrays.fill(ptv, true);

      for (int v = 0; v < vMax; v++) {
        boolean ptu = true;

        for (int u = 0; u < uMax; u++) {
          int alpha = sprite.getPixelRGBA(frame, u, vMax - v - 1) >> 24 & 0xFF;
          boolean t = alpha / 255.0F <= 0.1F;
          if (!t && alpha < 255) {
            translucent[0] = true;
          }

          if (ptu && !t) {
            faceData.set(Direction.WEST, u, v);
          }
          if (!ptu && t) {
            faceData.set(Direction.EAST, u - 1, v);
          }
          if (ptv[u] && !t) {
            faceData.set(Direction.UP, u, v);
          }
          if (!ptv[u] && t) {
            faceData.set(Direction.DOWN, u, v - 1);
          }

          ptu = t;
          ptv[u] = t;
        }

        if (!ptu) {
          faceData.set(Direction.EAST, uMax - 1, v);
        }
      }

      for (int u = 0; u < uMax; u++) {
        if (!ptv[u]) {
          faceData.set(Direction.DOWN, u, vMax - 1);
        }
      }
    });

    QuadBakingVertexConsumer quadBuilder = new QuadBakingVertexConsumer();
    quadBuilder.setTintIndex(tint);
    quadBuilder.setShade(false);
    quadBuilder.setAmbientOcclusion(true);
    quadBuilder.setLightEmission(emissivity << 4);
    quadBuilder.setSprite(sprite, chunkLayer, renderType);
    VertexConsumer quadConsumer = quadBuilder;
    if (!transform.isIdentity()) {
      quadConsumer = new TransformingVertexPipeline(quadBuilder, transform);
    }

    for (Direction facing : HORIZONTALS) {
      for (int v = 0; v < vMax; v++) {
        int uStart = 0;
        int uEnd = uMax;
        boolean building = false;

        for (int ux = 0; ux < uMax; ux++) {
          boolean canDraw = pixels == null || !pixels.get(ux, v, uMax, vMax);
          boolean face = canDraw && faceData.get(facing, ux, v);
          if (face) {
            uEnd = ux + 1;
            if (!building) {
              building = true;
              uStart = ux;
            }
          } else if (building && (!canDraw || translucent[0])) {
            int off = facing == Direction.DOWN ? 1 : 0;
            buildSideQuad(quadBuilder, quadConsumer, facing, color, tint, sprite, uStart, v + off, uEnd - uStart, emissivity, builder, interner, chunkLayer, renderType);
            building = false;
          }
        }

        if (building) {
          int off = facing == Direction.DOWN ? 1 : 0;
          buildSideQuad(quadBuilder, quadConsumer, facing, color, tint, sprite, uStart, v + off, uEnd - uStart, emissivity, builder, interner, chunkLayer, renderType);
        }
      }
    }

    for (Direction facing : VERTICALS) {
      for (int uxx = 0; uxx < uMax; uxx++) {
        int vStart = 0;
        int vEnd = vMax;
        boolean building = false;

        for (int v = 0; v < vMax; v++) {
          boolean canDraw = pixels == null || !pixels.get(uxx, v, uMax, vMax);
          boolean face = canDraw && faceData.get(facing, uxx, v);
          if (face) {
            vEnd = v + 1;
            if (!building) {
              building = true;
              vStart = v;
            }
          } else if (building && (!canDraw || translucent[0])) {
            int off = facing == Direction.EAST ? 1 : 0;
            buildSideQuad(quadBuilder, quadConsumer, facing, color, tint, sprite, uxx + off, vStart, vEnd - vStart, emissivity, builder, interner, chunkLayer, renderType);
            building = false;
          }
        }

        if (building) {
          int off = facing == Direction.EAST ? 1 : 0;
          buildSideQuad(quadBuilder, quadConsumer, facing, color, tint, sprite, uxx + off, vStart, vEnd - vStart, emissivity, builder, interner, chunkLayer, renderType);
        }
      }
    }

    builder.add(buildQuad(quadBuilder, quadConsumer, Direction.NORTH, color, tint, emissivity, sprite, interner, chunkLayer, renderType,
      0.0F, 0.0F, 0.46875F, sprite.getU0(), sprite.getV1(),
      0.0F, 1.0F, 0.46875F, sprite.getU0(), sprite.getV0(),
      1.0F, 1.0F, 0.46875F, sprite.getU1(), sprite.getV0(),
      1.0F, 0.0F, 0.46875F, sprite.getU1(), sprite.getV1()));
    builder.add(buildQuad(quadBuilder, quadConsumer, Direction.SOUTH, color, tint, emissivity, sprite, interner, chunkLayer, renderType,
      0.0F, 0.0F, 0.53125F, sprite.getU0(), sprite.getV1(),
      1.0F, 0.0F, 0.53125F, sprite.getU1(), sprite.getV1(),
      1.0F, 1.0F, 0.53125F, sprite.getU1(), sprite.getV0(),
      0.0F, 1.0F, 0.53125F, sprite.getU0(), sprite.getV0()));

    if (pixels != null) {
      contents.getUniqueFrames().forEach(frame -> {
        for (int vx = 0; vx < vMax; vx++) {
          for (int uxx = 0; uxx < uMax; uxx++) {
            int alphax = sprite.getPixelRGBA(0, uxx, vMax - vx - 1) >> 24 & 0xFF;
            if (alphax / 255.0F > 0.1F) {
              pixels.set(uxx, vx, uMax, vMax);
            }
          }
        }
      });
    }

    return List.copyOf(builder);
  }

  public static BakedQuad getQuadForGui(int color, int tint, TextureAtlasSprite sprite, Transformation transform, int emissivity) {
    QuadBakingVertexConsumer quadBuilder = new QuadBakingVertexConsumer();
    quadBuilder.setTintIndex(tint);
    quadBuilder.setShade(false);
    quadBuilder.setAmbientOcclusion(true);
    quadBuilder.setLightEmission(emissivity << 4);
    quadBuilder.setSprite(sprite, ChunkSectionLayer.SOLID, atlasRenderType(sprite));
    VertexConsumer quadConsumer = quadBuilder;
    if (!transform.isIdentity()) {
      quadConsumer = new TransformingVertexPipeline(quadBuilder, transform);
    }

    buildQuadRaw(quadBuilder, quadConsumer, Direction.SOUTH, color,
      0.0F, 0.0F, 0.53125F, sprite.getU0(), sprite.getV1(),
      1.0F, 0.0F, 0.53125F, sprite.getU1(), sprite.getV1(),
      1.0F, 1.0F, 0.53125F, sprite.getU1(), sprite.getV0(),
      0.0F, 1.0F, 0.53125F, sprite.getU0(), sprite.getV0());

    return quadBuilder.bakeQuad();
  }

  private static void buildSideQuad(QuadBakingVertexConsumer builder, VertexConsumer consumer, Direction side, int color, int tint, TextureAtlasSprite sprite, int u, int v, int size, int luminosity, List<BakedQuad> results, @Nullable ModelBaker.Interner interner, ChunkSectionLayer chunkLayer, RenderType renderType) {
    float eps = 0.01F;
    SpriteContents contents = sprite.contents();
    int width = contents.width();
    int height = contents.height();
    float x0 = (float) u / width;
    float y0 = (float) v / height;
    float x1 = x0;
    float y1 = y0;
    float z0 = 0.46875F;
    float z1 = 0.53125F;

    switch (side) {
      case WEST -> { z0 = 0.53125F; z1 = 0.46875F; y1 = (float)(v + size) / height; }
      case EAST -> { z0 = 0.53125F; z1 = 0.46875F; y1 = (float)(v + size) / height; }
      case DOWN -> { z0 = 0.53125F; z1 = 0.46875F; x1 = (float)(u + size) / width; }
      case UP -> { x1 = (float)(u + size) / width; }
    }

    float dx = (float) side.getUnitVec3i().getX() * eps / width;
    float dy = (float) side.getUnitVec3i().getY() * eps / height;
    float u0 = 16.0F * (x0 - dx);
    float u1 = 16.0F * (x1 - dx);
    float v0 = 16.0F * (1.0F - y0 - dy);
    float v1 = 16.0F * (1.0F - y1 - dy);

    Direction quadSide = side.getAxis() == Direction.Axis.Y ? side.getOpposite() : side;
    results.add(buildQuad(builder, consumer, quadSide, color, tint, luminosity, sprite, interner, chunkLayer, renderType,
      x0, y0, z0, sprite.getU(u0), sprite.getV(v0),
      x1, y1, z0, sprite.getU(u1), sprite.getV(v1),
      x1, y1, z1, sprite.getU(u1), sprite.getV(v1),
      x0, y0, z1, sprite.getU(u0), sprite.getV(v0)));
  }

  private static BakedQuad buildQuad(QuadBakingVertexConsumer builder, VertexConsumer consumer, Direction side, int color, int tint, int emissivity, TextureAtlasSprite sprite, @Nullable ModelBaker.Interner interner, ChunkSectionLayer chunkLayer, RenderType renderType,
      float x0, float y0, float z0, float u0, float v0,
      float x1, float y1, float z1, float u1, float v1,
      float x2, float y2, float z2, float u2, float v2,
      float x3, float y3, float z3, float u3, float v3) {
    builder.setTintIndex(tint);
    builder.setShade(false);
    builder.setAmbientOcclusion(true);
    builder.setLightEmission(emissivity << 4);
    builder.setSprite(sprite, chunkLayer, renderType);
    buildQuadRaw(builder, consumer, side, color,
      x0, y0, z0, u0, v0,
      x1, y1, z1, u1, v1,
      x2, y2, z2, u2, v2,
      x3, y3, z3, u3, v3);
    return builder.bakeQuad();
  }

  private static void buildQuadRaw(QuadBakingVertexConsumer builder, VertexConsumer consumer, Direction side, int color,
      float x0, float y0, float z0, float u0, float v0,
      float x1, float y1, float z1, float u1, float v1,
      float x2, float y2, float z2, float u2, float v2,
      float x3, float y3, float z3, float u3, float v3) {
    builder.setDirection(side);
    putVertex(consumer, x0, y0, z0, u0, v0, color);
    putVertex(consumer, x1, y1, z1, u1, v1, color);
    putVertex(consumer, x2, y2, z2, u2, v2, color);
    putVertex(consumer, x3, y3, z3, u3, v3, color);
  }

  private static void putVertex(VertexConsumer consumer, float x, float y, float z, float u, float v, int color) {
    consumer.addVertex(x, y, z);
    consumer.setColor(color);
    consumer.setUv(u, v);
    consumer.setNormal(0.0F, 0.0F, 1.0F);
  }

  private static final Direction[] HORIZONTALS = {Direction.UP, Direction.DOWN};
  private static final Direction[] VERTICALS = {Direction.WEST, Direction.EAST};

  private static class FaceData {
    private final EnumMap<Direction, BitSet> data = new EnumMap<>(Direction.class);
    private final int vMax;

    FaceData(int uMax, int vMax) {
      this.vMax = vMax;
      data.put(Direction.WEST, new BitSet(uMax * vMax));
      data.put(Direction.EAST, new BitSet(uMax * vMax));
      data.put(Direction.UP, new BitSet(uMax * vMax));
      data.put(Direction.DOWN, new BitSet(uMax * vMax));
    }

    public void set(Direction facing, int u, int v) {
      if (u >= 0 && v >= 0) {
        data.get(facing).set(v * vMax + u);
      }
    }

    public boolean get(Direction facing, int u, int v) {
      return data.get(facing).get(v * vMax + u);
    }
  }
}
