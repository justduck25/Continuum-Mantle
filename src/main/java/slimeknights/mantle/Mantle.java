package slimeknights.mantle;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.config.Config;
import slimeknights.mantle.datagen.MantleBlockTagProvider;
import slimeknights.mantle.datagen.MantleFluidTagProvider;
import slimeknights.mantle.datagen.MantleFluidTooltipProvider;
import slimeknights.mantle.datagen.MantleFluidTransferProvider;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferManager;
import slimeknights.mantle.loot.LootTableInjector;
import slimeknights.mantle.loot.MantleLoot;
import slimeknights.mantle.datagen.MantleMenuTagProvider;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.recipe.MantleRecipes;

/** Central mod object for Mantle. */
@Mod(Mantle.modId)
public class Mantle {
  public static final String modId = "mantle";
  public static final Logger logger = LogManager.getLogger("Mantle");
  /** Common tags now use the vanilla/NeoForge common namespace. */
  public static final String COMMON = "c";

  public static Mantle instance;

  public Mantle(IEventBus modEventBus, ModContainer modContainer) {
    instance = this;
    modContainer.registerConfig(Type.CLIENT, Config.CLIENT_SPEC);
    modContainer.registerConfig(Type.SERVER, Config.SERVER_SPEC);
    modEventBus.addListener(MantleNetwork::registerPackets);
    modEventBus.addListener(this::gatherClientData);
    MantleRecipes.init(modEventBus);
    modEventBus.addListener(MantleLoot::registerGlobalLootModifiers);
    LootTableInjector.init();
    FluidContainerTransferManager.INSTANCE.init();
  }


  /** Registers generated resources. NeoForge recommends registering all Mantle providers on clientData. */
  private void gatherClientData(GatherDataEvent.Client event) {
    event.createProvider(MantleFluidTooltipProvider::new);
    event.createProvider(MantleBlockTagProvider::new);
    event.createProvider(MantleFluidTagProvider::new);
    event.createProvider(MantleMenuTagProvider::new);
    event.createProvider(MantleFluidTransferProvider::new);
  }
  /** Gets a resource location for Mantle. */
  public static Identifier getResource(String name) {
    return Identifier.fromNamespaceAndPath(modId, name);
  }

  /** Gets a resource location for the common namespace. */
  public static Identifier commonResource(String name) {
    return Identifier.fromNamespaceAndPath(COMMON, name);
  }

  /** Makes a translation key for the given name. */
  public static String makeDescriptionId(String base, String name) {
    return Util.makeDescriptionId(base, getResource(name));
  }

  /** Makes a translation text component for the given name. */
  public static MutableComponent makeComponent(String base, String name) {
    return Component.translatable(makeDescriptionId(base, name));
  }

  /** Makes a formatted translation text component for the given name. */
  public static MutableComponent makeComponent(String base, String name, Object... args) {
    return Component.translatable(makeDescriptionId(base, name), args);
  }
}
