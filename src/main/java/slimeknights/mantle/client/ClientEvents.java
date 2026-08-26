package slimeknights.mantle.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.block.GaugeBlock;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.repository.FileRepository;
import slimeknights.mantle.client.model.FallbackModelLoader;
import slimeknights.mantle.client.model.NBTKeyModel;
import slimeknights.mantle.client.model.RetexturedBlockStateModel;
import slimeknights.mantle.client.model.RetexturedItemModel;
import slimeknights.mantle.client.model.RetexturedModel;
import slimeknights.mantle.client.model.connected.ConnectedModel;
import slimeknights.mantle.client.model.util.ColoredBlockModel;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.client.model.util.SimpleBlockModel;
import slimeknights.mantle.client.render.FluidCuboid;
import slimeknights.mantle.client.render.RenderItem;
import slimeknights.mantle.command.client.MantleClientCommand;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.fluid.texture.FluidTextureManager;
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;
import slimeknights.mantle.registration.MantleRegistrations;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.util.OffhandCooldownTracker;
import slimeknights.mantle.util.RegistryHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = Mantle.modId, value = Dist.CLIENT)
public class ClientEvents {
  /** Called on construct to initiatlize things that need early entry */
  public static void onConstruct() {}

  @SuppressWarnings("ConstantConditions")
  @SubscribeEvent
  static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
    // Custom sign renderers need a NeoForge 26 render-state port.
  }

  @SubscribeEvent
  static void registerModelLoaders(ModelEvent.RegisterLoaders event) {
    event.register(Mantle.getResource("fallback"), FallbackModelLoader.INSTANCE);
    event.register(Mantle.getResource("colored_block"), ColoredBlockModel.LOADER);
    event.register(Mantle.getResource("item_layer"), MantleItemLayerModel.LOADER);
    event.register(Mantle.getResource("simple_block"), SimpleBlockModel.LOADER);
    event.register(Mantle.getResource("connected"), ConnectedModel.LOADER);
    event.register(Mantle.getResource("nbt_key"), NBTKeyModel.LOADER);
    event.register(Mantle.getResource("retextured"), RetexturedModel.LOADER);
  }


  @SubscribeEvent
  static void registerBlockStateModels(RegisterBlockStateModels event) {
    event.registerModel(RetexturedBlockStateModel.Unbaked.ID, RetexturedBlockStateModel.Unbaked.MAP_CODEC);
  }

  @SubscribeEvent
  static void registerItemModels(RegisterItemModelsEvent event) {
    event.register(RetexturedItemModel.Unbaked.ID, RetexturedItemModel.Unbaked.MAP_CODEC);
  }
  @SuppressWarnings("removal")
  @SubscribeEvent
  static void registerListeners(AddClientReloadListenersEvent event) {
    event.addListener(Mantle.getResource("book_loader"), new BookLoader());
    ResourceColorManager.init(event);
    FluidTooltipHandler.init(event);
    FluidTextureManager.init(event);
    event.addListener(Mantle.getResource("fluid_cuboid"), FluidCuboid.REGISTRY);
    event.addListener(Mantle.getResource("render_item_registry"), RenderItem.REGISTRY);
    event.addListener(Mantle.getResource("render_item_state_registry"), RenderItem.STATE_REGISTRY);
  }

  @SubscribeEvent
  static void clientSetup(FMLClientSetupEvent event) {
    event.enqueueWork(() -> RegistrationHelper.forEachWoodType(Sheets::addWoodType));

    BookLoader.registerBook(Mantle.getResource("test"), new FileRepository(Mantle.getResource("books/test")));
    MantleClientCommand.init();
  }


  @SubscribeEvent
  static void commonSetup(FMLCommonSetupEvent event) {
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderGuiLayerEvent.Post.class, ClientEvents::renderOffhandAttackIndicator);
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderGuiLayerEvent.Post.class, ClientEvents::renderGaugeTooltip);
    NeoForge.EVENT_BUS.register(new ExtraHeartRenderHandler());
  }

  // registered with FORGE bus
  private static void renderOffhandAttackIndicator(RenderGuiLayerEvent.Post event) {}

  /** Renders the tooltip when targeting the gauge block */
  private static void renderGaugeTooltip(RenderGuiLayerEvent.Post event) {
    if (!VanillaGuiLayers.CROSSHAIR.equals(event.getName())) {
      return;
    }
    // must not be in a screen, though chat is fine
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.screen != null && minecraft.screen.getClass() != ChatScreen.class) {
      return;
    }
    // must have a hit result
    if (minecraft.level == null || minecraft.hitResult == null || minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
      return;
    }
    BlockHitResult blockHit = (BlockHitResult) minecraft.hitResult;
    BlockPos pos = blockHit.getBlockPos();

    // must be targeting a gauge
    BlockState targeted = minecraft.level.getBlockState(blockHit.getBlockPos());
    if (!targeted.is(MantleTags.Blocks.GAUGES)) {
      return;
    }
    BlockEntity gaugeContainer;
    Direction side;
    if (targeted.is(MantleTags.Blocks.ATTACHED_GAUGES)) {
      side = targeted.getValue(BlockStateProperties.FACING);
      gaugeContainer = minecraft.level.getBlockEntity(pos.relative(side.getOpposite()));
    } else {
      side = blockHit.getDirection();
      gaugeContainer = minecraft.level.getBlockEntity(pos);
    }
    // must have a block entity behind the gauge that is not blacklisted
    if (gaugeContainer == null || RegistryHelper.contains(BuiltInRegistries.BLOCK_ENTITY_TYPE, MantleTags.BlockEntities.GAUGE_BLACKLIST, gaugeContainer.getType())) {
      return;
    }
    // block entity must have a fluid handler
    ResourceHandler<FluidResource> handler = minecraft.level.getCapability(Capabilities.Fluid.BLOCK, pos, targeted, gaugeContainer, side);
    if (handler == null || handler.size() <= 0) {
      return;
    }
    // if the fluid is empty, just render the capacity
    FluidResource fluidRes = handler.getResource(0);
    int amount = handler.getAmountAsInt(0);
    FluidStack fluid = fluidRes.toStack(amount);
    int capacity = handler.getCapacityAsInt(0, fluidRes);
    List<Component> tooltip;
    if (fluid.isEmpty() || amount <= 0) {
      tooltip = List.of(GaugeBlock.formatCapacity(capacity));
    } else if (RegistryHelper.contains(BuiltInRegistries.BLOCK_ENTITY_TYPE, MantleTags.BlockEntities.HIDES_GAUGE_AMOUNT, gaugeContainer.getType())) {
      // in the tag, don't show capacity
      Identifier id = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
      tooltip = new ArrayList<>(3);
      tooltip.add(fluidRes.getHoverName());
      FluidTooltipHandler.appendAdvanced(id, tooltip);
      tooltip.add(GaugeBlock.formatCapacity(capacity).withStyle(ChatFormatting.GRAY));
      tooltip.add(FluidTooltipHandler.formatModName(id));
    } else {
      // render full fluid tooltip
      tooltip = FluidTooltipHandler.getFluidTooltip(fluid);
    }

    int x = minecraft.getWindow().getGuiScaledWidth() / 2;
    int y = minecraft.getWindow().getGuiScaledHeight() / 2;
    event.getGuiGraphics().setComponentTooltipForNextFrame(minecraft.font, tooltip, x, y);
  }
}
