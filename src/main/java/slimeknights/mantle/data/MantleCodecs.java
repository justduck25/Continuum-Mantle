package slimeknights.mantle.data;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.Arrays;
import java.util.function.Function;

/** This class contains codecs for various vanilla things that Mantle exposes in its loadable APIs. */
public class MantleCodecs {
  /** Codec for loot pool entries. */
  public static final Codec<LootPoolEntryContainer> LOOT_ENTRY = BuiltInRegistries.LOOT_POOL_ENTRY_TYPE.byNameCodec().dispatch(LootPoolEntryContainer::codec, Function.identity());
  /** Codec for loot item function arrays. */
  public static final Codec<LootItemFunction[]> LOOT_FUNCTIONS = BuiltInRegistries.LOOT_FUNCTION_TYPE.byNameCodec().dispatch(LootItemFunction::codec, Function.identity()).listOf().xmap(list -> list.toArray(LootItemFunction[]::new), Arrays::asList);
  /** Codec for ingredients, including NeoForge custom ingredient types. */
  public static final Codec<Ingredient> INGREDIENT = Ingredient.CODEC;
}