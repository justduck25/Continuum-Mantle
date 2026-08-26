package slimeknights.mantle.client.model.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Builder for {@link slimeknights.mantle.client.model.FallbackModelLoader} */
public class FallbackModelBuilder extends CustomLoaderBuilder {
  private final List<DomainModel> models = new ArrayList<>();

  public FallbackModelBuilder() {
    this(Mantle.getResource("fallback"), false);
  }

  public FallbackModelBuilder(Identifier loaderId, boolean allowInlineElements) {
    super(loaderId, allowInlineElements);
  }

  /** Adds a fallback model with a domain restriction */
  public FallbackModelBuilder fallback(JsonObject modelJson, @Nullable String modId) {
    this.models.add(new DomainModel(modelJson, modId));
    return this;
  }

  /** Adds a fallback model using the loader ID as the domain restriction */
  public FallbackModelBuilder fallback(JsonObject modelJson) {
    return fallback(modelJson, null);
  }

  @Override
  protected CustomLoaderBuilder copyInternal() {
    FallbackModelBuilder copy = new FallbackModelBuilder(loaderId, allowInlineElements);
    for (DomainModel dm : this.models) {
      copy.models.add(new DomainModel(dm.modelJson.deepCopy(), dm.domain));
    }
    return copy;
  }

  @Override
  public JsonObject toJson(JsonObject json) {
    json = super.toJson(json);
    if (this.models.size() < 2) {
      throw new IllegalStateException("Must have at least two models to use the fallback loader");
    }
    JsonArray fallbacks = new JsonArray();
    for (DomainModel builder : models) {
      fallbacks.add(builder.toJson());
    }
    json.add("models", fallbacks);
    return json;
  }

  /** Builder with an optional domain restriction */
  private record DomainModel(JsonObject modelJson, @Nullable String domain) {
    /** Converts this to JSON */
    public JsonObject toJson() {
      JsonObject json = modelJson.deepCopy();
      if (domain != null) {
        json.addProperty("fallback_mod_id", domain);
      }
      return json;
    }
  }
}
