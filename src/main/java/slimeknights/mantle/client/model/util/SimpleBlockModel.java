package slimeknights.mantle.client.model.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.model.UnbakedModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

/** NeoForge 26 compatible vanilla-delegating block model loader. */
public final class SimpleBlockModel {
  public static final UnbakedModelLoader<UnbakedModel> LOADER = SimpleBlockModel::deserialize;

  private SimpleBlockModel() {}

  public static UnbakedModel deserialize(JsonObject json, JsonDeserializationContext context) {
    return ModelLoaderHelper.delegate(json, context);
  }
}