package slimeknights.mantle.registration.deferred;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.mantle.registration.object.EnumObject;

import java.util.Locale;
import java.util.function.Supplier;

/** Helper for registering potions. */
@SuppressWarnings("unused")
public class PotionDeferredRegister extends DeferredRegisterWrapper<Potion> {
  public PotionDeferredRegister(String modID) {
    super(Registries.POTION, modID);
  }

  public DeferredHolder register(String name, Supplier<Potion> potion) {
    return register.register(name, potion);
  }

  public Builder registerTypes(String name, Supplier<? extends MobEffect> effect, int duration, int amplifier) {
    return new Builder(name, effect, duration, amplifier);
  }

  public Builder registerTypes(DeferredHolder effect, int duration, int amplifier) {
    return new Builder(effect.getId().getPath(), effect, duration, amplifier);
  }

  public Builder registerTypes(DeferredHolder effect) {
    return registerTypes(effect, 3 * 60 * 20, 0);
  }

  public enum PotionType {
    NORMAL,
    LONG,
    STRONG
  }

  public class Builder {
    private final EnumObject.Builder<PotionType,Potion> builder;
    private final String name;
    private final Supplier<? extends MobEffect> effect;
    private final int duration;
    private final int amplifier;

    private Builder(String name, Supplier<? extends MobEffect> effect, int duration, int amplifier) {
      this.builder = new EnumObject.Builder<>(PotionType.class);
      this.name = name;
      this.effect = effect;
      this.duration = duration;
      this.amplifier = amplifier;
      with(PotionType.NORMAL, duration, amplifier);
    }

    private Builder with(PotionType type, int duration, int amplifier) {
      String id = type == PotionType.NORMAL ? name : type.toString().toLowerCase(Locale.ROOT) + "_" + name;
      builder.put(type, register(id, () -> new Potion(modID + "." + name, new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.get()), duration, amplifier))));
      return this;
    }

    public Builder withStrong(int duration, int amplifier) {
      return with(PotionType.STRONG, duration, amplifier);
    }

    public Builder withStrong() {
      return withStrong(duration / 2, amplifier + 1);
    }

    public Builder withLong(int duration, int amplifier) {
      return with(PotionType.LONG, duration, amplifier);
    }

    public Builder withLong() {
      return withLong(duration * 8 / 3, amplifier);
    }

    public EnumObject<PotionType,Potion> build() {
      return builder.build();
    }
  }
}