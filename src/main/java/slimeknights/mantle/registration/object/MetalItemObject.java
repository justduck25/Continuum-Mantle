package slimeknights.mantle.registration.object;

import lombok.Getter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Object wrapper containing ingots, nuggets, and blocks */
public class MetalItemObject extends ItemObject<Block> implements MultiObject<ItemLike> {
  private final Supplier<? extends Item> ingot;
  private final Supplier<? extends Item> nugget;
  @Getter
  private final TagKey<Block> blockTag;
  @Getter
  private final TagKey<Item> blockItemTag;
  @Getter
  private final TagKey<Item> ingotTag;
  @Getter
  private final TagKey<Item> nuggetTag;

  public MetalItemObject(String tagName, ItemObject<? extends Block> block, Supplier<? extends Item> ingot, Supplier<? extends Item> nugget) {
    super(block);
    this.ingot = ingot;
    this.nugget = nugget;
    this.blockTag = blockTag("storage_blocks/" + tagName);
    this.blockItemTag = getTag("storage_blocks/" + tagName);
    this.ingotTag = getTag("ingots/" + tagName);
    this.nuggetTag = getTag("nuggets/" + tagName);
  }

  /** Gets the ingot for this object */
  public Item getIngot() {
    return ingot.get();
  }

  /** Gets the ingot for this object */
  public Item getNugget() {
    return nugget.get();
  }

  /**
   * Creates a block tag for a common resource.
   * @param name  Tag name
   * @return  Tag
   */
  private static TagKey<Block> blockTag(String name) {
    return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", name));
  }

  /**
   * Creates an item tag for a common resource.
   * @param name  Tag name
   * @return  Tag
   */
  private static TagKey<Item> getTag(String name) {
    return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", name));
  }

  @Override
  public List<ItemLike> values() {
    return List.of(get(), getIngot(), getIngot());
  }

  @Override
  public void forEach(Consumer<? super ItemLike> consumer) {
    consumer.accept(get());
    consumer.accept(getIngot());
    consumer.accept(getNugget());
  }
}