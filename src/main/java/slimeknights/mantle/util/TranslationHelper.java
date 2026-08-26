package slimeknights.mantle.util;

import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@SuppressWarnings("WeakerAccess")
public class TranslationHelper {
  private TranslationHelper() {}

  public static final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###,###.##", DecimalFormatSymbols.getInstance(Locale.US));

  public static boolean canTranslate(String key) {
    return Language.getInstance().has(key);
  }

  public static boolean canTranslate(String key, String attempted) {
    return !key.equals(attempted);
  }

  public static void addOptionalTooltip(ItemStack stack, List<Component> tooltip) {
    addOptionalTooltip(stack.getItem().getDescriptionId() + ".tooltip", tooltip);
  }

  public static void addOptionalTooltip(ItemStack stack, Consumer<Component> tooltip) {
    addOptionalTooltip(stack.getItem().getDescriptionId() + ".tooltip", tooltip);
  }

  public static void addOptionalTooltip(String key, List<Component> tooltip) {
    String translated = Language.getInstance().getOrDefault(key);
    if (canTranslate(key, translated)) {
      addEachLine(translated, tooltip);
    }
  }

  public static void addOptionalTooltip(String key, Consumer<Component> tooltip) {
    String translated = Language.getInstance().getOrDefault(key);
    if (canTranslate(key, translated)) {
      addEachLine(translated, tooltip);
    }
  }

  public static void addEachLine(String text, List<Component> tooltip) {
    for (String string : text.split("\n")) {
      tooltip.add(Component.literal(string).withStyle(ChatFormatting.GRAY));
    }
  }

  public static void addEachLine(String text, Consumer<Component> tooltip) {
    for (String string : text.split("\n")) {
      tooltip.accept(Component.literal(string).withStyle(ChatFormatting.GRAY));
    }
  }

  @Nullable
  public static String convertNewlines(@Nullable String line) {
    if (line == null) {
      return null;
    }
    int j;
    while ((j = line.indexOf("\\n")) >= 0) {
      line = line.substring(0, j) + '\n' + line.substring(j + 2);
    }
    return line;
  }
}