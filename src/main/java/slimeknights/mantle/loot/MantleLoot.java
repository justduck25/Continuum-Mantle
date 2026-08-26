package slimeknights.mantle.loot;

import com.google.gson.JsonDeserializer;
import com.mojang.serialization.MapCodec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.loot.condition.BlockTagLootCondition;
import slimeknights.mantle.loot.condition.ContainsItemModifierLootCondition;
import slimeknights.mantle.loot.condition.EmptyModifierLootCondition;
import slimeknights.mantle.loot.condition.HasLootContextSetCondition;
import slimeknights.mantle.loot.condition.ILootModifierCondition;
import slimeknights.mantle.loot.condition.InvertedModifierLootCondition;
import slimeknights.mantle.loot.entry.TagPreferenceLootEntry;
import slimeknights.mantle.loot.function.RetexturedLootFunction;
import slimeknights.mantle.loot.function.SetFluidLootFunction;

import static slimeknights.mantle.loot.condition.ILootModifierCondition.MODIFIER_CONDITIONS;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MantleLoot {
  /** Condition to match a block tag and property predicate. */
  public static MapCodec<BlockTagLootCondition> BLOCK_TAG_CONDITION;
  /** Condition for global loot modifiers that ensures a context set is present. */
  public static MapCodec<HasLootContextSetCondition> HAS_CONTEXT_SET;
  /** Function to add block entity texture to a dropped item. */
  public static MapCodec<RetexturedLootFunction> RETEXTURED_FUNCTION;
  /** Function to add a fluid to an item fluid capability. */
  public static MapCodec<SetFluidLootFunction> SET_FLUID_FUNCTION;
  /** Entry to pull a value from a tag preference. */
  public static MapCodec<TagPreferenceLootEntry> TAG_PREFERENCE;

  /** Called during serializer registration to register any relevant loot logic. */
  public static void registerGlobalLootModifiers(final RegisterEvent event) {
    event.register(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, helper -> {
      helper.register(Mantle.getResource("add_entry"), AddEntryLootModifier.CODEC);
      helper.register(Mantle.getResource("replace_item"), ReplaceItemLootModifier.CODEC);

      MODIFIER_CONDITIONS.registerDeserializer(InvertedModifierLootCondition.ID, (JsonDeserializer<? extends ILootModifierCondition>)InvertedModifierLootCondition::deserialize);
      MODIFIER_CONDITIONS.registerDeserializer(EmptyModifierLootCondition.ID, EmptyModifierLootCondition.INSTANCE);
      MODIFIER_CONDITIONS.registerDeserializer(ContainsItemModifierLootCondition.ID, (JsonDeserializer<? extends ILootModifierCondition>)ContainsItemModifierLootCondition::deserialize);
    });

    event.register(Registries.LOOT_FUNCTION_TYPE, helper -> {
      RETEXTURED_FUNCTION = registerFunction(helper, "fill_retextured_block", RetexturedLootFunction.MAP_CODEC);
      SET_FLUID_FUNCTION = registerFunction(helper, "set_fluid", SetFluidLootFunction.MAP_CODEC);
    });

    event.register(Registries.LOOT_CONDITION_TYPE, helper -> {
      BLOCK_TAG_CONDITION = registerCondition(helper, "block_tag", BlockTagLootCondition.MAP_CODEC);
      HAS_CONTEXT_SET = registerCondition(helper, "has_context_set", HasLootContextSetCondition.MAP_CODEC);
    });

    event.register(Registries.LOOT_POOL_ENTRY_TYPE, helper -> {
      TAG_PREFERENCE = registerEntry(helper, "tag_preference", TagPreferenceLootEntry.MAP_CODEC);
    });
  }

  private static <T extends LootItemFunction> MapCodec<T> registerFunction(RegisterEvent.RegisterHelper<MapCodec<? extends LootItemFunction>> helper, String name, MapCodec<T> codec) {
    helper.register(Mantle.getResource(name), codec);
    return codec;
  }

  private static <T extends LootItemCondition> MapCodec<T> registerCondition(RegisterEvent.RegisterHelper<MapCodec<? extends LootItemCondition>> helper, String name, MapCodec<T> codec) {
    helper.register(Mantle.getResource(name), codec);
    return codec;
  }

  private static <T extends LootPoolEntryContainer> MapCodec<T> registerEntry(RegisterEvent.RegisterHelper<MapCodec<? extends LootPoolEntryContainer>> helper, String name, MapCodec<T> codec) {
    helper.register(Mantle.getResource(name), codec);
    return codec;
  }
}
