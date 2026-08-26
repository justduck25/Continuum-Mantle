package slimeknights.mantle.fluid.tooltip;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.mantle.data.gson.TagKeySerializer;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/** Provider for fluid tooltip information */
@SuppressWarnings({"unused", "SameParameterValue"})  // API
public abstract class AbstractFluidTooltipProvider extends GenericDataProvider {
  /** Folder for saving the logic */
  public static final String FOLDER = "mantle/fluid_tooltips";
  /** GSON instance matching the runtime fluid tooltip loader. */
  public static final Gson GSON = (new GsonBuilder())
    .registerTypeAdapter(Identifier.class, slimeknights.mantle.data.gson.IdentifierSerializer.resourceLocation(slimeknights.mantle.Mantle.modId))
    .registerTypeAdapter(FluidIngredient.class, FluidIngredient.LOADABLE)
    .registerTypeAdapter(TagKey.class, new TagKeySerializer<>(Registries.FLUID))
    .setPrettyPrinting()
    .disableHtmlEscaping()
    .create();
  private final Map<Identifier,Identifier> redirects = new HashMap<>();
  private final Map<Identifier,FluidUnitListBuilder> builders = new HashMap<>();
  private final String modId;

  public AbstractFluidTooltipProvider(PackOutput output, String modId) {
    super(output, Target.RESOURCE_PACK, FOLDER, GSON);
    this.modId = modId;
  }

  /** Adds all relevant fluids to the maps */
  protected abstract void addFluids();

  @Override
  public final CompletableFuture<?> run(CachedOutput cache) {
    addFluids();
    return allOf(Stream.concat(
      builders.entrySet().stream().map(entry -> saveJson(cache, entry.getKey(), entry.getValue().build())),
      redirects.entrySet().stream().map(entry -> {
      JsonObject json = new JsonObject();
      json.addProperty("redirect", entry.getValue().toString());
      return saveJson(cache, entry.getKey(), json);
    })));
  }


  /* Helpers */

  /** Creates a Identifier for the local mod */
  protected Identifier id(String name) {
    return Identifier.fromNamespaceAndPath(modId, name);
  }

  /** Adds a fluid to the builder */
  protected FluidUnitListBuilder add(Identifier id, @Nullable TagKey<Fluid> tag) {
    if (redirects.containsKey(id)) {
      throw new IllegalArgumentException(id + " is already registered as a redirect");
    }
    FluidUnitListBuilder newBuilder = new FluidUnitListBuilder(tag);
    FluidUnitListBuilder original = builders.put(id, newBuilder);
    if (original != null) {
      throw new IllegalArgumentException(id + " is already registered");
    }
    return newBuilder;
  }

  /** Adds a fluid to the builder */
  protected FluidUnitListBuilder add(String id, TagKey<Fluid> tag) {
    return add(id(id), tag);
  }

  /** Adds a fluid to the builder using the tag name as the ID */
  protected FluidUnitListBuilder add(TagKey<Fluid> tag) {
    return add(id(tag.location().getPath()), tag);
  }

  /** Adds a fluid to the builder with no tag */
  protected FluidUnitListBuilder add(Identifier id) {
    return add(id, null);
  }

  /** Adds a fluid to the builder with no tag */
  protected FluidUnitListBuilder add(String id) {
    return add(id(id), null);
  }

  /** Adds a redirect from a named builder to a target */
  protected void addRedirect(Identifier id, Identifier target) {
    if (builders.containsKey(id)) {
      throw new IllegalArgumentException(id + " is already registered as a unit list");
    }
    Identifier original = redirects.put(id, target);
    if (original != null) {
      throw new IllegalArgumentException(id + " is already redirecting to " + original);
    }
  }

  /** Builder for a unit list */
  @SuppressWarnings("unused")
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  protected class FluidUnitListBuilder {
    @Nullable
    private final TagKey<Fluid> tag;
    private final List<FluidUnit> units = new ArrayList<>();

    /** Adds a unit with a full translation key */
    public FluidUnitListBuilder addUnitRaw(String key, int amount) {
      units.add(new FluidUnit(key, amount));
      return this;
    }

    /** Adds a unit local to the current mod */
    public FluidUnitListBuilder addUnit(String key, int amount) {
      return addUnitRaw(Util.makeDescriptionId("gui", id("fluid." + key)), amount);
    }

    /** Adds a unit local to the given mod */
    public FluidUnitListBuilder addUnit(String key, String domain, int amount) {
      return addUnitRaw(Util.makeDescriptionId("gui", Identifier.fromNamespaceAndPath(domain, "fluid." + key)), amount);
    }

    /** Builds the final instance */
    private FluidUnitList build() {
      return new FluidUnitList(tag, List.copyOf(units));
    }
  }
}
