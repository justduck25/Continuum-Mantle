package slimeknights.mantle.util;

import net.minecraft.resources.Identifier;

/** @deprecated use new utilities from {@link Identifier} */
@Deprecated(forRemoval = true)
public interface IdExtender<T extends Identifier> {
  /** Extender for standard resource locations */
  LocationExtender INSTANCE = new LocationExtender() {};

  /** Creates a resource location */
  T location(String namespace, String path);

  /** @deprecated use {@link JsonHelper#wrap(Identifier, String, String)} */
  @Deprecated(forRemoval = true)
  default T wrap(Identifier location, String prefix, String suffix) {
    return location(location.getNamespace(), prefix + location.getPath() + suffix);
  }

  /** @deprecated use {@link Identifier#withPrefix(String)} */
  @Deprecated(forRemoval = true)
  default T prefix(Identifier location, String prefix) {
    return location(location.getNamespace(), prefix + location.getPath());
  }

  /** @deprecated use {@link Identifier#withSuffix(String)} */
  @Deprecated(forRemoval = true)
  default T suffix(Identifier location, String suffix) {
    return location(location.getNamespace(), location.getPath() + suffix);
  }

  /** Extender for specifically resource locations, used in recipe helpers */
  interface LocationExtender extends IdExtender<Identifier> {
    @Override
    default Identifier location(String namespace, String path) {
      return Identifier.fromNamespaceAndPath(namespace, path);
    }
  }
}
