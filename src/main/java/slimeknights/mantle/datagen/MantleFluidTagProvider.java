package slimeknights.mantle.datagen;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.Mantle;

import java.util.concurrent.CompletableFuture;

import static slimeknights.mantle.datagen.MantleTags.Fluids.BEETROOT_SOUP;
import static slimeknights.mantle.datagen.MantleTags.Fluids.LAVA;
import static slimeknights.mantle.datagen.MantleTags.Fluids.MUSHROOM_STEW;
import static slimeknights.mantle.datagen.MantleTags.Fluids.RABBIT_STEW;
import static slimeknights.mantle.datagen.MantleTags.Fluids.SOUP;
import static slimeknights.mantle.datagen.MantleTags.Fluids.WATER;

/** Provider for tags added by mantle, generally not useful for other mods */
@Internal
public class MantleFluidTagProvider extends FluidTagsProvider {
  public MantleFluidTagProvider(PackOutput output, CompletableFuture<Provider> holders) {
    super(output, holders, Mantle.modId);
  }

  @Override
  protected void addTags(Provider pProvider) {
    this.tag(WATER).add(Fluids.WATER, Fluids.FLOWING_WATER);
    this.tag(LAVA).add(Fluids.LAVA, Fluids.FLOWING_LAVA);
    this.tag(SOUP)
      .addOptionalTag(BEETROOT_SOUP)
      .addOptionalTag(MUSHROOM_STEW)
      .addOptionalTag(RABBIT_STEW);
  }

  @Override
  public String getName() {
    return "Mantle Fluid Tag Provider";
  }
}
