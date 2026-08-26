package slimeknights.mantle.client.model;

import com.mojang.math.Quadrant;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.model.data.ModelProperty;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.block.entity.IRetexturedBlockEntity;
import slimeknights.mantle.util.RetexturedHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Dynamic blockstate model for Mantle retextured block models in NeoForge 1.21.4+. */
public class RetexturedBlockStateModel implements DynamicBlockStateModel {
  private static final ModelProperty<Object> MATERIAL_PROPERTY = loadMaterialProperty();
  private static final Object MATERIAL_RENDER_INFO_LOADER = loadMaterialRenderInfoLoader();
  private static final Class<?> MATERIAL_VARIANT_ID_CLASS = loadMaterialVariantIdClass();

  private final ModelBaker baker;
  private final ResolvedModel model;
  private final ModelState modelState;
  private final Set<String> retexturedNames;
  private final List<net.minecraft.client.resources.model.cuboid.CuboidModelElement> elements;
  private final BlockStateModelPart basePart;
  private final Map<VariantSource, BlockStateModelPart> variantCache = new ConcurrentHashMap<>();

  private RetexturedBlockStateModel(ModelBaker baker, ResolvedModel model, ModelState modelState, Set<String> retexturedNames, List<net.minecraft.client.resources.model.cuboid.CuboidModelElement> elements) {
    this.baker = baker;
    this.model = model;
    this.modelState = modelState;
    this.retexturedNames = retexturedNames;
    this.elements = elements;
    this.basePart = SimpleModelWrapper.bake(baker, model, modelState);
  }

  @Override
  public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
    VariantSource source = variantFrom(level, pos);
    return source.isEmpty() ? this : new GeometryKey(this, source);
  }

  @Override
  public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
    parts.add(part(variantFrom(level, pos)));
  }

  private static VariantSource variantFrom(BlockAndTintGetter level, BlockPos pos) {
    Block texture = level.getModelData(pos).get(RetexturedHelper.BLOCK_PROPERTY);
    if ((texture == null || texture == Blocks.AIR) && level.getBlockEntity(pos) instanceof IRetexturedBlockEntity retextured) {
      texture = retextured.getTexture();
    }
    Object material = MATERIAL_PROPERTY == null ? null : level.getModelData(pos).get(MATERIAL_PROPERTY);
    return new VariantSource(texture, material);
  }

  private BlockStateModelPart part(VariantSource source) {
    if (source.isEmpty() || retexturedNames.isEmpty()) {
      return basePart;
    }
    return variantCache.computeIfAbsent(source, this::bakeVariant);
  }

  private BlockStateModelPart bakeVariant(VariantSource source) {
    if (elements.isEmpty()) {
      return basePart;
    }
    TextureSlots baseSlots = model.getTopTextureSlots();
    Material.Baked fallback = model.resolveParticleMaterial(baseSlots, baker);
    Material.Baked retexturedSide = source.texture() != null && source.texture() != Blocks.AIR ? bakedTextureMaterial(source.texture(), false) : null;
    Material.Baked retexturedEnd = source.texture() != null && source.texture() != Blocks.AIR ? bakedTextureMaterial(source.texture(), true) : null;
    java.util.function.Function<String, Material.Baked> materialGetter = name -> {
      String lookup = name.startsWith("#") ? name.substring(1) : name;
      Material materialSlot = baseSlots.getMaterial(lookup);
      Material.Baked original = materialSlot != null ? baker.materials().get(materialSlot, model) : fallback;
      if (retexturedNames.contains(lookup)) {
        Material.Baked fromMaterial = bakedMaterialMaterial(source.material(), materialSlot);
        if (fromMaterial != null) {
          return fromMaterial;
        }
        Material.Baked fromBlock = isEndTextureSlot(lookup) ? retexturedEnd : retexturedSide;
        return fromBlock != null ? fromBlock : original;
      }
      return original;
    };
    QuadCollection.Builder builder = new QuadCollection.Builder();
    UnbakedElementsHelper.bakeElements(baker, builder, elements, materialGetter, modelState);
    QuadCollection quads = builder.build();
    Material.Baked particle = retexturedSide != null ? retexturedSide : model.resolveParticleMaterial(baseSlots, baker);
    return new SimpleModelWrapper(quads, model.getTopAmbientOcclusion(), particle);
  }

  private Material.Baked bakedMaterialMaterial(Object materialVariant, Material baseMaterial) {
    if (materialVariant == null || MATERIAL_RENDER_INFO_LOADER == null || MATERIAL_VARIANT_ID_CLASS == null || baseMaterial == null) {
      return null;
    }

    return null;
  }

  public static Material.Baked bakedTextureMaterial(Block block, boolean endTexture) {
    var modelSet = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
    BlockStateModel blockModel = modelSet.get(block.defaultBlockState());
    List<BlockStateModelPart> parts = new ArrayList<>();
    blockModel.collectParts(RandomSource.create(42L), parts);
    Direction[] preferred = endTexture
      ? new Direction[] { Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null }
      : new Direction[] { Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP, Direction.DOWN, null };
    for (Direction direction : preferred) {
      for (BlockStateModelPart part : parts) {
        List<BakedQuad> quads = part.getQuads(direction);
        if (!quads.isEmpty()) {
          var info = quads.getFirst().materialInfo();
          return new Material.Baked(info.sprite(), false);
        }
      }
    }
    return blockModel.particleMaterial();
  }

  public static boolean isEndTextureSlot(String slotName) {
    String lowerSlot = slotName.toLowerCase(java.util.Locale.ROOT);
    return lowerSlot.contains("bottom") || lowerSlot.contains("end") || lowerSlot.contains("top");
  }

  @Override
  public Material.Baked particleMaterial() {
    return basePart.particleMaterial();
  }

  @Override
  public int materialFlags() {
    return basePart.materialFlags();
  }

  private static ModelProperty<Object> loadMaterialProperty() {
    try {
      Class<?> propertiesClass = Class.forName("slimeknights.tconstruct.library.client.model.ModelProperties");
      @SuppressWarnings("unchecked") ModelProperty<Object> property = (ModelProperty<Object>) propertiesClass.getField("MATERIAL").get(null);
      return property;
    } catch (ReflectiveOperationException e) {
      return null;
    }
  }

  private static Object loadMaterialRenderInfoLoader() {
    try {
      Class<?> loaderClass = Class.forName("slimeknights.tconstruct.library.client.materials.MaterialRenderInfoLoader");
      return loaderClass.getField("INSTANCE").get(null);
    } catch (ReflectiveOperationException e) {
      return null;
    }
  }

  private static Class<?> loadMaterialVariantIdClass() {
    try {
      return Class.forName("slimeknights.tconstruct.library.materials.definition.MaterialVariantId");
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private record VariantSource(Block texture, Object material) {
    private boolean isEmpty() {
      return (texture == null || texture == Blocks.AIR) && material == null;
    }
  }

  private record GeometryKey(RetexturedBlockStateModel model, VariantSource source) {}

  public record Unbaked(Identifier model, int x, int y, boolean uvlock) implements CustomUnbakedBlockStateModel {
    public static final Identifier ID = Mantle.getResource("retextured_block");
    public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model),
      Codec.INT.optionalFieldOf("x", 0).forGetter(Unbaked::x),
      Codec.INT.optionalFieldOf("y", 0).forGetter(Unbaked::y),
      Codec.BOOL.optionalFieldOf("uvlock", false).forGetter(Unbaked::uvlock)
    ).apply(instance, Unbaked::new));

    @Override
    public BlockStateModel bake(ModelBaker baker) {
      ResolvedModel resolved = baker.getModel(model);
      BlockModelRotation rotation = BlockModelRotation.get(Quadrant.fromXYAngles(quadrant(x), quadrant(y)));
      ModelState state = uvlock ? rotation.withUvLock() : rotation;
      Object fluidTextureModel = findFluidTextureModel(resolved);
      if (fluidTextureModel != null) {
        return bakeFluidTextureModel(fluidTextureModel, baker, resolved, state);
      }
      RetexturedModel retexturedModel = findRetexturedModel(resolved);
      Set<String> retextured = retexturedModel == null ? Set.of() : retexturedModel.retexturedNames();
      List<net.minecraft.client.resources.model.cuboid.CuboidModelElement> elements = retexturedModel == null ? List.of() : retexturedModel.elements();
      return new RetexturedBlockStateModel(baker, resolved, state, retextured, elements);
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
      resolver.markDependency(model);
    }

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
      return MAP_CODEC;
    }

    private static Object findFluidTextureModel(ResolvedModel resolved) {
      final Class<?> fluidTextureClass;
      try {
        fluidTextureClass = Class.forName("slimeknights.tconstruct.library.client.model.FluidTextureModel");
      } catch (ClassNotFoundException e) {
        return null;
      }
      for (ResolvedModel current = resolved; current != null; current = current.parent()) {
        Object wrapped = current.wrapped();
        if (wrapped != null && fluidTextureClass.isInstance(wrapped)) {
          return wrapped;
        }
      }
      return null;
    }

    private static BlockStateModel bakeFluidTextureModel(Object model, ModelBaker baker, ResolvedModel resolved, ModelState state) {
      try {
        Method bake = model.getClass().getMethod("bakeDynamic", ModelBaker.class, ResolvedModel.class, ModelState.class);
        return (BlockStateModel) bake.invoke(model, baker, resolved, state);
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Failed to bake FluidTextureModel dynamic block model", e);
      }
    }

    private static RetexturedModel findRetexturedModel(ResolvedModel resolved) {
      for (ResolvedModel current = resolved; current != null; current = current.parent()) {
        if (current.wrapped() instanceof RetexturedModel model) {
          return model;
        }
      }
      return null;
    }

    private static Quadrant quadrant(int angle) {
      return switch (Math.floorMod(angle, 360)) {
        case 90 -> Quadrant.R90;
        case 180 -> Quadrant.R180;
        case 270 -> Quadrant.R270;
        default -> Quadrant.R0;
      };
    }
  }
}