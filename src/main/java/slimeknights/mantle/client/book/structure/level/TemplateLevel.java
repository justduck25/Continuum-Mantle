package slimeknights.mantle.client.book.structure.level;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/** Lightweight block view used to render structure previews in books. */
public class TemplateLevel implements BlockAndTintGetter {
  private final Map<BlockPos, StructureBlockInfo> blocks = new HashMap<>();
  private final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
  private final Predicate<BlockPos> shouldShow;

  public TemplateLevel(List<StructureBlockInfo> blocks, Predicate<BlockPos> shouldShow) {
    this.shouldShow = shouldShow;
    var registryAccess = Objects.requireNonNull(Minecraft.getInstance().level).registryAccess();
    for (StructureBlockInfo info : blocks) {
      this.blocks.put(info.pos(), info);
      CompoundTag tag = info.nbt();
      if (tag != null) {
        BlockEntity blockEntity = BlockEntity.loadStatic(info.pos(), info.state(), tag, registryAccess);
        if (blockEntity != null) {
          this.blockEntities.put(info.pos(), blockEntity);
        }
      }
    }
  }

  @Override
  public CardinalLighting cardinalLighting() {
    return CardinalLighting.DEFAULT;
  }

  @Override
  public LevelLightEngine getLightEngine() {
    return LevelLightEngine.EMPTY;
  }

  @Override
  public int getBlockTint(BlockPos pos, ColorResolver color) {
    return -1;
  }

  @Nullable
  @Override
  public BlockEntity getBlockEntity(BlockPos pos) {
    if (!this.shouldShow.test(pos)) {
      return null;
    }
    return this.blockEntities.get(pos);
  }

  @Override
  public BlockState getBlockState(BlockPos pos) {
    if (this.shouldShow.test(pos)) {
      StructureBlockInfo info = this.blocks.get(pos);
      if (info != null) {
        return info.state();
      }
    }
    return Blocks.VOID_AIR.defaultBlockState();
  }

  @Override
  public FluidState getFluidState(BlockPos pos) {
    return this.getBlockState(pos).getFluidState();
  }

  @Override
  public int getHeight() {
    return 384;
  }

  @Override
  public int getMinY() {
    return -64;
  }
}
