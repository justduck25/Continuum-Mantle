package slimeknights.mantle.data.registry;

import net.minecraft.resources.Identifier;
import slimeknights.mantle.registration.object.IdAwareObject;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic registry of a {@link IdAwareObject}.
 * @param <T> Type of the component being registered.
 */
public class IdAwareComponentRegistry<T extends IdAwareObject> extends AbstractNamedComponentRegistry<T> {
  /** Registered box expansion types */
  private final Map<Identifier,T> values = new HashMap<>();

  public IdAwareComponentRegistry(String errorText) {
    super(errorText);
  }

  /** Registers the value with the given name */
  public synchronized <V extends T> V register(V value) {
    Identifier name = value.getId();
    if (values.putIfAbsent(name, value) != null) {
      throw new IllegalArgumentException("Duplicate registration " + name);
    }
    return value;
  }

  /** Gets a value or null if missing */
  @Override
  @Nullable
  public T getValue(Identifier name) {
    return values.get(name);
  }

  @Override
  public Identifier getKey(T object) {
    return object.getId();
  }

  @Override
  public Collection<Identifier> getKeys() {
    return values.keySet();
  }

  @Override
  public Collection<T> getValues() {
    return values.values();
  }
}
