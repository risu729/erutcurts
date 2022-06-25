/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.mcbe.manifest4j;

import java.util.Objects;
import java.util.UUID;

import io.github.risu729.mcbe.manifest4j.gson.ManifestGson;

public final class Header {

  static final Boolean DEFAULT_LOCK_TEMPLATE_OPTIONS = false;
  static final SemVer MIN_MCBE_VERSION = SemVer.of(1, 13, 0);

  private final String name; // necessary
  private final String description;
  private final UUID uuid; // necessary
  private final SemVer version; // necessary
  // necessary and only for, when  Module.type != WORLD_TEMPLATE or SKIN_PACK
  // must be later than or equal to 1.13.0 when format_version == 2
  private final SemVer minEngineVersion;
  private final Boolean platformLocked;
  private final PackScope packScope;
  // necessary, only for, and must be later than or equal to 1.13.0
  // when Module.type == world_template
  private final SemVer baseGameVersion;
  // necessary and only for when Module.type == world_template
  private final Boolean lockTemplateOptions;

  public static Header of(String name) {
    return new Builder().name(name).build();
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public UUID getUUID() {
    return uuid;
  }

  public SemVer getVersion() {
    return version;
  }

  public SemVer getMinEngineVersion() {
    return minEngineVersion;
  }

  public Boolean getPlatformLocked() {
    return platformLocked;
  }

  public PackScope getPackScope() {
    return packScope;
  }

  public SemVer getBaseGameVersion() {
    return baseGameVersion;
  }

  public Boolean getLockTemplateOptions() {
    return lockTemplateOptions;
  }

  public enum PackScope {
    GLOBAL,
    WORLD
  }

  public static class Builder {

    private String name;
    private String description;
    private UUID uuid;
    private SemVer version;
    private SemVer minEngineVersion;
    private Boolean platformLocked;
    private PackScope packScope;
    private SemVer baseGameVersion;
    private Boolean lockTemplateOptions;

    public Builder() {
    }

    public Builder(Header other) {
      name(other.name);
      description(other.description);
      uuid(other.uuid);
      version(other.version);
      minEngineVersion(other.minEngineVersion);
      platformLocked(other.platformLocked);
      packScope(other.packScope);
      baseGameVersion(other.baseGameVersion);
      lockTemplateOptions(other.lockTemplateOptions);
    }

    public Builder name(String name) {
      this.name = Objects.requireNonNull(name, "name must not be null");
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder uuid(UUID uuid) {
      this.uuid = uuid;
      return this;
    }

    public Builder version(SemVer version) {
      this.version = version;
      return this;
    }

    public Builder minEngineVersion(SemVer minEngineVersion) {
      this.minEngineVersion = minEngineVersion;
      return this;
    }

    public Builder platformLocked(Boolean platformLocked) {
      this.platformLocked = platformLocked;
      return this;
    }

    public Builder packScope(PackScope packScope) {
      this.packScope = packScope;
      return this;
    }

    public Builder baseGameVersion(SemVer baseGameVersion) {
      if (baseGameVersion != null
          && baseGameVersion.compareTo(MIN_MCBE_VERSION) < 0) {
        throw new IllegalStateException(
            "base_game_version must be later than or equal to "
                + MIN_MCBE_VERSION + " : " + baseGameVersion);
      }
      this.baseGameVersion = baseGameVersion;
      return this;
    }

    public Builder lockTemplateOptions(Boolean lockTemplateOptions) {
      this.lockTemplateOptions = lockTemplateOptions;
      return this;
    }

    public Header build() {
      return new Header(this);
    }
  }

  private Header(Builder builder) {
    this.name = Objects.requireNonNull(builder.name, "name is necessary");
    this.description = builder.description;
    this.uuid = Objects.requireNonNullElse(builder.uuid, UUID.randomUUID());
    this.version = Objects.requireNonNullElse(builder.version, SemVer.DEFAULT);
    this.minEngineVersion = builder.minEngineVersion;
    this.platformLocked = builder.platformLocked;
    this.packScope = builder.packScope;
    this.baseGameVersion = builder.baseGameVersion;
    this.lockTemplateOptions = builder.lockTemplateOptions;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    return (obj instanceof Header other)
        && Objects.equals(name, other.name)
        && Objects.equals(description, other.description)
        && Objects.equals(uuid, other.uuid)
        && Objects.equals(version, other.version)
        && Objects.equals(minEngineVersion, other.minEngineVersion)
        && Objects.equals(platformLocked, other.platformLocked)
        && packScope == other.packScope
        && Objects.equals(baseGameVersion, other.baseGameVersion)
        && Objects.equals(lockTemplateOptions, other.lockTemplateOptions);
      
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 31 + Objects.hashCode(name);
    hash = hash * 31 + Objects.hashCode(description);
    hash = hash * 31 + Objects.hashCode(uuid);
    hash = hash * 31 + Objects.hashCode(version);
    hash = hash * 31 + Objects.hashCode(minEngineVersion);
    hash = hash * 31 + Objects.hashCode(platformLocked);
    hash = hash * 31 + Objects.hashCode(packScope);
    hash = hash * 31 + Objects.hashCode(baseGameVersion);
    hash = hash * 31 + Objects.hashCode(lockTemplateOptions);
    return hash;
  }

  @Override
  public String toString() {
    return ManifestGson.SERIALIZE_NULLS.toJson(this);
  }
}