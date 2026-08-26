package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import slimeknights.mantle.data.loadable.primitive.IdentifierLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Common logic for {@link RegistryLoadable} and {@link LazyRegistryLoadable} */
public interface BaseRegistryLoadable<T> extends IdentifierLoadable<T> {
  Map<Object, Identifier> KEY_CACHE = new ConcurrentHashMap<>();

  /** Gets the registry associated with this loadable. Null if the registry cannot be located */
  @Nullable
  Registry<T> registry();

  /** Gets the ID of this registry for error messages */
  Identifier registryId();

  /** Registers a custom key mapping for an object that may not be in the registry */
  static void registerKey(Object object, Identifier id) {
    KEY_CACHE.put(object, id);
  }

  @Override
  default T fromKey(Identifier name, String key, TypedMap context) {
    Registry<T> registry = registry();
    if (registry != null && registry.containsKey(name)) {
      T value = registry.get(name).map(reference -> reference.value()).orElse(null);
      if (value != null) {
        return value;
      }
    }
    throw new JsonSyntaxException("Unable to parse " + key + " as registry " + registryId() + " does not contain ID " + name);
  }

  @Override
  default Identifier getKey(T object) {
    Identifier cachedKey = KEY_CACHE.get(object);
    if (cachedKey != null) {
      return cachedKey;
    }
    Registry<T> registry = registry();
    if (registry != null) {
      Identifier location = registry.getKey(object);
      if (location != null) {
        return location;
      }
    }
    throw new RuntimeException("Registry " + registryId() + " does not contain object " + object);
  }

  @Override
  default T decode(FriendlyByteBuf buffer, TypedMap context) {
    int id = buffer.readVarInt();
    Registry<T> registry = registry();
    if (registry != null) {
      T value = registry.byId(id);
      if (value != null) {
        return value;
      }
    }
    throw new DecoderException("Registry " + registryId() + " does not contain ID " + id);
  }

  @Override
  default void encode(FriendlyByteBuf buffer, T object) {
    Registry<T> registry = registry();
    if (registry == null) {
      throw new EncoderException("Registry " + registryId() + " cannot be located");
    }
    int id = registry.getId(object);
    if (id < 0) {
      throw new EncoderException("Registry " + registryId() + " does not contain object " + object);
    }
    buffer.writeVarInt(id);
  }
}

