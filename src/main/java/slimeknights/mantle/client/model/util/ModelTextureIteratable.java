package slimeknights.mantle.client.model.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/** Legacy compatibility shim; NeoForge 26 exposes texture slots through the vanilla model pipeline. */
@Deprecated(forRemoval = false)
public final class ModelTextureIteratable implements Iterable<Map<String, ?>> {
  @Override
  public Iterator<Map<String, ?>> iterator() {
    return Collections.<Map<String, ?>>emptyList().iterator();
  }
}