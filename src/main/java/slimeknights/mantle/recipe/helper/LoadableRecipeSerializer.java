package slimeknights.mantle.recipe.helper;

import com.mojang.serialization.MapCodec;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.data.loadable.LoadableCodec;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Recipe serializer instance using loadables.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LoadableRecipeSerializer {
  /** Fallback ID for network-only recipe decode when no legacy ID is available. */
  private static final Identifier FALLBACK_ID = Identifier.fromNamespaceAndPath("mantle", "loadable_recipe");
  /** Context key to use if you want the recipe serializer passed into your recipe */
  public static final ContextKey<RecipeSerializer<?>> SERIALIZER = new ContextKey<>("serializer");
  /** Context key to use if you want a type aware serializer in the recipe, requires {@link #of(RecordLoadable, Supplier)} for your serializer. */
  public static final ContextKey<TypeAwareRecipeSerializer<?>> TYPED_SERIALIZER = new ContextKey<>("typed_serializer");
  /** Context key to use if you want the recipe type passed into your recipe, requires {@link #of(RecordLoadable, Supplier)} for your serializer. */
  public static final ContextKey<RecipeType<?>> TYPE = new ContextKey<>("type");
  /** Field for a group key in a recipe (common requirement) */
  public static final LoadableField<String,Recipe<?>> RECIPE_GROUP = StringLoadable.DEFAULT.defaultField("group", "", Recipe::group);

  /** Creates a standard serializer from a loadable */
  public static <T extends Recipe<?>> RecipeSerializer<T> of(RecordLoadable<T> loadable) {
    return new RecipeSerializer<>(
      MapCodec.assumeMapUnsafe(new LoadableCodec<>(loadable)),
      StreamCodec.of((buf, recipe) -> encodeNetwork(buf, loadable, recipe), buf -> decodeNetwork(buf, loadable, TypedMap.EMPTY))
    );
  }

  /** Creates a type aware serializer from a loadable */
  @SuppressWarnings("unchecked")
  public static <T extends R, R extends Recipe<?>> RecipeSerializer<T> of(RecordLoadable<T> loadable, Supplier<? extends RecipeType<R>> type) {
    RecipeSerializer<T>[] serializer = new RecipeSerializer[1];
    TypeAwareRecipeSerializer<T> typeAware = new TypeAwareRecipeSerializer<>() {
      @Override
      public RecipeType<?> getType() {
        return type.get();
      }

      @Override
      public RecipeSerializer<T> getSerializer() {
        return serializer[0];
      }
    };
    TypedMap context = TypedMapBuilder.builder().put(TYPE, type.get()).put(TYPED_SERIALIZER, typeAware).build();
    serializer[0] = new RecipeSerializer<>(
      MapCodec.assumeMapUnsafe(new LoadableCodec<>(loadable, context)),
      StreamCodec.of((buf, recipe) -> encodeNetwork(buf, loadable, recipe), buf -> decodeNetwork(buf, loadable, context))
    );
    return serializer[0];
  }

  private static <T extends Recipe<?>> void encodeNetwork(RegistryFriendlyByteBuf buffer, RecordLoadable<T> loadable, T recipe) {
    buffer.writeIdentifier(getId(recipe));
    loadable.encode(buffer, recipe);
  }

  private static <T extends Recipe<?>> T decodeNetwork(RegistryFriendlyByteBuf buffer, RecordLoadable<T> loadable, TypedMap context) {
    Identifier id = buffer.readIdentifier();
    try {
      return loadable.decode(buffer, TypedMapBuilder.builder().putAll(context).put(ContextKey.ID, id).build());
    } catch (RuntimeException e) {
      throw new RuntimeException("Failed to decode recipe " + id, e);
    }
  }

  /** Attempts to fetch an ID from legacy Mantle/TCon recipe objects. */
  private static Identifier getId(Object object) {
    try {
      Method method = object.getClass().getMethod("getId");
      Object value = method.invoke(object);
      if (value instanceof Identifier id) {
        return id;
      }
    } catch (ReflectiveOperationException ignored) {}

    try {
      Field field = object.getClass().getDeclaredField("id");
      field.setAccessible(true);
      Object value = field.get(object);
      if (value instanceof Identifier id) {
        return id;
      }
    } catch (ReflectiveOperationException ignored) {}

    return FALLBACK_ID;
  }
}
