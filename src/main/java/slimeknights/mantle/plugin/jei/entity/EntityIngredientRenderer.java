package slimeknights.mantle.plugin.jei.entity;

import lombok.RequiredArgsConstructor;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import slimeknights.mantle.recipe.ingredient.EntityIngredient;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class EntityIngredientRenderer implements IIngredientRenderer<EntityIngredient.EntityInput> {
  private static final Set<EntityType<?>> IGNORED_ENTITIES = new HashSet<>();
  private final int size;
  private final Map<EntityType<?>, Entity> ENTITY_MAP = new HashMap<>();

  @Override
  public int getWidth() {
    return size;
  }

  @Override
  public int getHeight() {
    return size;
  }

  @Override
  public void render(GuiGraphicsExtractor graphics, @Nullable EntityIngredient.EntityInput input) {
    if (input == null) return;

    EntityType<?> type = input.type();
    Entity entity = ENTITY_MAP.get(type);
    if (entity == null && !IGNORED_ENTITIES.contains(type)) {
      Minecraft mc = Minecraft.getInstance();
      Level level = mc.level;
      if (level == null) return;
      try {
        entity = type.create(level, EntitySpawnReason.EVENT);
        if (entity != null) {
          entity.setPos(0, 0, 0);
          if (entity instanceof LivingEntity living) {
            living.yBodyRot = 0;
            living.yHeadRot = 0;
            living.setYRot(0);
            living.setXRot(0);
          }
          ENTITY_MAP.put(type, entity);
        }
      } catch (Exception e) {
        IGNORED_ENTITIES.add(type);
        return;
      }
    }
    if (entity == null) return;

    if (!(entity instanceof net.minecraft.world.entity.LivingEntity living)) return;
    living.setYRot(0);
    living.setXRot(0);
    // Minecraft 26.1 initializes the living render state through this helper.
    // JEI calls ingredient renderers with the pose translated to the ingredient slot.
    // InventoryScreen's helper consumes absolute GUI coordinates, not local slot coordinates.
    int x = Math.round(graphics.pose().m20());
    int y = Math.round(graphics.pose().m21());
    // Preserve Mantle's original scale: normal mobs use half the renderer size.
    // Large mobs are reduced further, but small mobs are never scaled up.
    int scale = size / 2;
    float height = living.getBbHeight();
    float width = living.getBbWidth();
    if (height > 2 || width > 2) {
      scale = (int)(size / Math.max(height, width));
    }
    // Use the current scaled GUI mouse position, matching the original interactive renderer.
    Minecraft minecraft = Minecraft.getInstance();
    float mouseX = (float)minecraft.mouseHandler.getScaledXPos(minecraft.getWindow());
    float mouseY = (float)minecraft.mouseHandler.getScaledYPos(minecraft.getWindow());
    InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, x, y, x + size, y + size, scale, 0, mouseX, mouseY, living);
  }

  @Override
  public List<Component> getTooltip(EntityIngredient.EntityInput type, TooltipFlag flag) {
    List<Component> tooltip = new ArrayList<>();
    tooltip.add(type.type().getDescription());
    if (flag.isAdvanced()) {
      tooltip.add((Component.literal(BuiltInRegistries.ENTITY_TYPE.getKey(type.type()).toString())).withStyle(ChatFormatting.DARK_GRAY));
    }
    return tooltip;
  }
}
