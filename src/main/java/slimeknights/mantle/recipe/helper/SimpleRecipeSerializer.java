package slimeknights.mantle.recipe.helper;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Function;

/** Helpers for recipe serializers with no properties other than legacy recipe ID. */
public final class SimpleRecipeSerializer {
  private static final Identifier FALLBACK_ID = Identifier.fromNamespaceAndPath("mantle", "simple_recipe");

  private SimpleRecipeSerializer() {}

  public static <T extends Recipe<?>> RecipeSerializer<T> of(Function<Identifier,T> constructor) {
    return new RecipeSerializer<>(
      MapCodec.unit(() -> constructor.apply(FALLBACK_ID)),
      StreamCodec.of((RegistryFriendlyByteBuf buf, T recipe) -> {}, buf -> constructor.apply(FALLBACK_ID))
    );
  }
}