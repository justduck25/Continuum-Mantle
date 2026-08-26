package slimeknights.mantle.registration.adapter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.resources.Identifier;
import slimeknights.mantle.registration.object.EnumObject;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Registry adapter for tile entity types with helpers for adding blocks
 */
@SuppressWarnings("unused")
public class BlockEntityTypeRegistryAdapter extends RegistryAdapter<BlockEntityType<?>> {
  public interface BlockEntityFactory<T extends BlockEntity> {
    T create(BlockPos pos, BlockState state);
  }

  /** @inheritDoc */
  public BlockEntityTypeRegistryAdapter(BiConsumer<Identifier, BlockEntityType<?>> register, String modId) {
    super(register, modId);
  }

  /** @inheritDoc */
  public BlockEntityTypeRegistryAdapter(BiConsumer<Identifier, BlockEntityType<?>> register) {
    super(register);
  }

  /**
   * Gets the data fixer type for the tile entity instance
   * @param name  Tile entity name
   * @return  Data fixer type
   */
  @Nullable
  private Type<?> getType(String name) {
    return Util.fetchChoiceType(References.BLOCK_ENTITY, resourceName(name));
  }

  /**
   * Registers a tile entity type for a single block
   * @param factory  Tile entity factory
   * @param block    Single block to add
   * @param name     Tile entity name
   * @param <T>      Tile entity type
   * @return  Registry object instance
   */
  @SuppressWarnings("ConstantConditions")
  public <T extends BlockEntity> BlockEntityType<T> register(BlockEntityFactory<? extends T> factory, Block block, String name) {
    return register(new BlockEntityType<>(factory::create, block), name);
  }

  /**
   * Registers a tile entity type for a single block
   * @param factory  Tile entity factory
   * @param blocks   Blocks to add
   * @param name     Tile entity name
   * @param <T>      Tile entity type
   * @return  Registry object instance
   */
  @SuppressWarnings("ConstantConditions")
  public <T extends BlockEntity> BlockEntityType<T> register(BlockEntityFactory<? extends T> factory, Collection<? extends Block> blocks, String name) {
    return register(new BlockEntityType<>(factory::create, blocks.toArray(new Block[0])), name);
  }

  /**
   * Registers a new tile entity type using a tile entity factory and an enum object
   * @param name     Tile entity name
   * @param factory  Tile entity factory
   * @param blocks   Enum object instance
   * @param <T>      Tile entity type
   * @return  Tile entity type registry object
   */
  public <T extends BlockEntity> BlockEntityType<T> register(BlockEntityFactory<? extends T> factory, EnumObject<?, ? extends Block> blocks, String name) {
    return register(factory, blocks.values(), name);
  }

  /**
   * Registers a new tile entity type using a tile entity factory and an immutable set builder
   * @param factory          Tile entity factory
   * @param name             Tile entity name
   * @param blockCollector   Function to get blocks for the list
   * @param <T>              Tile entity type
   * @return  Tile entity type registry object
   */
  public <T extends BlockEntity> BlockEntityType<T> register(BlockEntityFactory<? extends T> factory, String name, Consumer<ImmutableSet.Builder<Block>> blockCollector) {
    ImmutableSet.Builder<Block> blocks = ImmutableSet.builder();
    blockCollector.accept(blocks);
    return register(factory, blocks.build(), name);
  }
}
