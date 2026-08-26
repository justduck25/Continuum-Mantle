package slimeknights.mantle.recipe.helper;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.LoadableCodec;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/** Helper to create ingredient types using loadables */
public class LoadableIngredientSerializer {
  /** Creates an IngredientType from a RecordLoadable */
  public static <T extends ICustomIngredient> IngredientType<T> of(RecordLoadable<T> loadable) {
    return new IngredientType<>(
      MapCodec.assumeMapUnsafe(new LoadableCodec<>(loadable)),
      StreamCodec.of(loadable::encode, loadable::decode)
    );
  }
}

