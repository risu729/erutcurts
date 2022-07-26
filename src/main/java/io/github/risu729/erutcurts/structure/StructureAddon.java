/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts.structure;

import io.github.risu729.erutcurts.Erutcurts;
import io.github.risu729.erutcurts.util.FileUtil;
import io.github.risu729.mcbe.manifest4j.Header;
import io.github.risu729.mcbe.manifest4j.Manifest;
import io.github.risu729.mcbe.manifest4j.Metadata;
import io.github.risu729.mcbe.manifest4j.Module_;
import io.github.risu729.mcbe.manifest4j.SemVer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class StructureAddon implements AutoCloseable {

  private static final Metadata.GeneratedWith ERUTCURTS_GENERATED_WITH =
      new Metadata.GeneratedWith.Builder()
          .name("Erutcurts")
          .versions(SemVer.fromString(Erutcurts.VERSION))
          .build();

  private static final Path DEFAULT_PACK_ICON =
      Path.of("src", "main", "resources", "default_pack_icon.png");

  private static final Pattern DIRECTORY_NAME_REGEX =
      Pattern.compile(
          "^(?!^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$)([^\\.<>:\"/\\\\\\|\\?\\*\\x00-\\x20\\x7f][^\\.<>:\"/\\\\\\|\\?\\*\\x00-\\x1f\\x7f]{0,253}[^\\.<>:\"/\\\\\\|\\?\\*\\x00-\\x20\\x7f]|[^\\.<>:\"/\\\\\\|\\?\\*\\x00-\\x20\\x7f])$",
          Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private final String packName;
  private final Manifest manifest;
  private final Path packIcon;
  private final TreeMap<String, Path> structures; // necessarry at least 1
  private final Path tempDir;

  public static StructureAddon of(Path... structures) {
    return new Builder().structures(structures).build();
  }

  public static StructureAddon of(Collection<Path> structures) {
    return new Builder().structures(structures).build();
  }

  public static StructureAddon fromBehavior(Path pack) throws IOException {
    return AddonExtractor.extractBehavior(pack);
  }

  public Path generateBehavior(Path target) throws IOException {
    return AddonGenerator.generateBehavior(this, target);
  }

  /*
  public Path generateWorld(Path target) {
    return target; // TODO
  }
  */

  public String getPackName() {
    return packName;
  }

  public Manifest getManifest() {
    return manifest;
  }

  public Path getPackIcon() {
    return packIcon;
  }

  @SuppressWarnings("unchecked")
  public Collection<Path> getStructures() {
    return ((TreeMap<String, Path>) structures.clone()).values();
  }

  @SuppressWarnings("unchecked")
  public Set<String> getStructureNames() {
    return ((TreeMap<String, Path>) structures.clone()).keySet();
  }

  public Path getTempDir() {
    return tempDir;
  }

  public static class Builder {

    private static final Set<String> PACK_ICON_EXTENSIONS = Set.of("png");

    private String packName;
    private Manifest manifest;
    private Path packIcon;
    private TreeMap<String, Path> structures; // necessarry at least 1

    public Builder() {}

    public Builder(StructureAddon other) {
      packName(other.packName);
      manifest(other.manifest);
      packIcon(other.packIcon);
      structures(other.structures.values());
    }

    public Builder packName(String packName) {
      if (packName != null && !DIRECTORY_NAME_REGEX.matcher(packName).matches()) {
        throw new IllegalArgumentException("invalid directory name: " + packName);
      }
      this.packName = packName;
      return this;
    }

    public Builder manifest(Manifest manifest) {
      if (manifest.getModules().stream().noneMatch(m -> m.getType() == Module_.Type.DATA)) {
        throw new IllegalArgumentException(
            "types of modules in manifest must contain data: " + manifest);
      }
      this.manifest = manifest;
      return this;
    }

    public Builder manifest(String manifest) {
      return manifest(Manifest.fromJson(manifest));
    }

    public Builder manifest(Path manifest) {
      Objects.requireNonNull(manifest);
      try {
        return manifest(Files.readString(manifest));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    public Builder packIcon(Path packIcon) {
      if (packIcon == null) {
        this.packIcon = null;
        return this;
      }
      packIcon = packIcon.normalize();
      if (!FileUtil.isExtension(packIcon, PACK_ICON_EXTENSIONS)) {
        throw new IllegalArgumentException(
            "extension of pack_icon must be one of "
                + PACK_ICON_EXTENSIONS.toString()
                + ": "
                + packIcon);
      }
      this.packIcon = packIcon;
      return this;
    }

    public Builder structures(Path... structures) {
      return structures(structures == null ? null : Set.of(structures));
    }

    public Builder structures(Collection<Path> structures) {
      this.structures = null;
      return addStructures(structures);
    }

    public Builder addStructures(Path... structures) {
      return addStructures(structures == null ? null : Set.of(structures));
    }

    public Builder addStructures(Collection<Path> structures) {
      if (structures == null || structures.isEmpty()) {
        return this;
      }
      if (this.structures == null) {
        this.structures = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      }
      for (var p : structures) {
        Objects.requireNonNull(p, "path of structure must not be null");
        p = p.normalize();
        if (!FileUtil.isExtension(p, MCExtension.MCSTRUCTURE.toString())) {
          throw new IllegalArgumentException(
              "extension of a structure must be " + MCExtension.MCSTRUCTURE.toString() + ": " + p);
        }
        String name =
            p.getFileName()
                .toString()
                .replaceAll("\\." + MCExtension.MCSTRUCTURE.toString() + "$", "");
        if (this.structures.containsKey(name)) {
          throw new IllegalStateException("duplicated structures: " + name);
        }
        this.structures.put(name, p);
      }
      return this;
    }

    public StructureAddon build() {
      return new StructureAddon(this);
    }
  }

  @SuppressWarnings("unchecked")
  private StructureAddon(Builder builder) {
    this.tempDir = FileUtil.createTempDir();

    this.structures = (TreeMap<String, Path>) Objects.requireNonNull(builder.structures).clone();
    this.structures.entrySet().forEach(e -> e.setValue(FileUtil.copyToDir(e.getValue(), tempDir)));

    String packNameTemp =
        builder.packName != null
            ? builder.packName
            : builder.manifest != null
                ? builder.manifest.getHeader().getName()
                : builder.structures.firstKey();
    // if packNameTemp is invalid for directory name, use randomly generated one
    if (DIRECTORY_NAME_REGEX.matcher(packNameTemp).matches()) {
      this.packName = packNameTemp;
    } else {
      String random =
          new Random().ints(6, 0, 9).mapToObj(String::valueOf).collect(Collectors.joining());
      this.packName = "Erutcurts" + random;
    }

    if (builder.manifest != null) {
      this.manifest =
          new Manifest.Builder(builder.manifest)
              .metadata(
                  new Metadata.Builder(builder.manifest.getMetadata())
                      .addGeneratedWith(ERUTCURTS_GENERATED_WITH)
                      .build())
              .build();
    } else {
      String name =
          switch (builder.structures.size()) {
            case 0 -> throw new AssertionError();
            case 1 -> "Structure: " + structures.firstKey();
            case 2 -> "Structures: " + structures.firstKey() + " and " + structures.lastKey();
            default -> "Structures "
                + structures.firstKey()
                + " and "
                + (structures.size() - 1)
                + " others";
          };
      this.manifest =
          new Manifest.Builder()
              .header(
                  new Header.Builder()
                      .name(name)
                      .description(
                          "§lIncluded Structures§r\n"
                              + String.join(", ", builder.structures.keySet())
                              + "\n\nby Erutcurts")
                      .build())
              .modules(Module_.of(Module_.Type.DATA))
              .metadata(new Metadata.Builder().generatedWith(ERUTCURTS_GENERATED_WITH).build())
              .build();
    }

    if (builder.packIcon == null) {
      this.packIcon = DEFAULT_PACK_ICON;
    } else {
      this.packIcon = FileUtil.copyToDir(builder.packIcon, tempDir);
    }
  }

  @Override
  public void close() {
    FileUtil.delete(tempDir);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return (obj instanceof StructureAddon other)
        && Objects.equals(packName, other.packName)
        && Objects.equals(manifest, other.manifest)
        && Objects.equals(packIcon, other.packIcon)
        && Objects.equals(structures, other.structures);
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 31 + Objects.hashCode(packName);
    hash = hash * 31 + Objects.hashCode(manifest);
    hash = hash * 31 + Objects.hashCode(packIcon);
    hash = hash * 31 + Objects.hashCode(structures);
    return hash;
  }

  @Override
  public String toString() {
    var str = new StringBuilder();
    str.append("packName: ");
    str.append(packName);
    str.append("\npackIcon: ");
    str.append(packIcon);
    str.append("\nstructures: ");
    str.append(structures);
    str.append("\nmanifest:\n");
    str.append(manifest);
    return str.toString();
  }
}
