package slimeknights.mantle.registration.object;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.mantle.util.RegistryHelper;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Registry object wrapper to implement {@link ItemLike}
 * @param <I>  Item class
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ItemObject<I extends ItemLike> implements Supplier<I>, ItemLike, IdAwareObject {
  private final Supplier<? extends I> entry;
  private final Identifier id;

  public ItemObject(DefaultedRegistry<I> registry, I entry) {
    this.entry = RegistryHelper.getHolder(registry, entry);
    this.id = registry.getKey(entry);
  }

  public ItemObject(DeferredHolder object) {
    this.entry = object;
    this.id = object.getId();
  }

  protected ItemObject(ItemObject<? extends I> object) {
    this.entry = object.entry;
    this.id = object.id;
  }

  @Override
  public I get() {
    return Objects.requireNonNull(entry.get(), () -> "Item Object not present " + id);
  }

  @Nullable
  public I getOrNull() {
    try {
      return entry.get();
    } catch (NullPointerException e) {
      return null;
    }
  }

  @Override
  public Identifier getId() {
    return id;
  }

  @Override
  public Item asItem() {
    return get().asItem();
  }
}