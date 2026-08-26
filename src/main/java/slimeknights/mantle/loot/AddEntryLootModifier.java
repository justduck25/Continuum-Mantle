package slimeknights.mantle.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import slimeknights.mantle.data.MantleCodecs;
import slimeknights.mantle.loot.condition.ILootModifierCondition;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/** Loot modifier to inject an additional loot entry into an existing table. */
public class AddEntryLootModifier extends LootModifier {
  public static final MapCodec<AddEntryLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).and(inst.group(
    ILootModifierCondition.CODEC.listOf().fieldOf("post_conditions").forGetter(m -> m.modifierConditions),
    MantleCodecs.LOOT_ENTRY.fieldOf("entry").forGetter(m -> m.entry),
    MantleCodecs.LOOT_FUNCTIONS.fieldOf("functions").forGetter(m -> m.functions))).apply(inst, AddEntryLootModifier::new));

  private final List<ILootModifierCondition> modifierConditions;
  private final LootPoolEntryContainer entry;
  private final LootItemFunction[] functions;
  private final BiFunction<ItemStack, LootContext, ItemStack> combinedFunctions;

  protected AddEntryLootModifier(LootItemCondition[] conditionsIn, int priority, List<ILootModifierCondition> modifierConditions, LootPoolEntryContainer entry, LootItemFunction[] functions) {
    super(conditionsIn, priority);
    this.modifierConditions = modifierConditions;
    this.entry = entry;
    this.functions = functions;
    this.combinedFunctions = LootItemFunctions.compose(List.of(functions));
  }

  public static Builder builder(LootPoolEntryContainer entry) {
    return new Builder(entry);
  }

  public static Builder builder(LootPoolEntryContainer.Builder<?> builder) {
    return builder(builder.build());
  }

  @Nonnull
  @Override
  protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
    for (ILootModifierCondition modifierCondition : modifierConditions) {
      if (!modifierCondition.test(generatedLoot, context)) {
        return generatedLoot;
      }
    }
    Consumer<ItemStack> consumer = LootItemFunction.decorate(this.combinedFunctions, generatedLoot::add, context);
    entry.expand(context, generator -> generator.createItemStack(consumer, context));
    return generatedLoot;
  }

  @Override
  public MapCodec<? extends IGlobalLootModifier> codec() {
    return CODEC;
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder extends AbstractLootModifierBuilder<Builder> {
    private final List<ILootModifierCondition> modifierConditions = new ArrayList<>();
    private final LootPoolEntryContainer entry;
    private final List<LootItemFunction> functions = new ArrayList<>();

    public Builder addCondition(ILootModifierCondition condition) {
      modifierConditions.add(condition);
      return this;
    }

    public Builder addFunction(LootItemFunction function) {
      functions.add(function);
      return this;
    }

    public AddEntryLootModifier build() {
      return new AddEntryLootModifier(getConditions(), IGlobalLootModifier.DEFAULT_PRIORITY, modifierConditions, entry, functions.toArray(new LootItemFunction[0]));
    }
  }
}