package slimeknights.mantle.loot.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;
import java.util.Set;

/** Variant of the vanilla block state condition that allows using a block tag instead of a single block. */
public record BlockTagLootCondition(TagKey<Block> tag, Optional<StatePropertiesPredicate> properties) implements LootItemCondition {
  public static final MapCodec<BlockTagLootCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter(BlockTagLootCondition::tag),
    StatePropertiesPredicate.CODEC.optionalFieldOf("properties").forGetter(BlockTagLootCondition::properties)
  ).apply(instance, BlockTagLootCondition::new));

  public BlockTagLootCondition(TagKey<Block> tag) {
    this(tag, Optional.empty());
  }

  public BlockTagLootCondition(TagKey<Block> tag, StatePropertiesPredicate.Builder builder) {
    this(tag, builder.build());
  }

  @Override
  public boolean test(LootContext context) {
    BlockState state = context.getOptionalParameter(LootContextParams.BLOCK_STATE);
    return state != null && state.is(tag) && properties.map(predicate -> predicate.matches(state)).orElse(true);
  }

  @Override
  public Set<ContextKey<?>> getReferencedContextParams() {
    return Set.of(LootContextParams.BLOCK_STATE);
  }

  @Override
  public MapCodec<BlockTagLootCondition> codec() {
    return MAP_CODEC;
  }
}