package slimeknights.mantle.client.model.connected;

import com.mojang.math.OctahedralGroup;
import com.mojang.math.Quadrant;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.model.util.ColoredBlockModel;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Dynamic blockstate model that applies connected texture variants based on neighboring world blocks.
 */
public class ConnectedBlockStateModel implements DynamicBlockStateModel {
  private final ModelBaker baker;
  private final ResolvedModel resolved;
  private final ModelState modelState;
  private final OctahedralGroup rotationGroup;
  private final List<CuboidModelElement> elements;
  private final Map<String, String[]> connectedTextures;
  private final Set<Direction> sides;
  private final BiPredicate<BlockState, BlockState> predicate;
  private final List<slimeknights.mantle.client.model.builder.ColorData> colorData;
  private final TextureSlots baseSlots;
  private final BlockStateModelPart basePart;
  private final BlockStateModelPart[] partsCache = new BlockStateModelPart[64];
  private final Map<String, Material.Baked> bakedTextureCache = new ConcurrentHashMap<>();

  public ConnectedBlockStateModel(
      ModelBaker baker,
      ResolvedModel resolved,
      ModelState modelState,
      OctahedralGroup rotationGroup,
      List<CuboidModelElement> elements,
      Map<String, String[]> connectedTextures,
      Set<Direction> sides,
      BiPredicate<BlockState, BlockState> predicate,
      List<slimeknights.mantle.client.model.builder.ColorData> colorData) {
    this.baker = baker;
    this.resolved = resolved;
    this.modelState = modelState;
    this.rotationGroup = rotationGroup;
    this.elements = elements;
    this.connectedTextures = connectedTextures;
    this.sides = sides;
    this.predicate = predicate;
    this.colorData = colorData;
    this.baseSlots = resolved.getTopTextureSlots();
    this.basePart = SimpleModelWrapper.bake(baker, resolved, modelState);
    this.partsCache[0] = this.basePart;
  }

  @Override
  public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
    if (connectedTextures.isEmpty() || elements.isEmpty()) {
      return this;
    }
    byte connections = getConnections(level, pos, state);
    return connections == 0 ? this : new GeometryKey(this, connections);
  }

  @Override
  public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
    if (connectedTextures.isEmpty() || elements.isEmpty()) {
      parts.add(basePart);
      return;
    }
    byte connections = getConnections(level, pos, state);
    parts.add(getOrBakePart(connections));
  }

  private byte getConnections(BlockAndTintGetter level, BlockPos pos, BlockState state) {
    byte connections = 0;
    for (Direction dir : Direction.values()) {
      if (sides.contains(dir)) {
        Direction worldDir = rotationGroup.rotate(dir);
        BlockState neighborState = level.getBlockState(pos.relative(worldDir));
        if (predicate.test(state, neighborState)) {
          connections |= (byte) (1 << dir.get3DDataValue());
        }
      }
    }
    return connections;
  }

  private BlockStateModelPart getOrBakePart(byte connections) {
    int index = connections & 0x3F;
    BlockStateModelPart part = partsCache[index];
    if (part == null) {
      synchronized (partsCache) {
        part = partsCache[index];
        if (part == null) {
          part = bakeVariant(connections);
          partsCache[index] = part;
        }
      }
    }
    return part;
  }

  private BlockStateModelPart bakeVariant(byte connections) {
    if (connections == 0) {
      return basePart;
    }

    List<CuboidModelElement> updatedElements = new ArrayList<>(elements.size());
    for (CuboidModelElement element : elements) {
      Map<Direction, CuboidFace> updatedFaces = new EnumMap<>(Direction.class);
      for (Map.Entry<Direction, CuboidFace> entry : element.faces().entrySet()) {
        Direction faceDir = entry.getKey();
        CuboidFace originalFace = entry.getValue();
        CuboidFace face = originalFace;

        String textureName = originalFace.texture();
        String lookup = textureName.startsWith("#") ? textureName.substring(1) : textureName;

        String connectedSlot = findConnectedSlot(lookup);
        if (connectedSlot != null) {
          String[] suffixes = connectedTextures.get(connectedSlot);
          if (suffixes != null) {
            Function<Direction, Direction> transform = getTransform(faceDir, originalFace.uvs(), originalFace.rotation());
            String suffix = getTextureSuffix(suffixes, connections, transform);
            if (!suffix.isEmpty()) {
              String suffixedTexture = connectedSlot + "_" + suffix;
              face = new CuboidFace(originalFace.cullForDirection(), originalFace.tintIndex(), "#" + suffixedTexture, originalFace.uvs(), originalFace.rotation());
            }
          }
        }
        updatedFaces.put(faceDir, face);
      }
      updatedElements.add(new CuboidModelElement(element.from(), element.to(), updatedFaces, element.rotation(), element.shade(), element.lightEmission()));
    }

    Material.Baked fallbackParticle = resolved.resolveParticleMaterial(baseSlots, baker);
    Function<String, Material.Baked> materialGetter = name -> {
      String slot = name.startsWith("#") ? name.substring(1) : name;
      return resolveMaterial(slot, fallbackParticle);
    };

    QuadCollection.Builder builder = new QuadCollection.Builder();
    slimeknights.mantle.client.model.util.ModelHelper.bakeElements(baker, builder, updatedElements, colorData, materialGetter, modelState);
    return new SimpleModelWrapper(builder.build(), resolved.getTopAmbientOcclusion(), fallbackParticle);
  }

  private String findConnectedSlot(String slot) {
    if (connectedTextures.containsKey(slot)) {
      return slot;
    }
    return null;
  }

  private Material.Baked resolveMaterial(String slot, Material.Baked fallback) {
    Material.Baked cached = bakedTextureCache.get(slot);
    if (cached != null) {
      return cached;
    }

    // 1. Direct slot in baseSlots?
    Material material = baseSlots.getMaterial(slot);
    if (material != null) {
      Material.Baked baked = baker.materials().get(material, resolved);
      if (baked != null) {
        bakedTextureCache.put(slot, baked);
        return baked;
      }
    }

    // 2. Is this a suffixed slot, e.g. "all_u" or "pane_udlr"?
    int underscore = slot.lastIndexOf('_');
    if (underscore > 0) {
      String baseSlot = slot.substring(0, underscore);
      String suffix = slot.substring(underscore + 1);
      Material baseMat = baseSlots.getMaterial(baseSlot);
      if (baseMat != null) {
        Identifier baseId = baseMat.sprite();
        Identifier suffixedId = Identifier.fromNamespaceAndPath(baseId.getNamespace(), baseId.getPath() + "/" + suffix);
        Material suffixedMat = new Material(suffixedId, baseMat.forceTranslucent());
        Material.Baked baked = baker.materials().get(suffixedMat, resolved);
        if (baked != null) {
          bakedTextureCache.put(slot, baked);
          return baked;
        }
      }
    }

    return fallback;
  }

  private static String getTextureSuffix(String[] suffixes, byte connections, Function<Direction, Direction> transform) {
    int key = 0;
    for (Direction dir : Direction.Plane.HORIZONTAL) {
      int flag = 1 << transform.apply(dir).get3DDataValue();
      if ((connections & flag) == flag) {
        key |= 1 << dir.get2DDataValue();
      }
    }
    return suffixes[key];
  }

  private static Direction rotateDirection(Direction direction, Direction rotation) {
    if (rotation == Direction.UP) {
      return direction;
    }
    if (rotation == Direction.DOWN) {
      if (direction.getAxis() == Direction.Axis.Z) {
        return direction.getOpposite();
      }
      return direction;
    }
    return switch (direction) {
      case NORTH -> Direction.UP;
      case SOUTH -> Direction.DOWN;
      case EAST -> rotation.getCounterClockWise();
      case WEST -> rotation.getClockWise();
      default -> throw new IllegalArgumentException("Direction must be horizontal axis");
    };
  }

  private static Function<Direction, Direction> getTransform(Direction face, CuboidFace.UVs uvs, Quadrant rotation) {
    Function<Direction, Direction> transform = d -> rotateDirection(d, face);

    if (uvs != null) {
      boolean flipV = uvs.minV() > uvs.maxV();
      if (uvs.minU() > uvs.maxU()) {
        if (flipV) {
          transform = transform.compose(Direction::getOpposite);
        } else {
          transform = transform.compose(d -> d.getAxis() == Direction.Axis.X ? d.getOpposite() : d);
        }
      } else if (flipV) {
        transform = transform.compose(d -> d.getAxis() == Direction.Axis.Z ? d.getOpposite() : d);
      }
    }

    if (rotation != null && rotation != Quadrant.R0) {
      transform = switch (rotation) {
        case R90 -> transform.compose(Direction::getClockWise);
        case R180 -> transform.compose(Direction::getOpposite);
        case R270 -> transform.compose(Direction::getCounterClockWise);
        default -> transform;
      };
    }

    return transform;
  }

  @Override
  public Material.Baked particleMaterial() {
    return basePart.particleMaterial();
  }

  @Override
  public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
    return basePart.particleMaterial();
  }

  @Override
  @BakedQuad.MaterialFlags
  public int materialFlags() {
    return basePart.materialFlags();
  }

  @Override
  @BakedQuad.MaterialFlags
  public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
    return basePart.materialFlags();
  }

  private record GeometryKey(ConnectedBlockStateModel model, byte connections) {}

  public record Unbaked(Identifier model, int x, int y, boolean uvlock) implements CustomUnbakedBlockStateModel {
    public static final Identifier ID = Mantle.getResource("connected_block");
    public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model),
      Codec.INT.optionalFieldOf("x", 0).forGetter(Unbaked::x),
      Codec.INT.optionalFieldOf("y", 0).forGetter(Unbaked::y),
      Codec.BOOL.optionalFieldOf("uvlock", false).forGetter(Unbaked::uvlock)
    ).apply(instance, Unbaked::new));

    @Override
    public BlockStateModel bake(ModelBaker baker) {
      ResolvedModel resolved = baker.getModel(model);
      OctahedralGroup rotationGroup = Quadrant.fromXYAngles(quadrant(x), quadrant(y));
      BlockModelRotation rotation = BlockModelRotation.get(rotationGroup);
      ModelState state = uvlock ? rotation.withUvLock() : rotation;

      ConnectedModel connectedModel = findConnectedModel(resolved);
      if (connectedModel == null || connectedModel.connectedTextures().isEmpty()) {
        return new SingleVariant(SimpleModelWrapper.bake(baker, model, state));
      }

      List<CuboidModelElement> elements = findElements(resolved, connectedModel);
      Map<String, String[]> connectedTextures = connectedModel.connectedTextures();
      Set<Direction> sides = connectedModel.sides();
      BiPredicate<BlockState, BlockState> predicate = ConnectedModelRegistry.getPredicate(connectedModel.predicate());
      List<slimeknights.mantle.client.model.builder.ColorData> colorData = findColorData(resolved, connectedModel);

      return new ConnectedBlockStateModel(baker, resolved, state, rotationGroup, elements, connectedTextures, sides, predicate, colorData);
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
      resolver.markDependency(model);
    }

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
      return MAP_CODEC;
    }

    private static ConnectedModel findConnectedModel(ResolvedModel resolved) {
      for (ResolvedModel current = resolved; current != null; current = current.parent()) {
        if (current.wrapped() instanceof ConnectedModel model) {
          return model;
        }
      }
      return null;
    }

    private static List<CuboidModelElement> findElements(ResolvedModel resolved, ConnectedModel connectedModel) {
      if (!connectedModel.elements().isEmpty()) {
        return connectedModel.elements();
      }
      for (ResolvedModel current = resolved; current != null; current = current.parent()) {
        if (current.wrapped() instanceof ConnectedModel cm && !cm.elements().isEmpty()) {
          return cm.elements();
        }
        if (current.wrapped() instanceof ColoredBlockModel cbm && !cbm.elements().isEmpty()) {
          return cbm.elements();
        }
      }
      return List.of();
    }

    private static List<slimeknights.mantle.client.model.builder.ColorData> findColorData(ResolvedModel resolved, ConnectedModel connectedModel) {
      if (!connectedModel.colorData().isEmpty()) {
        return connectedModel.colorData();
      }
      for (ResolvedModel current = resolved; current != null; current = current.parent()) {
        if (current.wrapped() instanceof ConnectedModel cm && !cm.colorData().isEmpty()) {
          return cm.colorData();
        }
        if (current.wrapped() instanceof ColoredBlockModel cbm && !cbm.colorData().isEmpty()) {
          return cbm.colorData();
        }
      }
      return List.of();
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
