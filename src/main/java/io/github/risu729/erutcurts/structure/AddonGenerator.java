/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts.structure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;

final class AddonGenerator {

  // behavior packs
  private static MCExtension BEHAVIOR_EXTENSION = MCExtension.MCPACK;
  private static final Path MANIFEST_PATH = Path.of("manifest.json");
  private static final Path PACK_ICON_PATH = Path.of("pack_icon.png");
  private static final Path STRUCTURES_DIR = Path.of("structures");

  // world

  public static Path generateBehavior(StructureAddon structureAddon, Path target) throws IOException {
    Path tempDir = Files.createTempDirectory(Files.createDirectories(structureAddon.getTempDir()), "AddonGenerator");
    Path packDir = generateBehaviorDirectory(structureAddon, tempDir);
    Path result =  zip(packDir, target, BEHAVIOR_EXTENSION);
    FileUtils.deleteQuietly(tempDir.toFile());
    return result;
  }

  /*
  static Path generateWorld(StructureAddon structureAddon, Path target) throws IOException {
  }
  */

  private static Path generateBehaviorDirectory(StructureAddon structureAddon, Path tempDir) throws IOException {
    final Path behaviorDir = Files.createDirectory(tempDir.resolve(structureAddon.getPackName()));
    final Path structuresDir = Files.createDirectory(behaviorDir.resolve(STRUCTURES_DIR));
    
    Files.writeString(behaviorDir.resolve(MANIFEST_PATH), structureAddon.getManifest().toJson());
    Files.copy(structureAddon.getPackIcon(), behaviorDir.resolve(PACK_ICON_PATH));
    for (Path p : structureAddon.getStructures()) {
      Files.copy(p, structuresDir.resolve(p.getFileName()));
    }

    return behaviorDir;
  }

  /*
  private static Path generateWorldDirectory(StructureAddon structureAddon, Path tempDir) throws IOException {
    // TODO: generate level.dat, world_icon, behavior, function
  }
  */

  private static Path zip(Path dirToZip, Path target, MCExtension extension) throws IOException {
    Files.createDirectories(target);
    
    Path zipFilePath;
    for (int i = 0; ; i++) {
      zipFilePath = target.resolve(dirToZip.getFileName().toString()
          + (i == 0 ? "" : "_" + i) + extension.toStringWithPeriod());
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