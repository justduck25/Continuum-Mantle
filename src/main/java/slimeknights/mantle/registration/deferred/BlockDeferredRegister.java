package slimeknights.mantle.registration.deferred;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.PressurePlateBlock;

import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;











import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.EnumObject.Builder;
import slimeknights.mantle.registration.object.FenceBuildingBlockObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.mantle.registration.object.MetalItemObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject.WoodVariant;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Deferred register to handle registering blocks with possible item forms
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BlockDeferredRegister extends DeferredRegisterWrapper<Block> {
  private static final ThreadLocal<ResourceKey<Block>> CURRENT_BLOCK_KEY = new ThreadLocal<>();
  private static final BlockBehaviour.Properties POTTED_PROPS = BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY);


  /** Applies the currently registering block ID to properties created inside a block supplier. */
  public static BlockBehaviour.Properties setIdFromCurrentKey(BlockBehaviour.Properties props) {
    ResourceKey<Block> key = CURRENT_BLOCK_KEY.get();
    return key == null ? props : props.setId(key);
  }

  /** Runs a block supplier while exposing the block key for property helpers. */
  public static <B extends Block> B withCurrentBlockKey(ResourceKey<Block> key, Supplier<? extends B> supplier) {
    CURRENT_BLOCK_KEY.set(key);
    try {
      return supplier.get();
    } finally {
      CURRENT_BLOCK_KEY.remove();
    }
  }
  protected final SynchronizedDeferredRegister<Item> itemRegister;
  public BlockDeferredRegister(String modID) {
    super(Registries.BLOCK, modID);
    this.itemRegister = SynchronizedDeferredRegister.create(Registries.ITEM, modID);
  }

  @Override
  public void register(IEventBus bus) {
    super.register(bus);
    itemRegister.register(bus);
  }


  /* Blocks with no items */

  /**
   * Registers a block with the block registry
   * @param name   Block ID
   * @param block  Block supplier
   * @param <B>    Block class
   * @return  Block registry object
   */
  public <B extends Block> DeferredHolder registerNoItem(String name, Supplier<? extends B> block) {
    ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, resource(name));
    return register.register(name, () -> {
      CURRENT_BLOCK_KEY.set(key);
      try {
        return block.get();
      } finally {
        CURRENT_BLOCK_KEY.remove();
      }
    });
  }

  /**
   * Registers a block with the block registry
   * @param name   Block ID
   * @param props  Block properties
   * @return  Block registry object
   */
  public DeferredHolder registerNoItem(String name, BlockBehaviour.Properties props) {
    return registerNoItem(name, () -> new Block(setIdFromCurrentKey(props)));
  }


  /* Block item pairs */

  /**
   * Registers a block with the block registry, using the function for the BlockItem
   * @param name   Block ID
   * @param block  Block supplier
   * @param item   Function to create a BlockItem from a Block
   * @param <B>    Block class
   * @return  Block item registry object pair
   */
  public <B extends Block> ItemObject<B> register(String name, Supplier<? extends B> block, final Function<? super B, ? extends BlockItem> item) {
    DeferredHolder blockObj = registerNoItem(name, block);
    ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, resource(name));
    itemRegister.register(name, () -> ItemDeferredRegister.withCurrentItemKey(itemKey, () -> item.apply((B) blockObj.get())));
    return new ItemObject<>(blockObj);
  }

  /**
   * Registers a block with the block registry, using the function for the BlockItem
   * @param name        Block ID
   * @param blockProps  Block supplier
   * @param item        Function to create a BlockItem from a Block
   * @return  Block item registry object pair
   */
  public ItemObject<Block> register(String name, BlockBehaviour.Properties blockProps, Function<? super Block, ? extends BlockItem> item) {
    return register(name, () -> new Block(setIdFromCurrentKey(blockProps)), item);
  }


  /* Building */

  /**
   * Registers a building block with slabs and stairs, using a custom block
   * @param name   Block name
   * @param block  Block supplier
   * @param item   Item block, used for all variants
   * @return  Building block object
   */
  public BuildingBlockObject registerBuilding(String name, Supplier<? extends Block> block, Function<? super Block, ? extends BlockItem> item) {
    ItemObject<Block> blockObj = register(name, block, item);
    return new BuildingBlockObject(
        blockObj,
        this.register(name + "_slab", () -> new SlabBlock(setIdFromCurrentKey(BlockBehaviour.Properties.ofFullCopy(blockObj.get()))), item),
        this.register(name + "_stairs", () -> new MantleStairBlock(blockObj.get().defaultBlockState(), setIdFromCurrentKey(BlockBehaviour.Properties.ofFullCopy(blockObj.get()))), item));
  }

  /**
   * Registers a block with slab, and stairs
   * @param name      Name of the block
   * @param props     Block properties
   * @param item      Function to get an item from the block
   * @return  BuildingBlockObject class that returns different block types
   */
  public BuildingBlockObject registerBuilding(String name, BlockBehaviour.Properties props, Function<? super Block, ? extends BlockItem> item) {
    ItemObject<Block> blockObj = register(name, props, item);
    return new BuildingBlockObject(blockObj,
      register(name + "_slab", () -> new SlabBlock(setIdFromCurrentKey(props)), item),
      register(name + "_stairs", () -> new MantleStairBlock(blockObj.get().defaultBlockState(), setIdFromCurrentKey(props)), item)
    );
  }

  /**
   * Registers a building block with slabs, stairs and wall, using a custom block
   * @param name   Block name
   * @param block  Block supplier
   * @param item   Item block, used for all variants
   * @return  Building block object
   */
  public WallBuildingBlockObject registerWallBuilding(String name, Supplier<? extends Block> block, Function<? super Block, ? extends BlockItem> item) {
    BuildingBlockObject obj = this.registerBuilding(name, block, item);
    return new WallBuildingBlockObject(obj, this.register(name + "_wall", () -> new WallBlock(setIdFromCurrentKey(BlockBehaviour.Properties.ofFullCopy(obj.get()))), item));
  }

  /**
   * Registers a block with slab, stairs, and wall
   * @param name      Name of the block
   * @param props     Block properties
   * @param item      Function to get an item from the block
   * @return  StoneBuildingBlockObject class that returns different block types
   */
  public WallBuildingBlockObject registerWallBuilding(String name, BlockBehaviour.Properties props, Function<? super Block, ? extends BlockItem> item) {
    return new WallBuildingBlockObject(
      registerBuilding(name, props, item),
      register(name + "_wall", () -> new WallBlock(setIdFromCurrentKey(props)), item)
    );
  }

  /**
   * Registers a building block with slabs, stairs and wall, using a custom block
   * @param name   Block name
   * @param block  Block supplier
   * @param item   Item block, used for all variants
   * @return  Building block object
   */
  public FenceBuildingBlockObject registerFenceBuilding(String name, Supplier<? extends Block> block, Function<? super Block, ? extends BlockItem> item) {
    BuildingBlockObject obj = this.registerBuilding(name, block, item);
    return new FenceBuildingBlockObject(obj, this.register(name + "_fence", () -> new FenceBlock(setIdFromCurrentKey(BlockBehaviour.Properties.ofFullCopy(obj.get()))), item));
  }

  /**
   * Registers a block with slab, stairs, and fence
   * @param name      Name of the block
   * @param props     Block properties
   * @param item      Function to get an item from the block
   * @return  WoodBuildingBlockObject class that returns different block types
   */
  public FenceBuildingBlockObject registerFenceBuilding(String name, BlockBehaviour.Properties props, Function<? super Block, ? extends BlockItem> item) {
    return new FenceBuildingBlockObject(
      registerBuilding(name, props, item),
      register(name + "_fence", () -> new FenceBlock(setIdFromCurrentKey(props)), item)
    );
  }

  /**
   * Registers a new wood object
   * @param name             Name of the wood object
   * @param behaviorCreator  Logic to create the behavior
   * @param flammable        If true, this wood type is flammable
   * @return Wood object
   */
  public WoodBlockObject registerWood(String name, Function<WoodVariant,BlockBehaviour.Properties> behaviorCreator, boolean flammable) {
    BlockSetType setType = new BlockSetType(resourceName(name));
    WoodType woodType = new WoodType(resourceName(name), setType);
    RegistrationHelper.registerWoodType(woodType);
    Item.Properties itemProps = new Item.Properties();

    // many of these are already burnable via tags, but simplier to set them all here
    Function<Integer, Function<? super Block, ? extends BlockItem>> burnableItem;
    Function<? super Block, ? extends BlockItem> burnableTallItem;
    BiFunction<? super Block, ? super Block, ? extends BlockItem> burnableSignItem;
    BiFunction<? super Block, ? super Block, ? extends BlockItem> burnableHangingSignItem;
    Item.Properties signProps = new Item.Properties().stacksTo(16);
    if (flammable) {
      burnableItem     = burnTime -> block -> new BlockItem(block, ItemDeferredRegister.setIdFromCurrentKey(itemProps));
      burnableTallItem = block -> new DoubleHighBlockItem(block, ItemDeferredRegister.setIdFromCurrentKey(itemProps));
      burnableSignItem = (standing, wall) -> new SignItem(standing, wall, ItemDeferredRegister.setIdFromCurrentKey(signProps));
      burnableHangingSignItem = (standing, wall) -> new HangingSignItem(standing, wall, ItemDeferredRegister.setIdFromCurrentKey(signProps));
    } else {
      Function<? super Block, ? extends BlockItem> defaultItemBlock = block -> new BlockItem(block, ItemDeferredRegister.setIdFromCurrentKey(itemProps));
      burnableItem = burnTime -> defaultItemBlock;
      burnableTallItem = block -> new DoubleHighBlockItem(block, ItemDeferredRegister.setIdFromCurrentKey(itemProps));
      burnableSignItem = (standing, wall) -> new SignItem(standing, wall, ItemDeferredRegister.setIdFromCurrentKey(signProps));
      burnableHangingSignItem = (standing, wall) -> new HangingSignItem(standing, wall, ItemDeferredRegister.setIdFromCurrentKey(signProps));
    }

    // planks
    Function<? super Block, ? extends BlockItem> burnable300 = burnableItem.apply(300);
    BlockBehaviour.Properties planksProps = behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).strength(2.0f, 3.0f);
    BuildingBlockObject planks = registerBuilding(name + "_planks", planksProps, block -> burnableItem.apply(block instanceof SlabBlock ? 150 : 300).apply(block));
    ItemObject<FenceBlock> fence = register(name + "_fence", () -> new FenceBlock(setIdFromCurrentKey(Properties.ofFullCopy(planks.get()).forceSolidOn())), burnable300);
    // logs and wood
    Supplier<? extends RotatedPillarBlock> stripped = () -> new RotatedPillarBlock(setIdFromCurrentKey(behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).strength(2.0f)));
    ItemObject<RotatedPillarBlock> strippedLog = register("stripped_" + name + "_log", stripped, burnable300);
    ItemObject<RotatedPillarBlock> strippedWood = register("stripped_" + name + "_wood", stripped, burnable300);
    ItemObject<RotatedPillarBlock> log = register(name + "_log", () -> new RotatedPillarBlock(setIdFromCurrentKey(behaviorCreator.apply(WoodBlockObject.WoodVariant.LOG).instrument(NoteBlockInstrument.BASS).strength(2.0f))), burnable300);
    ItemObject<RotatedPillarBlock> wood = register(name + "_wood", () -> new RotatedPillarBlock(setIdFromCurrentKey(behaviorCreator.apply(WoodBlockObject.WoodVariant.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0f))), burnable300);

    // doors
    ItemObject<DoorBlock> door = register(name + "_door", () -> new MantleDoorBlock(setType, setIdFromCurrentKey(behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().pushReaction(PushReaction.DESTROY))), burnableTallItem);
    ItemObject<TrapDoorBlock> trapdoor = register(name + "_trapdoor", () -> new MantleTrapDoorBlock(setType, setIdFromCurrentKey(behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn((state, getter, pos, type) -> false))), burnable300);
    ItemObject<FenceGateBlock> fenceGate = register(name + "_fence_gate", () -> new FenceGateBlock(woodType, setIdFromCurrentKey(BlockBehaviour.Properties.ofFullCopy(fence.get()))), burnable300);
    // redstone
    BlockBehaviour.Properties redstoneProps = behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().pushReaction(PushReaction.DESTROY).strength(0.5F);
    ItemObject<PressurePlateBlock> pressurePlate = register(name + "_pressure_plate", () -> new MantlePressurePlateBlock(setType, setIdFromCurrentKey(redstoneProps)), burnable300);
    ItemObject<ButtonBlock> button = register(name + "_button", () -> new MantleButtonBlock(setType, 30, setIdFromCurrentKey(redstoneProps)), burnableItem.apply(100));
    // signs
    DeferredHolder standingSign = registerNoItem(name + "_sign", () -> new StandingSignBlock(woodType, setIdFromCurrentKey(behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).forceSolidOn().noCollision().strength(1.0F))));
    DeferredHolder wallSign = registerNoItem(name + "_wall_sign", () -> new WallSignBlock(woodType, setIdFromCurrentKey(behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).forceSolidOn().noCollision().strength(1.0F))));
    DeferredHolder hangingSign = registerNoItem(name + "_hanging_sign", () -> new CeilingHangingSignBlock(woodType, setIdFromCurrentKey(behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).forceSolidOn().noCollision().strength(1.0F))));
    DeferredHolder wallHangingSign = registerNoItem(name + "_wall_hanging_sign", () -> new WallHangingSignBlock(woodType, setIdFromCurrentKey(behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).forceSolidOn().noCollision().strength(1.0F))));
    // tell mantle to inject these into the TE




    // sign is included automatically in asItem of the standing sign
    ResourceKey<Item> signItemKey = ResourceKey.create(Registries.ITEM, resource(name + "_sign"));
    this.itemRegister.register(name + "_sign", () -> ItemDeferredRegister.withCurrentItemKey(signItemKey, () -> burnableSignItem.apply((Block) standingSign.get(), (Block) wallSign.get())));
    ResourceKey<Item> hangingSignItemKey = ResourceKey.create(Registries.ITEM, resource(name + "_hanging_sign"));
    this.itemRegister.register(name + "_hanging_sign", () -> ItemDeferredRegister.withCurrentItemKey(hangingSignItemKey, () -> burnableHangingSignItem.apply((Block) hangingSign.get(), (Block) wallHangingSign.get())));
    // finally, return
    return new WoodBlockObject(resource(name), woodType,
                               planks, log, strippedLog, wood, strippedWood,
                               fence, fenceGate, door, trapdoor, pressurePlate, button,
                               standingSign, wallSign, hangingSign, wallHangingSign);
  }


  /* Flower pots */

  /**
   * Registers a potted form of the given block using the vanilla pot
   * @param name  Name of the flower
   * @param block Block to put in the block
   * @return  Potted block instance
   */
  public DeferredHolder registerPotted(String name, Supplier<? extends Block> block) {
    DeferredHolder potted = registerNoItem("potted_" + name, () -> new FlowerPotBlock(block.get(), setIdFromCurrentKey(POTTED_PROPS)));
    return potted;
  }

  /** Registers a potted form of the given block using the vanilla pot */
  public DeferredHolder registerPotted(DeferredHolder block) {
    return registerPotted(block.getId().getPath(), block);
  }

  /** Registers a potted form of the given block using the vanilla pot */
  public DeferredHolder registerPotted(ItemObject<? extends Block> block) {
    return registerPotted(block.getId().getPath(), block);
  }


  /* Enum */

  /**
   * Registers an item with multiple variants, prefixing the name with the value name
   * @param values    Enum values to use for this block
   * @param name      Name of the block
   * @param mapper    Function to get a block for the given enum value
   * @param item      Function to get an item from the block
   * @return  EnumObject mapping between different block types
   */
  public <T extends Enum<T>, B extends Block> EnumObject<T,B> registerEnum(
      T[] values, String name, Function<T,? extends B> mapper, Function<? super B, ? extends BlockItem> item) {
    return registerEnum(values, name, (fullName, value) -> register(fullName, () -> mapper.apply(value), item));
  }

  /**
   * Registers a block with multiple variants, suffixing the name with the value name
   * @param name      Name of the block
   * @param values    Enum values to use for this block
   * @param mapper    Function to get a block for the given enum value
   * @param item      Function to get an item from the block
   * @return  EnumObject mapping between different block types
   */
  public <T extends Enum<T>, B extends Block> EnumObject<T,B> registerEnum(
      String name, T[] values, Function<T,? extends B> mapper, Function<? super B, ? extends BlockItem> item) {
    return registerEnum(name, values, (fullName, value) -> register(fullName, () -> mapper.apply(value), item));
  }

  /**
   * Registers a block with enum variants, but no item form
   * @param values  Enum value list
   * @param name    Suffix after value name
   * @param mapper  Function to map types to blocks
   * @param <T>  Type of enum
   * @param <B>  Type of block
   * @return  Enum object
   */
  public <T extends Enum<T>, B extends Block> EnumObject<T, B> registerEnumNoItem(T[] values, String name, Function<T, ? extends B> mapper) {
    return registerEnum(values, name, (fullName, value) -> registerNoItem(fullName, () -> mapper.apply(value)));
  }

  /** Registers a potted form of the given block using the vanilla pot */
  public <T extends Enum<T> & StringRepresentable, B extends Block> EnumObject<T, FlowerPotBlock> registerPottedEnum(T[] values, String name, EnumObject<T, B> block) {
    EnumObject.Builder<T, FlowerPotBlock> builder = new Builder<>(values[0].getDeclaringClass());
    for (T value : values) {
      Supplier<? extends B> supplier = block.getSupplier(value);
      if (supplier != null) {
        builder.put(value, registerPotted(value.getSerializedName() + "_" + name, supplier));
      }
    }
    return builder.build();
  }

  /** Registers a potted form of the given blocks using the vanilla pot, automatically choosing the values based on the passed object */
  public <T extends Enum<T> & StringRepresentable, B extends Block> EnumObject<T, FlowerPotBlock> registerPottedEnum(String name, EnumObject<T, B> block) {
    Collection<Entry<T,Supplier<? extends B>>> entries = block.entries();
    EnumObject.Builder<T, FlowerPotBlock> builder = new Builder<>(entries.iterator().next().getKey().getDeclaringClass());
    for (Entry<T,Supplier<? extends B>> entry : entries) {
      T value = entry.getKey();
      builder.put(value, registerPotted(value.getSerializedName() + "_" + name, entry.getValue()));
    }
    return builder.build();
  }


  /* Metal */

  /**
   * Creates a new metal item object
   * @param name           Metal name
   * @param tagName        Name to use for tags for this block
   * @param blockSupplier  Supplier for the block
   * @param blockItem      Block item
   * @param itemProps      Properties for the item
   * @return  Metal item object
   */
  public MetalItemObject registerMetal(String name, String tagName, Supplier<Block> blockSupplier, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
    ItemObject<Block> block = register(name + "_block", blockSupplier, blockItem);
    ResourceKey<Item> ingotKey = ResourceKey.create(Registries.ITEM, resource(name + "_ingot"));
    ResourceKey<Item> nuggetKey = ResourceKey.create(Registries.ITEM, resource(name + "_nugget"));
    DeferredHolder ingot = itemRegister.register(name + "_ingot", () -> ItemDeferredRegister.withCurrentItemKey(ingotKey, () -> new Item(ItemDeferredRegister.setIdFromCurrentKey(itemProps))));
    DeferredHolder nugget = itemRegister.register(name + "_nugget", () -> ItemDeferredRegister.withCurrentItemKey(nuggetKey, () -> new Item(ItemDeferredRegister.setIdFromCurrentKey(itemProps))));
    return new MetalItemObject(tagName, block, ingot, nugget);
  }

  /**
   * Creates a new metal item object
   * @param name           Metal name
   * @param blockSupplier  Supplier for the block
   * @param blockItem      Block item
   * @param itemProps      Properties for the item
   * @return  Metal item object
   */
  public MetalItemObject registerMetal(String name, Supplier<Block> blockSupplier, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
    return registerMetal(name, name, blockSupplier, blockItem, itemProps);
  }

  /**
   * Creates a new metal item object
   * @param name        Metal name
   * @param tagName     Name to use for tags for this block
   * @param blockProps  Properties for the block
   * @param blockItem   Block item
   * @param itemProps   Properties for the item
   * @return  Metal item object
   */
  public MetalItemObject registerMetal(String name, String tagName, BlockBehaviour.Properties blockProps, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
    return registerMetal(name, tagName, () -> new Block(setIdFromCurrentKey(blockProps)), blockItem, itemProps);
  }

  /**
   * Creates a new metal item object
   * @param name        Metal name
   * @param blockProps  Properties for the block
   * @param blockItem   Block item
   * @param itemProps   Properties for the item
   * @return  Metal item object
   */
  public MetalItemObject registerMetal(String name, BlockBehaviour.Properties blockProps, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
    return registerMetal(name, name, blockProps, blockItem, itemProps);
  }
  private static class MantleStairBlock extends StairBlock {
    MantleStairBlock(net.minecraft.world.level.block.state.BlockState baseState, Properties properties) {
      super(baseState, properties);
    }
  }

  private static class MantleDoorBlock extends DoorBlock {
    MantleDoorBlock(BlockSetType type, Properties properties) {
      super(type, properties);
    }
  }

  private static class MantlePressurePlateBlock extends PressurePlateBlock {
    MantlePressurePlateBlock(BlockSetType type, Properties properties) {
      super(type, properties);
    }
  }

  private static class MantleButtonBlock extends ButtonBlock {
    MantleButtonBlock(BlockSetType type, int ticksToStayPressed, Properties properties) {
      super(type, ticksToStayPressed, properties);
    }
  }
  private static class MantleTrapDoorBlock extends TrapDoorBlock {
    MantleTrapDoorBlock(BlockSetType type, Properties properties) {
      super(type, properties);
    }
  }
}
