package slimeknights.mantle.client.screen.book;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.data.element.TextData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TextDataRenderer {

  /**
   * @deprecated Call drawText with tooltip param and then call drawTooltip separately on the tooltip layer to prevent overlap
   */
  @Deprecated
  public static String drawText(GuiGraphicsExtractor graphics, int x, int y, int boxWidth, int boxHeight, TextData[] data, int mouseX, int mouseY, Font fr, BookScreen parent) {
    List<Component> tooltip = new ArrayList<>();
    String action = drawText(graphics, x, y, boxWidth, boxHeight, data, mouseX, mouseY, fr, tooltip);

    if (!tooltip.isEmpty()) {
      graphics.setComponentTooltipForNextFrame(fr, tooltip, mouseX, mouseY);
    }

    return action;
  }

  // TODO: can we merge this with TextComponentDataRenderer, put the differences in TextData vs TextComponentData?
  public static String drawText(GuiGraphicsExtractor graphics, int x, int y, int boxWidth, int boxHeight, TextData[] data, int mouseX, int mouseY, Font fr, List<Component> tooltip) {
    String action = "";

    int atX = x;
    int atY = y;
    int topY = y;

    float prevScale = 1.F;

    for (TextData item : data) {
      int box1X, box1Y, box1W = 9999, box1H = y + fr.lineHeight;
      int box2X, box2Y = 9999, box2W, box2H;
      int box3X = 9999, box3Y = 9999, box3W, box3H;

      // shouldn't happen, but better safe
      if (item == null) {
        continue;
      }
      // allow specifying linebreak on its own to force a linebreak
      if (item.text == null || item.text.isEmpty()) {
        if (item.linebreak) {
          atX = x;
          atY += scaledLineHeight(fr, prevScale);
        }
        continue;
      }
      // TODO: ditch this, the linebreak field handles it better
      if (item.text.equals("\n")) {
        atX = x;
        atY += scaledLineHeight(fr, prevScale);
        continue;
      }

      if (item.paragraph) {
        atX = x;
        atY += scaledLineHeight(fr, prevScale) * 2;
      }

      prevScale = item.scale;
      int lineHeight = scaledLineHeight(fr, item.scale);

      String modifiers = "";

      if (item.useOldColor) {
        ChatFormatting colFormat = ChatFormatting.getByName(item.color);
        if(colFormat != null) {
          modifiers += colFormat;
        } else {
          modifiers += "unknown color"; // more descriptive than null

          // This will spam the console, but that makes the error more obvious
          Mantle.logger.error("Failed to parse color: " + item.color + " for text rendering.");
        }
      }

      if (item.bold) {
        modifiers += ChatFormatting.BOLD;
      }
      if (item.italic) {
        modifiers += ChatFormatting.ITALIC;
      }
      if (item.underlined) {
        modifiers += ChatFormatting.UNDERLINE;
      }
      if (item.strikethrough) {
        modifiers += ChatFormatting.STRIKETHROUGH;
      }
      if (item.obfuscated) {
        modifiers += ChatFormatting.OBFUSCATED;
      }

      String text = translateString(item.text);

      int remainingHeight = boxHeight - (atY - topY);
      if (remainingHeight <= 0) {
        break;
      }
      String[] split = cropStringBySize(text, modifiers, boxWidth, remainingHeight, boxWidth - (atX - x), fr, item.scale);
      if (split.length == 0) {
        break;
      }

      box1X = atX;
      box1Y = atY;
      box2X = x;
      box2W = x + boxWidth;

      for (int i = 0; i < split.length; i++) {
        if (i == split.length - 1) {
          box3X = atX;
          box3Y = atY;
        }

        String s = split[i];
        drawScaledString(graphics, fr, modifiers + s, atX, atY, item.rgbColor, item.dropshadow, item.scale);

        if (i < split.length - 1) {
          atY += lineHeight;
          atX = x;
        }

        if (i == 0) {
          box2Y = atY;

          if (atX == x) {
            box1W = x + boxWidth;
          } else {
            box1W = atX;
          }
        }
      }

      box2H = atY;

      atX += fr.width(split[split.length - 1]) * item.scale;

      // if specified, include a trailing linebreak, works better than a separate linebreak element on handling whitespace
      if (item.linebreak || atX - x >= boxWidth) {
        atX = x;
        atY += lineHeight;
      }

      box3W = atX;
      box3H = (int) (atY + fr.lineHeight * item.scale);

      boolean mouseInside = (mouseX >= box1X && mouseX <= box1W && mouseY >= box1Y && mouseY <= box1H && box1X != box1W && box1Y != box1H)
                            || (mouseX >= box2X && mouseX <= box2W && mouseY >= box2Y && mouseY <= box2H && box2X != box2W && box2Y != box2H)
                            || (mouseX >= box3X && mouseX <= box3W && mouseY >= box3Y && mouseY <= box3H && box3X != box3W && box1Y != box3H);
      if (item.tooltip != null && item.tooltip.length > 0) {
        if (BookScreen.debug) {
          graphics.fillGradient(box1X,  box1Y,  box1W,      box1H,      0xFF00FF00, 0xFF00FF00);
          graphics.fillGradient(box2X,  box2Y,  box2W,      box2H,      0xFFFF0000, 0xFFFF0000);
          graphics.fillGradient(box3X,  box3Y,  box3W,      box3H,      0xFF0000FF, 0xFF0000FF);
          graphics.fillGradient(mouseX, mouseY, mouseX + 5, mouseY + 5, 0xFFFF00FF, 0xFFFFFF00);
        }

        if (mouseInside) {
          tooltip.addAll(Arrays.asList(item.tooltip));
        }
      }

      if (item.action != null && !item.action.isEmpty()) {
        if (mouseInside) {
          action = item.action;
        }
      }

      if (atY >= topY + boxHeight) {
        graphics.text(fr, "...", x, Math.max(topY, topY + boxHeight - lineHeight), 0xFF000000);
        break;
      }
    }

    if (BookScreen.debug && !action.isEmpty()) {
      tooltip.add(Component.empty());
      tooltip.add(Component.literal("Action: " + action).withStyle(ChatFormatting.GRAY));
    }

    return action;
  }

  public static String translateString(String s) {
    s = s.replace("$$(", "$\0(").replace(")$$", ")\0$");

    while (s.contains("$(") && s.contains(")$") && s.indexOf("$(") < s.indexOf(")$")) {
      String loc = s.substring(s.indexOf("$(") + 2, s.indexOf(")$"));
      s = s.replace("$(" + loc + ")$", I18n.get(loc));
    }

    if (s.indexOf("$(") > s.indexOf(")$") || s.contains(")$")) {
      Mantle.logger.error("[Books] [TextDataRenderer] Detected unbalanced localization symbols \"$(\" and \")$\" in string: \"" + s + "\".");
    }

    return s.replace("$\0(", "$(").replace(")\0$", ")$");
  }

  public static String[] cropStringBySize(String s, String modifiers, int width, int height, Font fr, float scale) {
    return cropStringBySize(s, modifiers, width, height, width, fr, scale);
  }

  public static String[] cropStringBySize(String s, String modifiers, int width, int height, int firstWidth, Font fr, float scale) {
    int lineHeight = scaledLineHeight(fr, scale);
    int maxLines = Math.max(1, height / lineHeight);
    List<String> lines = new ArrayList<>();
    String remaining = s;

    while (!remaining.isEmpty() && lines.size() < maxLines) {
      int lineWidth = lines.isEmpty() ? firstWidth : width;
      int newline = remaining.indexOf('\n');
      String paragraph = newline >= 0 ? remaining.substring(0, newline) : remaining;

      int fit = paragraph.length();
      while (fit > 0 && fr.width(modifiers + paragraph.substring(0, fit)) * scale > lineWidth) {
        fit--;
      }

      if (fit >= paragraph.length()) {
        lines.add(paragraph);
        remaining = newline >= 0 ? remaining.substring(newline + 1) : "";
      } else {
        int split = paragraph.lastIndexOf(' ', Math.max(0, fit));
        if (split <= 0) {
          split = Math.max(1, fit);
        }
        lines.add(paragraph.substring(0, split));
        remaining = StringUtils.stripStart(paragraph.substring(split) + (newline >= 0 ? remaining.substring(newline) : ""), " ");
      }
    }

    return lines.toArray(new String[0]);
  }

  private static int scaledLineHeight(Font fr, float scale) {
    return Math.max(1, (int)Math.ceil(fr.lineHeight * scale));
  }

  /** Gets the number of lines needed to render the given text */
  public static int getLinesForString(String s, String modifiers, int width, String prefix, Font fr) {
    return cropStringBySize(s, modifiers, width, Short.MAX_VALUE, width - fr.width(prefix), fr, 1.0f).length;
  }

  //BEGIN METHODS FROM GUI
  //TODO: does this exist elsewhere now?
  public static void drawScaledString(GuiGraphicsExtractor graphics, Font font, String text, float x, float y, int color, boolean dropShadow, float scale) {
    if ((color & 0xFF000000) == 0) {
      color |= 0xFF000000;
    }

    var poseStack = graphics.pose();
    poseStack.pushMatrix();
    poseStack.translate(x, y);
    poseStack.scale(scale, scale);

    graphics.text(font, text, 0, 0, color, dropShadow);

    poseStack.popMatrix();
  }
  //END METHODS FROM GUI
}
