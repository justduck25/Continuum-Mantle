package slimeknights.mantle.data.predicate.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;

public record HasEnchantmentEntityPredicate(Enchantment enchantment) implements LivingEntityPredicate {
  private static final String KEY = "enchantment";
  private static final Loadable<Enchantment> ENCHANTMENT_LOADABLE = new Loadable<>() {
    @Override
    public Enchantment convert(JsonElement element, String key, TypedMap context) {
      return Loadables.ENCHANTMENT.convert(element, key, context);
    }
    @Override
    public JsonElement serialize(Enchantment object) {
      return new JsonPrimitive(getKey(object).toString());
    }
    @Override
    public Enchantment decode(FriendlyByteBuf buf, TypedMap context) {
      Identifier id = buf.readIdentifier();
      if (buf instanceof RegistryFriendlyByteBuf registryBuffer) {
        return registryBuffer.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
          .get(ResourceKey.create(Registries.ENCHANTMENT, id))
          .map(Holder::value)
          .orElseThrow(() -> new DecoderException("Registry " + Registries.ENCHANTMENT.identifier() + " does not contain ID " + id));
      }
      return Loadables.ENCHANTMENT.parseString(id.toString(), KEY, context);
    }
    @Override
    public void encode(FriendlyByteBuf buf, Enchantment value) {
      buf.writeIdentifier(getKey(value));
    }
  };

  private static Identifier getKey(Enchantment enchantment) {
    Registry<Enchantment> registry = RegistryHelper.getRegistry(Registries.ENCHANTMENT);
    if (registry != null) {
      Identifier id = registry.getKey(enchantment);
      if (id != null) {
        return id;
      }
    }
    var access = RegistryHelper.getFallbackRegistryAccess();
    if (access != null) {
      Identifier id = access.lookupOrThrow(Registries.ENCHANTMENT).listElements()
        .filter(holder -> holder.value() == enchantment)
        .findFirst()
        .map(holder -> holder.key().identifier())
        .orElse(null);
      if (id != null) {
        return id;
      }
      id = getKeyFromTranslation(enchantment);
      if (id != null && access.lookupOrThrow(Registries.ENCHANTMENT).get(ResourceKey.create(Registries.ENCHANTMENT, id)).isPresent()) {
        return id;
      }
    }
    Identifier id = getKeyFromTranslation(enchantment);
    if (id != null) {
      return id;
    }
    try {
      return Loadables.ENCHANTMENT.getKey(enchantment);
    } catch (RuntimeException e) {
      throw new EncoderException("Registry " + Registries.ENCHANTMENT.identifier() + " does not contain object " + enchantment, e);
    }
  }

  /**
   * Enchantments are data-driven, so during packet encoding we can occasionally
   * see an equal-looking value that is not the same object as the active
   * registry entry. The vanilla translation key still preserves the ID.
   */
  @Nullable
  private static Identifier getKeyFromTranslation(Enchantment enchantment) {
    if (enchantment.description().getContents() instanceof TranslatableContents contents) {
      String key = contents.getKey();
      if (key.startsWith("enchantment.")) {
        String id = key.substring("enchantment.".length());
        int namespaceEnd = id.indexOf('.');
        if (namespaceEnd > 0 && namespaceEnd + 1 < id.length()) {
          return Identifier.fromNamespaceAndPath(id.substring(0, namespaceEnd), id.substring(namespaceEnd + 1));
        }
      }
    }
    return null;
  }

  public static final RecordLoadable<HasEnchantmentEntityPredicate> LOADER = RecordLoadable.create(ENCHANTMENT_LOADABLE.requiredField("enchantment", HasEnchantmentEntityPredicate::enchantment), HasEnchantmentEntityPredicate::new);

  @Override
  public boolean matches(LivingEntity entity) {
    Registry<Enchantment> registry = RegistryHelper.getRegistry(Registries.ENCHANTMENT);
    return registry != null && EnchantmentHelper.getEnchantmentLevel(registry.wrapAsHolder(enchantment), entity) > 0;
  }

  @Override
  public RecordLoadable<HasEnchantmentEntityPredicate> getLoader() {
    return LOADER;
  }
}
