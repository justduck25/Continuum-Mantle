package slimeknights.mantle.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

/**
 * Loads the first model from a list of models whose optional dependency is present.
 */
public enum FallbackModelLoader implements UnbakedModelLoader<UnbakedModel> {
  INSTANCE;

  @Override
  public UnbakedModel read(JsonObject data, JsonDeserializationContext context) {
    JsonArray models = GsonHelper.getAsJsonArray(data, "models");
    if (models.size() < 2) {
      throw new JsonSyntaxException("Fallback model must contain at least 2 models");
    }

    for (int i = 0; i < models.size(); i++) {
      String debugName = "models[" + i + "]";
      JsonObject entry = GsonHelper.convertToJsonObject(models.get(i), debugName);
      String modId = null;
      if (entry.has("fallback_mod_id")) {
        modId = GsonHelper.getAsString(entry, "fallback_mod_id");
      } else if (entry.has("loader")) {
        modId = Identifier.parse(GsonHelper.getAsString(entry, "loader")).getNamespace();
      }

      if (modId == null || ModList.get().isLoaded(modId)) {
        try {
          return context.deserialize(entry, UnbakedModel.class);
        } catch (JsonSyntaxException e) {
          throw new JsonSyntaxException("Failed to parse fallback model " + debugName, e);
        }
      }
    }

    throw new JsonSyntaxException("Failed to load fallback model, all " + models.size() + " variants had a failed condition");
  }
}