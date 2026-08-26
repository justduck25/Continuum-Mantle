package slimeknights.mantle.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Shared server-safe storage for the last page opened in a Mantle book. */
public final class BookPageHelper {
  private static final String MANTLE = "mantle";
  private static final String BOOK = "book";
  private static final String PAGE = "current_page";

  private BookPageHelper() {}

  public static void writeSavedPageToBook(ItemStack stack, String page) {
    CompoundTag root = stack.get(DataComponents.CUSTOM_DATA) == null
        ? new CompoundTag() : stack.get(DataComponents.CUSTOM_DATA).copyTag();
    CompoundTag mantle = root.getCompound(MANTLE).orElseGet(CompoundTag::new);
    CompoundTag book = mantle.getCompound(BOOK).orElseGet(CompoundTag::new);
    book.putString(PAGE, page);
    mantle.put(BOOK, book);
    root.put(MANTLE, mantle);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
  }
}