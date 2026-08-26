package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.mapping.EnumMapLoadable;
import slimeknights.mantle.data.loadable.primitive.IdentifierLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.Arrays;
import java.util.Map;

/** Special loadable for display contexts, which are an extensible enum rather than a registry. */
public enum DisplayContextLoadable implements IdentifierLoadable<ItemDisplayContext> {
  INSTANCE;

  @Override
  public ItemDisplayContext fromKey(Identifier name, String key, TypedMap context) {
    return Arrays.stream(ItemDisplayContext.values())
      .filter(value -> value.getSerializedName().equals(name.getPath()) || value.getSerializedName().equals(name.toString()))
      .findFirst()
      .orElseThrow(() -> new JsonSyntaxException("Unable to parse " + key + " as an ItemDisplayContext: " + name));
  }

  @Override
  public Identifier getKey(ItemDisplayContext object) {
    String name = object.getSerializedName();
    return name.indexOf(':') >= 0 ? Identifier.parse(name) : Identifier.withDefaultNamespace(name);
  }

  @Override
  public ItemDisplayContext decode(FriendlyByteBuf buffer, TypedMap context) {
    int id = buffer.readVarInt();
    ItemDisplayContext value = ItemDisplayContext.BY_ID.apply(id);
    if (value == null) {
      throw new io.netty.handler.codec.DecoderException("Unknown ItemDisplayContext ID " + id);
    }
    return value;
  }

  @Override
  public void encode(FriendlyByteBuf buffer, ItemDisplayContext value) {
    buffer.writeVarInt(value.getId());
  }

  @Override
  public <V> Loadable<Map<ItemDisplayContext,V>> mapWithValues(Loadable<V> valueLoadable, int minSize) {
    return new EnumMapLoadable<>(ItemDisplayContext.class, this, valueLoadable, minSize);
  }
}
