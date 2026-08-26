package slimeknights.mantle.recipe.data;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.conditions.ICondition;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builds a recipe output wrapper, adding NeoForge conditions and optional serializer overrides to recipes passing through it.
 */
@SuppressWarnings("unused")
public class ConsumerWrapperBuilder {
  private final List<ICondition> conditions = new ArrayList<>();
  @Nullable
  private final RecipeSerializer<?> override;
  @Nullable
  private final Identifier overrideName;

  private ConsumerWrapperBuilder(@Nullable RecipeSerializer<?> override, @Nullable Identifier overrideName) {
    this.override = override;
    this.overrideName = overrideName;
  }

  public static ConsumerWrapperBuilder wrap() {
    return new ConsumerWrapperBuilder(null, null);
  }

  public static ConsumerWrapperBuilder wrap(RecipeSerializer<?> override) {
    return new ConsumerWrapperBuilder(override, null);
  }

  public static ConsumerWrapperBuilder wrap(Identifier override) {
    return new ConsumerWrapperBuilder(null, override);
  }

  @CanIgnoreReturnValue
  public ConsumerWrapperBuilder addCondition(ICondition condition) {
    conditions.add(condition);
    return this;
  }

  public RecipeOutput build(RecipeOutput consumer) {
    RecipeSerializer<?> serializer = getOverrideSerializer();
    return new RecipeOutput() {
      @Override
      public void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, AdvancementHolder advancement, ICondition... incomingConditions) {
        Recipe<?> outputRecipe = serializer == null ? recipe : new SerializerOverrideRecipe<>(recipe, serializer);
        consumer.accept(key, outputRecipe, advancement, mergeConditions(incomingConditions));
      }

      @Override
      public Advancement.Builder advancement() {
        return consumer.advancement();
      }

      @Override
      public void includeRootAdvancement() {
        consumer.includeRootAdvancement();
      }
    };
  }

  @Nullable
  private RecipeSerializer<?> getOverrideSerializer() {
    if (override != null) {
      return override;
    }
    if (overrideName != null) {
      return BuiltInRegistries.RECIPE_SERIALIZER.getOptional(overrideName)
        .orElseThrow(() -> new IllegalArgumentException("Unknown recipe serializer " + overrideName));
    }
    return null;
  }

  private ICondition[] mergeConditions(ICondition[] incoming) {
    if (conditions.isEmpty()) {
      return incoming;
    }
    if (incoming.length == 0) {
      return conditions.toArray(ICondition[]::new);
    }
    List<ICondition> merged = new ArrayList<>(conditions.size() + incoming.length);
    merged.addAll(conditions);
    merged.addAll(Arrays.asList(incoming));
    return merged.toArray(ICondition[]::new);
  }

  /** Exposes the recipe wrapped by a datagen serializer override. */
  public interface WrappedRecipe {
    Recipe<?> original();
  }

  private record SerializerOverrideRecipe<T extends RecipeInput>(Recipe<T> recipe, RecipeSerializer<?> serializer) implements Recipe<T>, WrappedRecipe {
    @Override
    public Recipe<T> original() {
      return recipe;
    }

    @Override
    public boolean matches(T input, Level level) {
      return recipe.matches(input, level);
    }

    @Override
    public ItemStack assemble(T input) {
      return recipe.assemble(input);
    }

    @Override
    public boolean isSpecial() {
      return recipe.isSpecial();
    }

    @Override
    public boolean showNotification() {
      return recipe.showNotification();
    }

    @Override
    public String group() {
      return recipe.group();
    }

    @SuppressWarnings("unchecked")
    @Override
    public RecipeSerializer<? extends Recipe<T>> getSerializer() {
      return (RecipeSerializer<? extends Recipe<T>>) serializer;
    }

    @Override
    public RecipeType<? extends Recipe<T>> getType() {
      return recipe.getType();
    }

    @Override
    public PlacementInfo placementInfo() {
      return recipe.placementInfo();
    }

    @Override
    public List<RecipeDisplay> display() {
      return recipe.display();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
      return recipe.recipeBookCategory();
    }
  }
}