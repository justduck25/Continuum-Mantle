package slimeknights.mantle.data.predicate.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.mantle.util.typed.TypedMap;

public record HasEnchantmentEntityPredicate(Enchantment enchantment) implements LivingEntityPredicate {
  private static final Loadable<Enchantment> ENCHANTMENT_LOADABLE = new Loadable<>() {
    @Override
    public Enchantment convert(JsonElement element, String key, TypedMap context) {
      return Loadables.ENCHANTMENT.convert(element, key, context);
    }
    @Override
    public JsonElement serialize(Enchantment object) {
      Registry<Enchantment> reg = RegistryHelper.getRegistry(Registries.ENCHANTMENT);
      if (reg != null) {
        Identifier id = reg.getKey(object);
        if (id != null) {
          return new JsonPrimitive(id.toString());
        }
      }
      try {
        return Loadables.ENCHANTMENT.serialize(object);
      } catch (RuntimeException e) {
        return new JsonPrimitive("minecraft:air");
      }
    }
    @Override
    public Enchantment decode(FriendlyByteBuf buf, TypedMap context) {
      return Loadables.ENCHANTMENT.decode(buf, context);
    }
    @Override
    public void encode(FriendlyByteBuf buf, Enchantment value) {
      Loadables.ENCHANTMENT.encode(buf, value);
    }
  };

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
