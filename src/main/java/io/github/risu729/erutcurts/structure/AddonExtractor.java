/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts.structure;

import static io.github.risu729.erutcurts.structure.AddonDirectoryConfig.*;

import io.github.risu729.erutcurts.util.FileUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.lingala.zip4j.ZipFile;

final class AddonExtractor {

  public static StructureAddon extractBehavior(Path pack) throws IOException {
    Path tempDir = FileUtil.createTempDir();
    try (var zipFile = new ZipFile(pack.toFile())) {
      zipFile.extractAll(tempDir.toString());
    }
    Path packDir;
    try (Stream<Path> stream =
        Files.find(
            tempDir, Integer.MAX_VALUE, (p, attr) -> Files.isDirectory(p) && !p.equals(tempDir))) {
      packDir = stream.findFirst().orElseThrow();
    }

    var builder = new StructureAddon.Builder();
    builder.packName(packDir.getFileName().toString());
    builder.manifest(packDir.resolve(Pack.MANIFEST_FILE));
    builder.packIcon(packDir.resolve(Pack.PACK_ICON_FILE));
    try (Stream<Path> structures = Files.walk(packDir.resolve(Behavior.STRUCTURES_DIR))) {
      builder.structures(
          structures
              .filter(p -> FileUtil.isExtension(p, MCExtension.MCSTRUCTURE.toString()))
              .toList());
    }
    StructureAddon result = builder.build();
    FileUtil.delete(tempDir);
    return result;
  }

  private AddonExtractor() {
    throw new AssertionError();
  }
}
