/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.mcbe.manifest4j;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Pattern;

import io.github.risu729.mcbe.manifest4j.gson.ManifestGson;

public final class Subpack implements Comparable<Subpack> {

  static final Comparator<Subpack> STRICT_COMPARATOR =
      Comparator.comparing(Subpack::getFolderName, Comparator.nullsFirst(Comparator.naturalOrder()));
  private static final Comparator<Subpack> COMPARATOR = STRICT_COMPARATOR
      .thenComparing(Subpack::getName, Comparator.nullsFirst(Comparator.naturalOrder()))
      .thenComparing(Subpack::getMemoryTier, Comparator.nullsFirst(Comparator.naturalOrder()));

  private final Path folderName; // necessary
  private final String name; // necessary
  private final Integer memoryTier; // 1 memory_tier == 0.25GB

  public Path getFolderName() {
    return folderName;
  }

  public String getName() {
    return name;
  }

  public Integer getMemoryTier() {
    return memoryTier;
  }

  public static class Builder {

    private static final Pattern FOLDER_NAME_REGEX = Pattern.compile(
        "^(?!^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$)([^\\.<>:\"/\\\\\\|\\?\\*\\x00-\\x20\\x7f][^\\.<>:\"/\\\\\\|\\?\\*\\x00-\\x1f\\x7f]{0,253}[^\\.<>:\"/\\\\\\|\\?\\*\\x00-\\x20\\x7f]|[^\\.<>:\"/\\\\\\|\\?\\*\\x00-\\x20\\x7f])$",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private Path folderName;
    private String name;
    private Integer memoryTier;

    public Builder() {
    }

    public Builder(final Subpack other) {
      folderName(other.folderName);
      name(other.name);
      memoryTier(other.memoryTier);
    }

    public Builder folderName(Path folderName) {
      Objects.requireNonNull(folderName, "folder_name must not be null");
      for (int i = 0; i < 2; i++) {
        folderName = switch (i) {
          case 0 -> folderName.normalize();
          case 1 -> Path.of("subpacks").relativize(folderName);
          default -> throw new AssertionError();
        };
        if (folderName.getNameCount() == 1) {
          if (!FOLDER_NAME_REGEX.matcher(folderName.toString()).matches()) {
            throw new IllegalArgumentException("invalid folder name : " + folderName);
          }
          this.folderName = folderName;
          return this;
        }
      }
      throw new IllegalArgumentException(
          "folder_name must be a directory directly under \"subpacks\" : " + folderName);
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder memoryTier(Integer memoryTier) {
      if (memoryTier != null && memoryTier < 0) {
        throw new IllegalArgumentException(
            "memory_tier must not be negative : " + memoryTier);
      }
      this.memoryTier = memoryTier;
      return this;
    }

    public Subpack build() {
      return new Subpack(this);
    }
  }

  private Subpack(Builder builder) {
    this.folderName = Objects.requireNonNull(builder.folderName, "folder_name is necessary");
    this.name = Objects.requireNonNullElseGet(builder.name, builder.folderName::toString);
    this.memoryTier = builder.memoryTier;
  }

  @Override
  public int compareTo(Subpack other) {
    return COMPARATOR.compare(this, other);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return (obj instanceof Subpack other)
        && Objects.equals(folderName, other.folderName)
        && Objects.equals(name, other.name)
        && Objects.equals(memoryTier, other.memoryTier);
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 31 + Objects.hashCode(folderName);
    hash = hash * 31 + Objects.hashCode(name);
    hash = hash * 31 + Objects.hashCode(memoryTier);
    return hash;
  }

  @Override
  public String toString() {
    return ManifestGson.SERIALIZE_NULLS.toJson(this);
  }
}