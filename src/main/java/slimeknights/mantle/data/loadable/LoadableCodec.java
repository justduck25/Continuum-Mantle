package slimeknights.mantle.data.loadable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.Identifier;
import slimeknights.mantle.data.JsonCodec;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Implementation of a codec using a loadable. Note this will be inefficient comparatively when using {@link net.minecraft.nbt.NbtOps} */
public record LoadableCodec<T>(Loadable<T> loadable, TypedMap context) implements JsonCodec<T> {
  /** JSON key used to pass a recipe ID through the vanilla 1.21 codec path. */
  private static final String ID_KEY = "id";

  public LoadableCodec(Loadable<T> loadable) {
    this(loadable, TypedMap.EMPTY);
  }

  @Override
  public T deserialize(JsonElement element, DynamicOps<?> ops) {
    return loadable.convert(element, "codec", contextWithJsonId(element));
  }

  @Override
  public JsonElement serialize(T object, DynamicOps<?> ops) {
    JsonElement element = loadable.serialize(object);
    Identifier id = getId(object);
    if (id != null && element instanceof JsonObject json && !json.has(ID_KEY)) {
      json.addProperty(ID_KEY, id.toString());
    }
    return element;
  }

  /** Adds the serialized ID to context, as 1.21 recipe codecs no longer receive the recipe key directly. */
  private TypedMap contextWithJsonId(JsonElement element) {
    if (!context.containsKey(ContextKey.ID) && element instanceof JsonObject json && json.has(ID_KEY)) {
      return TypedMapBuilder.builder().putAll(context).put(ContextKey.ID, Identifier.parse(json.get(ID_KEY).getAsString())).build();
    }
    return context;
  }

  /** Attempts to fetch an ID from legacy Mantle recipe objects. */
  private static Identifier getId(Object object) {
    try {
      Method method = object.getClass().getMethod("getId");
      Object value = method.invoke(object);
      if (value instanceof Identifier id) {
        return id;
      }
    } catch (ReflectiveOperationException ignored) {}

    try {
      Field field = object.getClass().getDeclaredField(ID_KEY);
      field.setAccessible(true);
      Object value = field.get(object);
      if (value instanceof Identifier id) {
        return id;
      }
    } catch (ReflectiveOperationException ignored) {}

    return null;
  }
}