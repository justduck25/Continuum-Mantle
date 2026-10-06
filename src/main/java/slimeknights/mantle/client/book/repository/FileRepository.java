package slimeknights.mantle.client.book.repository;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.apache.commons.io.IOUtils;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.SectionData;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class FileRepository extends BookRepository {

  public final Identifier location;

  public FileRepository(Identifier location) {
    this.location = location;
  }

  private final java.util.Map<String, Identifier> identifierCache = new java.util.concurrent.ConcurrentHashMap<>();
  private final java.util.Map<Identifier, Boolean> resourceExistsCache = new java.util.concurrent.ConcurrentHashMap<>();
  private String lastLang = null;

  public void clearCache() {
    this.identifierCache.clear();
    this.resourceExistsCache.clear();
  }

  @Override
  public List<SectionData> getSections() {
    return new ArrayList<>(Arrays.asList(BookLoader.getGson().fromJson(this.resourceToString(this.getResource(this.getIdentifier("index.json"))), SectionData[].class)));
  }

  @Override
  public boolean resourceExists(@Nullable Identifier location) {
    if (location == null) {
      return false;
    }
    return this.resourceExistsCache.computeIfAbsent(location, super::resourceExists);
  }

  @Override
  public Identifier getIdentifier(@Nullable String path, boolean safe) {
    if (path == null) {
      return safe ? Identifier.withDefaultNamespace("") : null;
    }

    final String lang = (Minecraft.getInstance().getLanguageManager() != null) ? Minecraft.getInstance().getLanguageManager().getSelected() : null;
    if (!java.util.Objects.equals(this.lastLang, lang)) {
      clearCache();
      this.lastLang = lang;
    }

    String cacheKey = (safe ? "s:" : "u:") + path;
    return this.identifierCache.computeIfAbsent(cacheKey, k -> resolveIdentifier(path, safe, lang));
  }

  private Identifier resolveIdentifier(String path, boolean safe, @Nullable String langPath) {
    if (!path.contains(":")) {
      String defaultLangPath = "en_us";
      Identifier res;

      if (langPath != null) {
        res = Identifier.parse(this.location + "/" + langPath + "/" + path);
        if (this.resourceExists(res)) {
          return res;
        }
      }
      res = Identifier.parse(this.location + "/" + defaultLangPath + "/" + path);
      if (this.resourceExists(res)) {
        return res;
      }
      res = Identifier.parse(this.location + "/" + path);
      if (this.resourceExists(res)) {
        return res;
      }
    } else {
      Identifier res = Identifier.parse(path);
      if (this.resourceExists(res)) {
        return res;
      }
    }

    return safe ? Identifier.withDefaultNamespace("") : null;
  }

  @Override
  public Optional<Resource> getLocation(@Nullable Identifier loc) {
    if (loc == null) {
      return Optional.empty();
    }
    return Minecraft.getInstance().getResourceManager().getResource(loc);
  }

  @Override
  public String resourceToString(@Nullable Resource resource, boolean skipComments) {
    if (resource == null) {
      return "";
    }

    try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
      StringBuilder builder = new StringBuilder();
      String line;
      boolean isLongComment = false;

      while ((line = reader.readLine()) != null) {
        String s = line.trim() + "\n";

        // Comment skipper
        if (skipComments) {
          if (isLongComment) {
            if (s.endsWith("*/\n") || s.contains("*/")) {
              isLongComment = false;
            }
            continue;
          } else {
            if (s.startsWith("/*")) {
              isLongComment = true;
              continue;
            }
          }
          if (s.startsWith("//")) {
            continue;
          }
        }

        builder.append(s);
      }

      return builder.toString().trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return "";
  }
}
