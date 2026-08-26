package slimeknights.mantle.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import slimeknights.mantle.data.MantleCodecs;
import slimeknights.mantle.recipe.helper.ItemOutput;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiFunction;

/** Loot modifier to replace an item with another. */
public class ReplaceItemLootModifier extends LootModifier {
  public static final MapCodec<ReplaceItemLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).and(inst.group(
    MantleCodecs.INGREDIENT.fieldOf("original").forGetter(m -> m.original),
    ItemOutput.REQUIRED_STACK_CODEC.fieldOf("replacement").forGetter(m -> m.replacement),
    MantleCodecs.LOOT_FUNCTIONS.fieldOf("functions").forGetter(m -> m.functions)
  )).apply(inst, ReplaceItemLootModifier::new));

  private final Ingredient original;
  private final ItemOutput replacement;
  private final LootItemFunction[] functions;
  private final BiFunction<ItemStack, LootContext, ItemStack> combinedFunctions;

  protected ReplaceItemLootModifier(LootItemCondition[] conditionsIn, int priority, Ingredient original, ItemOutput replacement, LootItemFunction[] functions) {
    super(conditionsIn, priority);
    this.original = original;
    this.replacement = replacement;
    this.functions = functions;
    this.combinedFunctions = LootItemFunctions.compose(List.of(functions));
  }

  public static Builder builder(Ingredient original, ItemOutput replacement) {
    return new Builder(original, replacement);
  }

  @Nonnull
  @Override
  protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
    ListIterator<ItemStack> iterator = generatedLoot.listIterator();
    while (iterator.hasNext()) {
      ItemStack stack = iterator.next();
      if (original.test(stack)) {
        ItemStack replacement = this.replacement.get();
        iterator.set(combinedFunctions.apply(replacement.copyWithCount(replacement.getCount() * stack.getCount()), context));
      }
    }
    return generatedLoot;
  }

  @Override
  public MapCodec<? extends IGlobalLootModifier> codec() {
    return CODEC;
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder extends AbstractLootModifierBuilder<Builder> {
    private final Ingredient input;
    private final ItemOutput replacement;
    private final List<LootItemFunction> functions = new ArrayList<>();

    public Builder addFunction(LootItemFunction function) {
      functions.add(function);
      return this;
    }

    public ReplaceItemLootModifier build() {
      return new ReplaceItemLootModifier(getConditions(), IGlobalLootModifier.DEFAULT_PRIORITY, input, replacement, functions.toArray(new LootItemFunction[0]));
    }
  }
}