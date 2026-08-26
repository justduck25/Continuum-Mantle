package slimeknights.mantle.registration.deferred;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.mantle.registration.object.EnumObject;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Deferred register to register tile entity instances.
 */
@SuppressWarnings("unused")
public class BlockEntityTypeDeferredRegister extends DeferredRegisterWrapper<BlockEntityType<?>> {
  @FunctionalInterface
  public interface BlockEntityFactory<T extends BlockEntity> {
    T create(BlockPos pos, BlockState state);
  }

  public BlockEntityTypeDeferredRegister(String modID) {
    super(Registries.BLOCK_ENTITY_TYPE, modID);
  }

  private static <T extends BlockEntity> BlockEntityType<T> createType(String name, BlockEntityFactory<? extends T> factory, Block[] blocks) {
    return new BlockEntityType<>(factory::create, Set.of(blocks));
  }

  public <T extends BlockEntity> DeferredHolder register(String name, BlockEntityFactory<? extends T> factory, Supplier<? extends Block> block) {
    return register.register(name, () -> createType(name, factory, new Block[] { block.get() }));
  }

  public <T extends BlockEntity> DeferredHolder register(String name, BlockEntityFactory<? extends T> factory, EnumObject<?, ? extends Block> blocks) {
    return register.register(name, () -> createType(name, factory, blocks.values().toArray(Block[]::new)));
  }

  public <T extends BlockEntity> DeferredHolder register(String name, BlockEntityFactory<? extends T> factory, Consumer<Set<Block>> blockCollector) {
    return register.register(name, () -> {
      Set<Block> blocks = new HashSet<>();
      blockCollector.accept(blocks);
      return createType(name, factory, blocks.toArray(Block[]::new));
    });
  }
}