package slimeknights.mantle.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * This utility contains helpers to handle the NBT for retexturable blocks
 */
public final class RetexturedHelper {
  private RetexturedHelper() {}

  public static final String KEY_ID = Mantle.makeDescriptionId("block", "retextured.id");
  public static final String TAG_TEXTURE = "texture";
  public static final ModelProperty<Block> BLOCK_PROPERTY = new ModelProperty<>(block -> block != Blocks.AIR);

  public static String getTextureName(@Nullable CompoundTag nbt) {
    if (nbt == null) {
      return "";
    }
    return nbt.getStringOr(TAG_TEXTURE, "");
  }

  public static String getTextureName(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data == null ? "" : getTextureName(data.copyTag());
  }

  public static String getTextureName(Block block) {
    if (block == Blocks.AIR) {
      return "";
    }
    return Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)).toString();
  }

  public static Block getBlock(String name) {
    if (!name.isEmpty()) {
      Identifier location = Identifier.tryParse(name);
      if (location != null) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.parse(name));
      }
    }
    return Blocks.AIR;
  }

  public static Block getTexture(ItemStack stack) {
    return getBlock(getTextureName(stack));
  }

  public static void setTexture(@Nullable CompoundTag nbt, String texture) {
    if (nbt != null) {
      if (texture.isEmpty()) {
        nbt.remove(TAG_TEXTURE);
      } else {
        nbt.putString(TAG_TEXTURE, texture);
      }
    }
  }

  public static ItemStack setTexture(ItemStack stack, String name) {
    CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> setTexture(tag, name));
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    if (data != null && data.isEmpty()) {
      stack.remove(DataComponents.CUSTOM_DATA);
    }
    return stack;
  }

  public static ItemStack setTexture(ItemStack stack, @Nullable Block block) {
    if (block == null || block == Blocks.AIR) {
      return setTexture(stack, "");
    }
    return setTexture(stack, BuiltInRegistries.BLOCK.getKey(block).toString());
  }

  public static void onTextureUpdated(BlockEntity self) {
    Level level = self.getLevel();
    if (level != null) {
      if (level.isClientSide()) {
        self.requestModelDataUpdate();
      }
      BlockState state = self.getBlockState();
      level.sendBlockUpdated(self.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
    }
  }

  public static ModelData.Builder getModelDataBuilder(Block block) {
    if (block == Blocks.AIR) {
      block = null;
    }
    return ModelData.builder().with(BLOCK_PROPERTY, block);
  }

  public static ModelData getModelData(Block block) {
    return getModelDataBuilder(block).build();
  }

  public static void addTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
    Block block = getTexture(stack);
    if (block != Blocks.AIR) {
      tooltip.add(block.getName().withStyle(ChatFormatting.GRAY));
      if (flag.isAdvanced()) {
        tooltip.add(Component.translatable(KEY_ID, BuiltInRegistries.BLOCK.getKey(block).toString()).withStyle(ChatFormatting.DARK_GRAY));
      }
    }
  }

  @Deprecated(forRemoval = true)
  public static void addTooltip(ItemStack stack, List<Component> tooltip) {
    addTooltip(stack, tooltip, TooltipFlag.NORMAL);
  }

  @SuppressWarnings("deprecation")
  public static boolean addTagVariants(Predicate<ItemStack> tab, ItemLike block, TagKey<Item> tag) {
    boolean added = false;
    for (Holder<Item> candidate : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
      if (!candidate.isBound()) {
        continue;
      }
      Item item = candidate.value();
      if (item == block.asItem()) {
        continue;
      }
      if (!(item instanceof BlockItem blockItem)) {
        continue;
      }
      added = true;
      if (tab.test(RetexturedHelper.setTexture(new ItemStack(block), blockItem.getBlock()))) {
        break;
      }
    }
    return added;
  }
}
