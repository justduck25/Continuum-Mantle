package slimeknights.mantle.client.book.repository;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import slimeknights.mantle.client.book.data.SectionData;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DummyRepository extends BookRepository {

  @Override
  public List<SectionData> getSections() {
    return Collections.emptyList();
  }

  @Override
  public Identifier getIdentifier(@Nullable String path, boolean safe) {
    return null;
  }

  @Override
  public Optional<Resource> getLocation(@Nullable Identifier loc) {
    return Optional.empty();
  }

  @Override
  public String resourceToString(@Nullable Resource resource, boolean skipComments) {
    return "";
  }
}
