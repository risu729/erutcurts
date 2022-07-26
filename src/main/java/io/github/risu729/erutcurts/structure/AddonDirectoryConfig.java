/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts.structure;

import java.nio.file.Path;

final class AddonDirectoryConfig {

  public static final class Pack {

    public static final Path MANIFEST_FILE = Path.of("manifest.json");
    public static final Path PACK_ICON_FILE = Path.of("pack_icon.png");

    private Pack() {
      throw new AssertionError();
    }
  }

  public static final class Behavior {

    public static final Path STRUCTURES_DIR = Path.of("structures");

    private Behavior() {
      throw new AssertionError();
    }
  }

  public static final class Resource {

    private Resource() {
      throw new AssertionError();
    }
  }

  public static final class World {

    private World() {
      throw new AssertionError();
    }
  }

  private AddonDirectoryConfig() {
    throw new AssertionError();
  }
}
