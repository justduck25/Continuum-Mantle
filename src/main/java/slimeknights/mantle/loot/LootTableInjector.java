package slimeknights.mantle.loot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.fml.ModList;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.listener.IEarlyReloadListener;
import slimeknights.mantle.loot.LootTableInjection.LootPoolInjection;
import slimeknights.mantle.util.JsonHelper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Class handling injecting additional entries into loot tables. */
public enum LootTableInjector implements IEarlyReloadListener {
  INSTANCE;

  /** Datapack folder for the injector. */
  public static final String FOLDER = "mantle/loot_injectors";

  /** Initializes the loot table injector. */
  public static void init() {
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, AddServerReloadListenersEvent.class, event -> {
      event.addListener(Mantle.getResource("loot_table_injectors"), INSTANCE);
      INSTANCE.context = event.getConditionContext();
    });
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, LootTableLoadEvent.class, INSTANCE::lootTableLoad);
  }

  /** Condition context for preventing load. */
  private IContext context = IContext.EMPTY;
  /** Map of injections to use on loot table load. */
  private Map<Identifier,LootTableInjection> injections = Collections.emptyMap();
  /** If true, we already loaded mod bundled injectors for the current runtime. */
  private boolean loadedModFileInjections = false;

  @Override
  public void onResourceManagerReload(ResourceManager manager) {
    Map<Identifier,LootTableInjection.Builder> builders = new HashMap<>();
    int loaded = 0;
    for (Entry<Identifier,Resource> entry : manager.listResources(FOLDER, loc -> loc.getPath().endsWith(".json")).entrySet()) {
      try (Reader reader = entry.getValue().openAsReader()) {
        JsonObject json = GsonHelper.fromJson(JsonHelper.DEFAULT_GSON, reader, JsonObject.class);
        if (json != null) {
          if (!json.keySet().isEmpty() && conditionsMatched(json)) {
            LootTableInjection injection = LootTableInjection.LOADABLE.deserialize(json);
            LootTableInjection.Builder builder = builders.computeIfAbsent(injection.name(), id -> new LootTableInjection.Builder());
            for (LootPoolInjection pool : injection.pools()) {
              builder.addToPool(pool);
            }
            loaded++;
          }
        } else {
          Mantle.logger.error("Couldn't parse loot table injection from {} as it's null or empty", entry.getKey());
        }
      } catch (IllegalArgumentException | IOException | JsonParseException ex) {
        Mantle.logger.error("Couldn't parse loot injection from {}", entry.getKey(), ex);
      }
    }

    injections = builders.entrySet().stream().map(entry -> entry.getValue().build(entry.getKey()))
                         .collect(Collectors.toUnmodifiableMap(LootTableInjection::name, Function.identity()));
  }


  /** Loads bundled mod injectors before loot tables finish loading. Datapack reload listeners run too late for NeoForge 26.1 loot registries. */
  private synchronized void loadModFileInjections() {
    if (loadedModFileInjections) {
      return;
    }
    loadedModFileInjections = true;

    Map<Identifier,LootTableInjection.Builder> builders = new HashMap<>();
    AtomicInteger loaded = new AtomicInteger();
    String folder = "/" + FOLDER + "/";
    ModList.get().forEachModFile(modFile -> modFile.getContents().visitContent("data", (path, resource) -> {
      String normalized = path.replace('\\', '/');
      if (normalized.endsWith(".json") && normalized.contains(folder)) {
        try (Reader reader = new InputStreamReader(modFile.getContents().openFile(path), StandardCharsets.UTF_8)) {
          JsonObject json = GsonHelper.fromJson(JsonHelper.DEFAULT_GSON, reader, JsonObject.class);
          if (json != null && !json.keySet().isEmpty() && conditionsMatched(json)) {
            LootTableInjection injection = LootTableInjection.LOADABLE.deserialize(json);
            LootTableInjection.Builder builder = builders.computeIfAbsent(injection.name(), id -> new LootTableInjection.Builder());
            for (LootPoolInjection pool : injection.pools()) {
              builder.addToPool(pool);
            }
            loaded.incrementAndGet();
          }
        } catch (IllegalArgumentException | IOException | JsonParseException ex) {
          Mantle.logger.error("Couldn't parse bundled loot injection from {}", normalized, ex);
        }
      }
    }));

    if (!builders.isEmpty()) {
      injections = builders.entrySet().stream().map(entry -> entry.getValue().build(entry.getKey()))
                           .collect(Collectors.toUnmodifiableMap(LootTableInjection::name, Function.identity()));
    }
  }
  /** Checks NeoForge conditions using the context supplied by the current reload. */
  private boolean conditionsMatched(JsonObject json) {
    RegistryOps<com.google.gson.JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    return ICondition.conditionsMatched(new ConditionalOps<>(registryOps, context), json);
  }

  /** Called on loot table load to handle the actual injection. */
  private void lootTableLoad(LootTableLoadEvent event) {
    if (injections.isEmpty()) {
      loadModFileInjections();
    }
    LootTableInjection injection = injections.get(event.getName());
    if (injection != null) {
      LootTable table = event.getTable();
      for (LootPoolInjection pool : injection.pools()) {
        pool.inject(table);
      }
    }
  }
}