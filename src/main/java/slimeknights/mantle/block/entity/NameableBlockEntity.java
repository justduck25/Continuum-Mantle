package slimeknights.mantle.block.entity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Extension of tile entity to make it namable
 */
public abstract class NameableBlockEntity extends MantleBlockEntity implements INameableMenuProvider {
	private static final String TAG_CUSTOM_NAME = "CustomName";

	/** Default title for this tile entity */
	@Getter
	private final Component defaultName;
	/** Title set to this tile entity */
	@Getter @Setter
	private Component customName;

	public NameableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Component defaultTitle) {
		super(type, pos, state);
		this.defaultName = defaultTitle;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.customName = BlockEntity.parseCustomNameSafe(input, TAG_CUSTOM_NAME);
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (this.hasCustomName()) {
			output.store(TAG_CUSTOM_NAME, ComponentSerialization.CODEC, this.customName);
		}
	}

	@Override
	public void saveSynced(CompoundTag tags) {
		super.saveSynced(tags);
		if (this.hasCustomName()) {
			ComponentSerialization.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, this.customName).result().ifPresent(tag -> tags.put(TAG_CUSTOM_NAME, tag));
		}
	}
}