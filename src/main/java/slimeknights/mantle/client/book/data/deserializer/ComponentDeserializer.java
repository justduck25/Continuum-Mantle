package slimeknights.mantle.client.book.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.lang.reflect.Type;

/** Gson adapter bridging Mantle book JSON to Minecraft's component codec. */
public class ComponentDeserializer implements JsonDeserializer<Component>, JsonSerializer<Component> {
  @Override
  public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(JsonParseException::new);
  }

  @Override
  public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
    return ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, src).getOrThrow(JsonParseException::new);
  }
}