package slimeknights.mantle.recipe.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.IMultiRecipe;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helpers used in creation of recipes.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RecipeHelper {
  public static <C extends Recipe<?>> Optional<C> getRecipe(RecipeManager manager, Identifier name, Class<C> clazz) {
    return manager.byKey(ResourceKey.create(Registries.RECIPE, name)).map(RecipeHolder::value).filter(clazz::isInstance).map(clazz::cast);
  }

  public static <I extends RecipeInput, T extends Recipe<I>, C extends T> List<C> getRecipes(RecipeManager manager, RecipeType<T> type, Class<C> clazz) {
    return manager.getRecipes().stream()
                  .filter(holder -> holder.value().getType() == type)
                  .map(RecipeHolder::value)
                  .filter(clazz::isInstance)
                  .map(clazz::cast)
                  .collect(Collectors.toList());
  }

  public static <I extends RecipeInput, T extends Recipe<I>, C extends T> List<C> getUIRecipes(RecipeManager manager, RecipeType<T> type, Class<C> clazz, Predicate<? super C> filter) {
    return manager.getRecipes().stream()
                  .filter(holder -> holder.value().getType() == type)
                  .filter(holder -> clazz.isInstance(holder.value()))
                  .sorted(Comparator.comparing(holder -> holder.id().identifier()))
                  .map(holder -> clazz.cast(holder.value()))
                  .filter(filter)
                  .collect(Collectors.toList());
  }

  public static <C> List<C> getJEIRecipes(RegistryAccess access, Stream<? extends RecipeHolder<?>> recipes, Class<C> clazz) {
    return recipes
        .sorted((h1, h2) -> {
          Recipe<?> r1 = h1.value();
          Recipe<?> r2 = h2.value();
          boolean m1 = r1 instanceof IMultiRecipe<?>;
          boolean m2 = r2 instanceof IMultiRecipe<?>;
          if (m1 && !m2) return 1;
          if (!m1 && m2) return -1;
          return h1.id().identifier().compareTo(h2.id().identifier());
        })
        .flatMap(holder -> {
          Recipe<?> recipe = holder.value();
          if (recipe instanceof IMultiRecipe<?>) {
            try {
              return ((IMultiRecipe<?>) recipe).getRecipes(access).stream();
            } catch (Exception e) {
              Mantle.logger.error("Failed to fetch JEI recipes for multi recipe {} ({})", holder.id().identifier(), recipe, e);
              return Stream.empty();
            }
          }
          return Stream.of(recipe);
        })
        .filter(clazz::isInstance)
        .map(clazz::cast)
        .collect(Collectors.toList());
  }

  public static <I extends RecipeInput, T extends Recipe<I>, C> List<C> getJEIRecipes(RegistryAccess access, RecipeManager manager, RecipeType<T> type, Class<C> clazz) {
    return getJEIRecipes(access, manager.getRecipes().stream().filter(holder -> holder.value().getType() == type), clazz);
  }
}