// Credit to Immersive Engineering and blusunrize for this class
// See: https://github.com/BluSunrize/ImmersiveEngineering/blob/1.18/src/main/java/blusunrize/immersiveengineering/common/util/fakeworld/FakeSpawnInfo.java
package slimeknights.mantle.client.book.structure.level;

import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;

public class FakeLevelData implements WritableLevelData {
  private static final GameRules RULES = new GameRules(FeatureFlags.DEFAULT_FLAGS);

  private LevelData.RespawnData respawnData = LevelData.RespawnData.DEFAULT;

  @Override
  public void setSpawn(LevelData.RespawnData respawnData) {
    this.respawnData = respawnData;
  }

  @Override
  public LevelData.RespawnData getRespawnData() {
    return respawnData;
  }

  @Override
  public long getGameTime() {
    return 0;
  }

  public long getDayTime() {
    return 0;
  }

  public boolean isThundering() {
    return false;
  }

  public boolean isRaining() {
    return false;
  }

  public void setRaining(boolean isRaining) {}

  @Override
  public boolean isHardcore() {
    return false;
  }

  public GameRules getGameRules() {
    return RULES;
  }

  @Override
  public Difficulty getDifficulty() {
    return Difficulty.PEACEFUL;
  }

  @Override
  public boolean isDifficultyLocked() {
    return false;
  }
}