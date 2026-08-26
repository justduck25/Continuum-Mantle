package slimeknights.mantle.registration.object;

import net.minecraft.resources.Identifier;

/** Interface for an object that holds its own name, used to simplify some utilities */
public interface IdAwareObject {
  /** Gets the ID for this object */
  Identifier getId();
}
