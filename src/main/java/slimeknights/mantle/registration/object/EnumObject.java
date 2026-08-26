package slimeknights.mantle.registration.object;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Represents an object which is a map of an enum to entry
 * @param <T>  Enum type
 * @param <I>  Entry type
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class EnumObject<T extends Enum<T>, I> implements MultiObject<I> {
  @SuppressWarnings({"rawtypes", "unchecked"})
  private static final EnumObject EMPTY = new EnumObject(Collections.emptyMap());

  private final Map<T,Supplier<? extends I>> map;

  protected EnumObject(Map<T, Supplier<? extends I>> map) {
    this.map = map;
  }

  @Nullable
  public Supplier<? extends I> getSupplier(T value) {
    return map.get(value);
  }

  public I get(T value) {
    Supplier<? extends I> supplier = map.get(value);
    if (supplier == null) {
      throw new NoSuchElementException("Missing key " + value);
    }
    return Objects.requireNonNull(supplier.get(), () -> "No enum object value for " + value);
  }

  @Nullable
  public I getOrNull(T value) {
    Supplier<? extends I> supplier = map.get(value);
    if (supplier == null) {
      return null;
    }
    try {
      return supplier.get();
    } catch (NullPointerException e) {
      return null;
    }
  }

  public boolean contains(Object value) {
    return this.map.values().stream().map(Supplier::get).anyMatch(value::equals);
  }

  public Collection<T> keys() {
    return this.map.keySet();
  }

  public Collection<Entry<T,Supplier<? extends I>>> entries() {
    return this.map.entrySet();
  }

  @Override
  public List<I> values() {
    return this.map.values().stream().map(Supplier::get).filter(Objects::nonNull).collect(Collectors.toList());
  }

  public void forEach(BiConsumer<T, ? super I> consumer) {
    this.map.forEach((key, sup) -> {
      I value;
      try {
        value = sup.get();
      } catch (NullPointerException e) {
        return;
      }
      if (value != null) {
        consumer.accept(key, value);
      }
    });
  }

  @Override
  public void forEach(Consumer<? super I> consumer) {
    forEach((k, v) -> consumer.accept(v));
  }

  @SuppressWarnings("unchecked")
  public static <T extends Enum<T>, I> EnumObject<T,I> empty() {
    return (EnumObject<T,I>) EMPTY;
  }

  @SuppressWarnings({"UnusedReturnValue", "unused"})
  public static class Builder<T extends Enum<T>, I> {
    private final Map<T, Supplier<? extends I>> map;

    public Builder(Class<T> clazz) {
      this.map = new EnumMap<>(clazz);
    }

    public Builder<T,I> put(T key, Supplier<? extends I> value) {
      this.map.put(key, value);
      return this;
    }

    public Builder<T,I> put(T key, I value) {
      this.map.put(key, () -> value);
      return this;
    }

    public Builder<T,I> putAll(Map<T, Supplier<? extends I>> map) {
      this.map.putAll(map);
      return this;
    }

    public Builder<T,I> putAll(EnumObject<T,? extends I> object) {
      this.map.putAll(object.map);
      return this;
    }

    public EnumObject<T,I> build() {
      return new EnumObject<>(map);
    }
  }
}