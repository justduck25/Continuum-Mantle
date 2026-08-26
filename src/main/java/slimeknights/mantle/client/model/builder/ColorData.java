package slimeknights.mantle.client.model.builder;

import com.google.gson.JsonObject;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.common.ColorLoadable;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import javax.annotation.Nullable;
import java.util.List;

/** Data for a colored model element. */
public record ColorData(int color, @Deprecated int luminosity, @Nullable Boolean uvlock) {
  public static final ColorData DEFAULT = new ColorData(-1, -1, null);
  public static final RecordLoadable<ColorData> LOADABLE = RecordLoadable.create(
    ColorLoadable.ALPHA.defaultField("color", false, ColorData::color),
    IntLoadable.range(-1, 15).defaultField("luminosity", -1, ColorData::luminosity),
    BooleanLoadable.INSTANCE.nullableField("uvlock", ColorData::uvlock),
    ColorData::new);
  public static final Loadable<List<ColorData>> LIST_LOADABLE = LOADABLE.list(0);

  public boolean isUvLock(boolean defaultLock) {
    return uvlock == null ? defaultLock : uvlock;
  }

  @Deprecated(forRemoval = true)
  public static ColorData fromJson(JsonObject json) {
    return LOADABLE.deserialize(json);
  }

  @Deprecated(forRemoval = true)
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    LOADABLE.serialize(this, json);
    return json;
  }
}