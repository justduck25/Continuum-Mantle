package slimeknights.mantle.client.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.RetexturedHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Dynamic item model for stacks storing Mantle retextured block data. */
public class RetexturedItemModel implements ItemModel {
  private final Unbaked unbaked;
  private final ItemModel.BakingContext context;
  private final Matrix4fc transformation;
  private final Map<Block, ItemModel> cache = new HashMap<>();

  private RetexturedItemModel(Unbaked unbaked, ItemModel.BakingContext context, Matrix4fc transformation) {
    this.unbaked = unbaked;
    this.context = context;
    this.transformation = transformation;
  }

  @Override
  public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
    Block texture = RetexturedHelper.getTexture(stack);
    if (texture == Blocks.AIR) {
      texture = null;
    }
    cache.computeIfAbsent(texture, this::bakeForTexture).update(output, stack, resolver, displayContext, level, owner, seed);
  }

  private ItemModel bakeForTexture(@Nullable Block texture) {
    ModelBaker baker = context.blockModelBaker();
    ResolvedModel resolved = baker.getModel(unbaked.model);
    RetexturedModel retexturedModel = findRetexturedModel(resolved);
    Set<String> retextured = retexturedModel == null ? Set.of() : retexturedModel.retexturedNames();
    TextureSlots textureSlots = resolved.getTopTextureSlots();
    QuadCollection quads;
    if (texture != null && retexturedModel != null && !retextured.isEmpty() && !retexturedModel.elements().isEmpty()) {
      Material.Baked retexturedSide = RetexturedBlockStateModel.bakedTextureMaterial(texture, false);
      Material.Baked retexturedEnd = RetexturedBlockStateModel.bakedTextureMaterial(texture, true);
      java.util.function.Function<String, Material.Baked> materialGetter = name -> {
        String lookup = name.startsWith("#") ? name.substring(1) : name;
        if (retextured.contains(lookup)) {
          return RetexturedBlockStateModel.isEndTextureSlot(lookup) ? retexturedEnd : retexturedSide;
        }
        Material material = textureSlots.getMaterial(lookup);
        return material != null ? baker.materials().get(material, resolved) : null;
      };
      QuadCollection.Builder builder = new QuadCollection.Builder();
      UnbakedElementsHelper.bakeElements(baker, builder, retexturedModel.elements(), materialGetter, BlockModelRotation.IDENTITY);
      quads = builder.build();
    } else {
      quads = resolved.bakeTopGeometry(textureSlots, baker, BlockModelRotation.IDENTITY);
    }
    ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolved, textureSlots);
    return new CuboidItemModelWrapper(List.of(), quads, properties, transformation);
  }

  private static RetexturedModel findRetexturedModel(ResolvedModel resolved) {
    for (ResolvedModel current = resolved; current != null; current = current.parent()) {
      if (current.wrapped() instanceof RetexturedModel model) {
        return model;
      }
    }
    return null;
  }

  public record Unbaked(Identifier model) implements ItemModel.Unbaked {
    public static final Identifier ID = Mantle.getResource("retextured_item");
    public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model)
    ).apply(instance, Unbaked::new));

    @Override
    public MapCodec<? extends ItemModel.Unbaked> type() {
      return MAP_CODEC;
    }

    @Override
    public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
      return new RetexturedItemModel(this, context, transformation);
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
      resolver.markDependency(model);
    }
  }
}