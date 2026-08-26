package slimeknights.mantle.loot;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Record holding a list of entries to inject into the given loot table. */
public record LootTableInjection(Identifier name, List<LootPoolInjection> pools) {
  public static final RecordLoadable<LootTableInjection> LOADABLE = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("name", LootTableInjection::name),
    LootPoolInjection.LOADABLE.list(1).requiredField("pools", LootTableInjection::pools),
    LootTableInjection::new);

  /** Record holding a list of entries to inject into the given pool. */
  public record LootPoolInjection(String name, LootPoolEntryContainer[] entries) {
    private static final VarHandle POOL_ENTRIES = findPoolHandle("entries", List.class);
    private static final VarHandle POOL_CONDITIONS = findPoolHandle("conditions", List.class);
    private static final VarHandle POOL_FUNCTIONS = findPoolHandle("functions", List.class);
    private static final Constructor<LootPool> POOL_CONSTRUCTOR = findPoolConstructor();

    public static final RecordLoadable<LootPoolInjection> LOADABLE = RecordLoadable.create(
      StringLoadable.DEFAULT.requiredField("name", LootPoolInjection::name),
      Loadables.LOOT_ENTRY.list(1).requiredField("entries", pool -> List.of(pool.entries)),
      LootPoolInjection::new);

    public LootPoolInjection(String name, List<LootPoolEntryContainer> entries) {
      this(name, entries.toArray(new LootPoolEntryContainer[0]));
    }

    /** Injects this into the given loot pool. */
    @SuppressWarnings("unchecked")
    public void inject(LootTable table) {
      LootPool pool = table.getPool(name);
      String poolName = name;
      if (pool == null && "main".equals(name)) {
        poolName = "pool0";
        pool = table.getPool(poolName);
      }
      if (pool != null) {
        List<LootPoolEntryContainer> poolEntries = new ArrayList<>((List<LootPoolEntryContainer>)POOL_ENTRIES.get(pool));
        poolEntries.addAll(List.of(entries));
        LootPool replacement = createPool(poolEntries, (List<LootItemCondition>)POOL_CONDITIONS.get(pool), (List<LootItemFunction>)POOL_FUNCTIONS.get(pool), pool.getRolls(), pool.getBonusRolls(), pool.getName());
        table.removePool(poolName);
        table.addPool(replacement);
      } else {
        Mantle.logger.warn("Failed to inject loot into {} pool {}", table.getLootTableId(), name);
      }
    }

    private static LootPool createPool(List<LootPoolEntryContainer> entries, List<LootItemCondition> conditions, List<LootItemFunction> functions, NumberProvider rolls, NumberProvider bonusRolls, String name) {
      try {
        return POOL_CONSTRUCTOR.newInstance(entries, conditions, functions, rolls, bonusRolls, Optional.ofNullable(name));
      } catch (ReflectiveOperationException ex) {
        throw new IllegalStateException("Failed to rebuild loot pool " + name, ex);
      }
    }

    private static Constructor<LootPool> findPoolConstructor() {
      try {
        Constructor<LootPool> constructor = LootPool.class.getDeclaredConstructor(List.class, List.class, List.class, NumberProvider.class, NumberProvider.class, Optional.class);
        constructor.setAccessible(true);
        return constructor;
      } catch (ReflectiveOperationException ex) {
        throw new ExceptionInInitializerError(ex);
      }
    }

    private static VarHandle findPoolHandle(String field, Class<?> type) {
      try {
        return MethodHandles.privateLookupIn(LootPool.class, MethodHandles.lookup()).findVarHandle(LootPool.class, field, type);
      } catch (ReflectiveOperationException ex) {
        throw new ExceptionInInitializerError(ex);
      }
    }
  }

  /** Builder instance for a loot table injection. */
  public static class Builder {
    private final Map<String,List<LootPoolEntryContainer>> pools = new LinkedHashMap<>();

    /** Inserts the given entries into the pool. */
    @CanIgnoreReturnValue
    public Builder addToPool(String name, LootPoolEntryContainer... entries) {
      Collections.addAll(pools.computeIfAbsent(name, n -> new ArrayList<>()), entries);
      return this;
    }

    /** Inserts the given entries into the pool. */
    @CanIgnoreReturnValue
    public Builder addToPool(LootPoolInjection injection) {
      return addToPool(injection.name, injection.entries);
    }

    /** Builds the list of injections. */
    public LootTableInjection build(Identifier name) {
      return new LootTableInjection(name, pools.entrySet().stream().map(entry -> new LootPoolInjection(entry.getKey(), List.copyOf(entry.getValue()))).toList());
    }
  }
}