package slimeknights.mantle.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class InventoryBlockEntityRenderer<T extends BlockEntity & Container> implements BlockEntityRenderer<T, InventoryBlockEntityRenderer.InventoryBlockEntityRenderState> {
  private final ItemModelResolver itemModelResolver;

  public InventoryBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    this.itemModelResolver = context.itemModelResolver();
  }

  @Override
  public InventoryBlockEntityRenderState createRenderState() {
    return new InventoryBlockEntityRenderState();
  }

  @Override
  public void extractRenderState(T inventory, InventoryBlockEntityRenderState state, float partialTicks, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
    BlockEntityRenderState.extractBase(inventory, state, breakProgress);
    state.isEmpty = inventory.isEmpty();
    if (!state.isEmpty) {
      state.blockState = inventory.getBlockState();
      state.renderItems = RenderItem.STATE_REGISTRY.get(state.blockState, List.of());
      state.items.clear();
      for (int i = 0; i < state.renderItems.size(); i++) {
        ItemStackRenderState itemState = new ItemStackRenderState();
        if (i < inventory.getContainerSize()) {
          ItemStack stack = inventory.getItem(i);
          if (!stack.isEmpty()) {
            this.itemModelResolver.updateForTopItem(itemState, stack, state.renderItems.get(i).getTransform(), inventory.getLevel(), null, (int)(inventory.getBlockPos().asLong() + i));
          }
        }
        state.items.add(itemState);
      }
    }
  }

  @Override
  public void submit(InventoryBlockEntityRenderState state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
    if (state.isEmpty || state.renderItems.isEmpty()) return;

    boolean isRotated = RenderingHelper.applyRotation(matrices, state.blockState);

    for (int i = 0; i < state.renderItems.size() && i < state.items.size(); i++) {
      RenderingHelper.renderItem(matrices, collector, state.items.get(i), state.renderItems.get(i), state.lightCoords);
    }

    if (isRotated) {
      matrices.popPose();
    }
  }

  @Override
  public boolean shouldRenderOffScreen() {
    return true;
  }

  public static class InventoryBlockEntityRenderState extends BlockEntityRenderState {
    public boolean isEmpty = true;
    public BlockState blockState;
    public List<ItemStackRenderState> items = new ArrayList<>();
    public List<RenderItem> renderItems = new ArrayList<>();
  }
}
