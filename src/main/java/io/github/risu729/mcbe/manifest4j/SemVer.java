/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.mcbe.manifest4j;

import java.util.Objects;
import java.util.regex.Pattern;

// Follows Semantic Versioning 2.0.0 (https://semver.org) but denoting a pre-release version and build metadata is not allowed.

public final class SemVer implements Comparable<SemVer> {

  private static final Pattern SEMVER_REGEX = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)$");

  static final SemVer DEFAULT = of(1, 0, 0);

  public static SemVer of(int major, int minor, int patch) {
    return new SemVer(major, minor, patch);
  }

  public static SemVer fromString(String str) {
    Objects.requireNonNull(str);
    if (!SEMVER_REGEX.matcher(str).matches()) {
      throw new IllegalArgumentException("malformed semantic verioning : " + str);
    }
    int[] arr = Pattern.compile("\\.")
        .splitAsStream(str)
        .mapToInt(Integer::valueOf)
        .toArray();
    return of(arr[0], arr[1], arr[2]);
  }

  private final int major;
  private final int minor;
  private final int patch;

  private SemVer(int major, int minor, int patch) {
    if (major < 0 || minor < 0 || patch < 0) {
      throw new IllegalArgumentException(
          "invalid semantic versioning : " + major + "." + minor + "." + patch);
    }
    this.major = major;
    this.minor = minor;
    this.patch = patch;
  }

  public int[] toArray() {
    return new int[] { major, minor, patch };
  }

  @Override
  public int compareTo(SemVer other) {
    return major != other.major ? Integer.compare(major, other.major)
        : minor != other.minor ? Integer.compare(minor, other.minor)
            : patch != other.patch ? Integer.compare(patch, other.patch)
                : 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    return (obj instanceof SemVer other)
        && major == other.major
        && minor == other.minor
        && patch == other.patch;
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 31 + Integer.hashCode(major);
    hash = hash * 31 + Integer.hashCode(minor);
    hash = hash * 31 + Integer.hashCode(patch);
    return hash;
  }

  @Override
  public String toString() {
    var str = new StringBuilder();
    str.append(major);
    str.append(".");
    str.append(minor);
    str.append(".");
    str.append(patch);
    return str.toString();
  }
}