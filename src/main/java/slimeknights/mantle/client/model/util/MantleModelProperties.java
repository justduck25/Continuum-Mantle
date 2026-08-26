package slimeknights.mantle.client.model.util;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import slimeknights.mantle.Mantle;

/** Context keys used to carry Mantle custom model metadata through NeoForge's model pipeline. */
public final class MantleModelProperties {
  public static final ContextKey<JsonObject> CUSTOM_DATA = key("custom_data");

  private MantleModelProperties() {}

  private static <T> ContextKey<T> key(String name) {
    Identifier id = Mantle.getResource(name);
    return new ContextKey<>(id);
  }
}