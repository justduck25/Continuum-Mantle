package slimeknights.mantle.client.model.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.DelegateUnbakedModel;
import net.neoforged.neoforge.client.model.EmptyModel;

/** Utilities for NeoForge 26 custom model loader compatibility. */
public final class ModelLoaderHelper {
  private ModelLoaderHelper() {}

  public static UnbakedModel delegate(JsonObject json, com.google.gson.JsonDeserializationContext context, String... customKeys) {
    JsonObject copy = json.deepCopy();
    JsonObject metadata = new JsonObject();
    copy.remove("loader");
    for (String key : customKeys) {
      JsonElement removed = copy.remove(key);
      if (removed != null) {
        metadata.add(key, removed.deepCopy());
      }
    }
    UnbakedModel delegate = !copy.has("parent") && !copy.has("elements") && !copy.has("textures")
      ? EmptyModel.INSTANCE
      : context.deserialize(copy, UnbakedModel.class);
    return metadata.size() == 0 ? delegate : new MetadataModel(delegate, metadata);
  }

  private static class MetadataModel extends DelegateUnbakedModel {
    private final JsonObject metadata;

    private MetadataModel(UnbakedModel delegate, JsonObject metadata) {
      super(delegate);
      this.metadata = metadata;
    }

    @Override
    public void fillAdditionalProperties(ContextMap.Builder propertiesBuilder) {
      super.fillAdditionalProperties(propertiesBuilder);
      propertiesBuilder.withParameter(MantleModelProperties.CUSTOM_DATA, this.metadata.deepCopy());
    }
  }
}