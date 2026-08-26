package slimeknights.mantle.plugin.jei.entity;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import slimeknights.mantle.plugin.jei.MantleJEIConstants;
import slimeknights.mantle.recipe.ingredient.EntityIngredient;

import javax.annotation.Nullable;

/** Handler for working with entity types as ingredients */
public class EntityIngredientHelper implements IIngredientHelper<EntityIngredient.EntityInput> {
  @Override
  public IIngredientType<EntityIngredient.EntityInput> getIngredientType() {
    return MantleJEIConstants.ENTITY_TYPE;
  }

  @Override
  public String getDisplayName(EntityIngredient.EntityInput type) {
    return type.type().getDescription().getString();
  }

  @Override
  public Object getUid(EntityIngredient.EntityInput type, UidContext context) {
    return getIdentifier(type).toString();
  }

  @Override
  public Identifier getIdentifier(EntityIngredient.EntityInput type) {
    return BuiltInRegistries.ENTITY_TYPE.getKey(type.type());
  }

  @Override
  public EntityIngredient.EntityInput copyIngredient(EntityIngredient.EntityInput type) {
    return type;
  }

  @Override
  public String getErrorInfo(@Nullable EntityIngredient.EntityInput type) {
    if (type == null) {
      return "null";
    }
    return getIdentifier(type).toString();
  }
}
