package slimeknights.mantle.fluid.texture;

import com.google.gson.JsonElement;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Manager for handling fluid textures */
public class FluidTextureManager extends SimpleJsonResourceReloadListener<JsonElement> {
  public static final String FOLDER = "mantle/fluid_texture";

  private static final FluidTextureManager INSTANCE = new FluidTextureManager();
  private static final FluidTexture FALLBACK = new FluidTexture(Identifier.withDefaultNamespace("block/water_still"), Identifier.withDefaultNamespace("block/water_flow"), null, null, 0, -1, -1, false, 0, 0);

  private Map<FluidType,FluidTexture> textures = Collections.emptyMap();

  private FluidTextureManager() {
    super(JsonHelper.JSON_ELEMENT_CODEC, FileToIdConverter.json(FOLDER));
  }

  public static void init(AddClientReloadListenersEvent event) {
    event.addListener(Mantle.getResource("fluid_texture"), INSTANCE);
  }

  @Override
  protected void apply(Map<Identifier,JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
    long time = System.nanoTime();
    Map<FluidType, FluidTexture> map = new HashMap<>();
    var fluidTypeRegistry = NeoForgeRegistries.FLUID_TYPES;

    for (Map.Entry<Identifier,JsonElement> entry : jsons.entrySet()) {
      Identifier id = entry.getKey();
      FluidType type = fluidTypeRegistry.getValue(id);
      if (type == null || !id.equals(fluidTypeRegistry.getKey(type))) {
        Mantle.logger.debug("Ignoring fluid texture {} as no fluid type exists with that name", id);
      } else {
        map.put(type, FluidTexture.deserialize(GsonHelper.convertToJsonObject(entry.getValue(), "fluid_texture")));
      }
    }
    this.textures = map;
    Mantle.logger.info("Loaded {} fluid textures in {} ms", map.size(), (System.nanoTime() - time) / 1000000f);
  }

  public static FluidTexture getData(FluidType fluid) {
    return INSTANCE.textures.getOrDefault(fluid, FALLBACK);
  }

  public static Identifier getStillTexture(FluidType fluid) {
    return getData(fluid).still();
  }

  public static Identifier getFlowingTexture(FluidType fluid) {
    return getData(fluid).flowing();
  }

  @Nullable
  public static Identifier getOverlayTexture(FluidType fluid) {
    return getData(fluid).overlay();
  }

  @Nullable
  public static Identifier getCameraTexture(FluidType fluid) {
    return getData(fluid).camera();
  }

  public static int getColor(FluidType fluid) {
    return getData(fluid).color();
  }
}