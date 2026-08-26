package slimeknights.mantle.client.model.builder;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.common.ColorLoadable;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import javax.annotation.Nullable;
import java.util.List;

/** Data for a generated item layer model. */
public record LayerData(int color, int luminosity, boolean noTint, @Nullable Identifier renderType) {
  public static final LayerData DEFAULT = new LayerData(-1, 0, false, null);
  public static final RecordLoadable<LayerData> LOADABLE = RecordLoadable.create(
    ColorLoadable.ALPHA.defaultField("color", false, LayerData::color),
    IntLoadable.range(0, 15).defaultField("luminosity", 0, LayerData::luminosity),
    BooleanLoadable.INSTANCE.defaultField("no_tint", false, false, LayerData::noTint),
    Loadables.RESOURCE_LOCATION.nullableField("render_type", LayerData::renderType),
    LayerData::new);
  public static final Loadable<List<LayerData>> LIST_LOADABLE = LOADABLE.list(1);

  @Deprecated(forRemoval = true)
  public static LayerData fromJson(JsonObject json) {
    return LOADABLE.deserialize(json);
  }

  @Deprecated(forRemoval = true)
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    LOADABLE.serialize(this, json);
    return json;
  }
}