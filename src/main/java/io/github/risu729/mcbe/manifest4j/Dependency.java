/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.mcbe.manifest4j;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

import io.github.risu729.mcbe.manifest4j.gson.ManifestGson;

public final class Dependency implements Comparable<Dependency> {

  static final Comparator<Dependency> STRICT_COMPARATOR =
      Comparator.comparing(Dependency::getUUID, Comparator.nullsFirst(Comparator.naturalOrder()));
  private static final Comparator<Dependency> COMPARATOR = STRICT_COMPARATOR
      .thenComparing(Dependency::getVersion, Comparator.nullsFirst(Comparator.naturalOrder()));

  private final UUID uuid; // necessary
  private final SemVer version; // necessary

  public UUID getUUID() {
    return uuid;
  }

  public SemVer getVersion() {
    return version;
  }

  public static class Builder {

    private UUID uuid;
    private SemVer version;

    public Builder() {
    }

    public Builder(Dependency other) {
      uuid(other.uuid);
      version(other.version);
    }

    public Builder(Manifest manifest) {
      this(manifest.getHeader());
    }

    public Builder(Header header) {
      uuid(header.getUUID());
      version(header.getVersion());
    }

    public Builder uuid(UUID uuid) {
      this.uuid = Objects.requireNonNull(uuid, "UUID must not be null");
      return this;
    }

    public Builder version(SemVer version) {
      this.version = version;
      return this;
    }

    public Dependency build() {
      return new Dependency(this);
    }
  }

  private Dependency(Builder builder) {
    this.uuid = Objects.requireNonNull(builder.uuid, "UUID is necessary");
    this.version = Objects.requireNonNullElse(builder.version, SemVer.DEFAULT);
  }

  @Override
  public int compareTo(Dependency other) {
    return COMPARATOR.compare(this, other);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    return (obj instanceof Dependency other)
        && Objects.equals(uuid, other.uuid)
        && Objects.equals(version, other.version);
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 31 + Objects.hashCode(uuid);
    hash = hash * 31 + Objects.hashCode(version);
    return hash;
  }

  @Override
  public String toString() {
    return ManifestGson.SERIALIZE_NULLS.toJson(this);
  }
}
