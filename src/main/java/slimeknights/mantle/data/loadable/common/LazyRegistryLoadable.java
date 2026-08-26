package slimeknights.mantle.data.loadable.common;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.RegistryHelper;

import javax.annotation.Nullable;

@RequiredArgsConstructor
public class LazyRegistryLoadable<T> implements BaseRegistryLoadable<T> {
  private final ResourceKey<? extends Registry<T>> registryKey;
  @Nullable
  private Registry<T> registry;

  @Nullable
  @Override
  public Registry<T> registry() {
    if (registry == null) {
      registry = RegistryHelper.getRegistry(registryKey);
      if (registry == null) {
        Mantle.logger.error("Registry {} cannot be located, treating as an empty registry. This may cause serialization issues down the line.", registryKey);
      }
    }
    return registry;
  }

  @Override
  public Identifier registryId() {
    return registryKey.identifier();
  }
}

