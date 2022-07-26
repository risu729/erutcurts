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
import net.lingala.zip4j.ZipFile;

final class AddonGenerator {

  public static Path generateBehavior(StructureAddon structureAddon, Path target)
      throws IOException {
    Path tempDir = FileUtil.createTempDir();
    Path packDir = generateBehaviorDirectory(structureAddon, tempDir);
    Path result = zip(packDir, target, MCExtension.MCPACK);
    FileUtil.delete(tempDir);
    return result;
  }

  private static Path generateBehaviorDirectory(StructureAddon structureAddon, Path tempDir)
      throws IOException {
    final Path behaviorDir = Files.createDirectory(tempDir.resolve(structureAddon.getPackName()));
    final Path structuresDir = Files.createDirectory(behaviorDir.resolve(Behavior.STRUCTURES_DIR));

    Files.writeString(
        behaviorDir.resolve(Pack.MANIFEST_FILE), structureAddon.getManifest().toJson());
    Files.copy(structureAddon.getPackIcon(), behaviorDir.resolve(Pack.PACK_ICON_FILE));
    for (Path p : structureAddon.getStructures()) {
      FileUtil.copyToDir(p, structuresDir);
    }

    return behaviorDir;
  }

  /*
  public static Path generateWorld(StructureAddon structureAddon, Path target) throws IOException {
  }

  private static Path generateWorldDirectory(StructureAddon structureAddon, Path tempDir) throws IOException {
    // TODO: generate level.dat, world_icon, behavior, function
  }
  */

  private static Path zip(Path dirToZip, Path target, MCExtension extension) throws IOException {
    Files.createDirectories(target);

    Path zipFilePath;
    for (int i = 0; ; i++) {
      zipFilePath =
          target.resolve(
              dirToZip.getFileName().toString()
                  + (i == 0 ? "" : "_" + i)
                  + extension.toStringWithPeriod());
      if (Files.notExists(zipFilePath)) {
        break;
      }
    }

    try (var zipFile = new ZipFile(zipFilePath.toFile())) {
      zipFile.addFolder(dirToZip.toFile());
    }

    return zipFilePath;
  }

  private AddonGenerator() {
    throw new AssertionError();
  }
}
