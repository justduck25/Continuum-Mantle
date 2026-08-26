package slimeknights.mantle.recipe.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import slimeknights.mantle.recipe.MantleRecipes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class ShapedFallbackRecipe extends ShapedRecipe {
  public static final MapCodec<ShapedFallbackRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
    instance.group(
      ShapedRecipe.MAP_CODEC.forGetter(r -> r),
      Identifier.CODEC.listOf().fieldOf("alternatives").forGetter(r -> r.alternatives)
    ).apply(instance, ShapedFallbackRecipe::new)
  );
  public static final StreamCodec<RegistryFriendlyByteBuf, ShapedFallbackRecipe> STREAM_CODEC = StreamCodec.composite(
    ShapedRecipe.STREAM_CODEC, r -> r,
    Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.alternatives,
    ShapedFallbackRecipe::new
  );

  @Getter
  private final List<Identifier> alternatives;
  private List<CraftingRecipe> alternativeCache;

  public ShapedFallbackRecipe(ShapedRecipe base, List<Identifier> alternatives) {
    super(new Recipe.CommonInfo(base.showNotification()), new CraftingRecipe.CraftingBookInfo(base.category(), base.group()), base.pattern, ItemStackTemplate.fromNonEmptyStack(base.assemble(CraftingInput.EMPTY)));
    this.alternatives = alternatives;
  }

  @Override
  public boolean matches(CraftingInput inv, Level world) {
    if (!super.matches(inv, world)) {
      return false;
    }

    if (alternativeCache == null) {
      RecipeManager manager = world.getServer() == null ? null : world.getServer().getRecipeManager();
      if (manager == null) {
        alternativeCache = List.of();
      } else {
        alternativeCache = alternatives.stream()
          .map(id -> ResourceKey.create(Registries.RECIPE, id))
          .map(manager::byKey)
          .flatMap(Optional::stream)
          .map(RecipeHolder::value)
          .filter(recipe -> {
            Class<?> clazz = recipe.getClass();
            return clazz == ShapedRecipe.class || clazz == ShapelessRecipe.class;
          })
          .map(recipe -> (CraftingRecipe) recipe)
          .collect(Collectors.toList());
      }
    }
    return this.alternativeCache.stream().noneMatch(recipe -> recipe.matches(inv, world));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public RecipeSerializer<ShapedRecipe> getSerializer() {
    return (RecipeSerializer) MantleRecipes.CRAFTING_SHAPED_FALLBACK.get();
  }
}