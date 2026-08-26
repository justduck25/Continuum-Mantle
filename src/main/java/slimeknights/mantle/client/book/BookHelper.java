package slimeknights.mantle.client.book;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import slimeknights.mantle.util.BookPageHelper;

import javax.annotation.Nullable;

public class BookHelper {

  public static final String BOOK_COMPOUND = "mantle";
  public static final String BOOK_DATA_COMPOUND = "book";

  public static final String NBT_CURRENT_PAGE = "current_page";

  public static String getCurrentSavedPage(@Nullable ItemStack item) {
    if (item != null && !item.isEmpty()) {
      CustomData customData = item.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
        CompoundTag root = customData.copyTag();
        CompoundTag mantle = root.getCompound(BOOK_COMPOUND).orElse(null);
        CompoundTag book = mantle == null ? null : mantle.getCompound(BOOK_DATA_COMPOUND).orElse(null);
        if (book != null) {
          return book.getString(NBT_CURRENT_PAGE).orElse("");
        }
      }
    }
    return "";
  }

  public static void writeSavedPageToBook(ItemStack stack, String currentPage) {
    BookPageHelper.writeSavedPageToBook(stack, currentPage);
  }
}