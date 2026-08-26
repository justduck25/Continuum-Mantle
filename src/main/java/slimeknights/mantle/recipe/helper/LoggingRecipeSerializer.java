package slimeknights.mantle.recipe.helper;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;

/**
 * Recipe serializer that logs network exceptions before throwing them as otherwise the exceptions may be invisible
 * @param <T>  Recipe class
 */
public interface LoggingRecipeSerializer<T extends Recipe<?>> {
  /**
   * Read the recipe from the packet
   * @param id      Recipe ID
   * @param buffer  Buffer instance
   * @return  Parsed recipe
   * @throws RuntimeException  If any errors happen, the exception will be logged automatically
   */
  @Nullable
  T fromNetworkSafe(Identifier id, FriendlyByteBuf buffer);

  /**
   * Write the method to the buffer
   * @param buffer  Buffer instance
   * @param recipe  Recipe instance
   * @throws RuntimeException  If any errors happen, the exception will be logged automatically
   */
  void toNetworkSafe(FriendlyByteBuf buffer, T recipe);

  @Nullable
  default T fromNetwork(Identifier id, FriendlyByteBuf buffer) {
    try {
      return fromNetworkSafe(id, buffer);
    } catch (RuntimeException e) {
      String error = this.getClass().getSimpleName() + ": Error reading recipe " + id + " from packet";
      Mantle.logger.error("{}", error, e);
      throw new DecoderException(error + " - " + e.getMessage(), e);
    }
  }
  default void toNetwork(FriendlyByteBuf buffer, T recipe) {
    try {
      toNetworkSafe(buffer, recipe);
    } catch (RuntimeException e) {
      String error = this.getClass().getSimpleName() + ": Error writing recipe " + recipe.getClass().getSimpleName() + " to packet";
      Mantle.logger.error("{}", error, e);
      throw new EncoderException(error + " - " + e.getMessage(), e);
    }
  }
}
