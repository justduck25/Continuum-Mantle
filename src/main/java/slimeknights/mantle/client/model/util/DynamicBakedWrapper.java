package slimeknights.mantle.client.model.util;

/** Legacy compatibility shim for the removed BakedModel wrapper API. */
@Deprecated(forRemoval = false)
public abstract class DynamicBakedWrapper<T> {
  protected final T originalModel;
  protected DynamicBakedWrapper(T originalModel) {
    this.originalModel = originalModel;
  }
}