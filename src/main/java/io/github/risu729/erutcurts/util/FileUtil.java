/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts.util;

import io.github.risu729.erutcurts.Erutcurts;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class FileUtil {

  public static Path createTempDir() {
    try {
      return Files.createTempDirectory(Erutcurts.TEMP_DIR, null);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static Path copyToDir(Path source, Path targetDir) {
    if (!Files.isDirectory(targetDir)) {
      throw new IllegalArgumentException("targetDir is not a directory: " + targetDir);
    }
    try {
      return Files.copy(source, targetDir.resolve(source.getFileName()));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static void delete(Path path) {
    try (Stream<Path> stream = Files.walk(path)) {
      stream
          .sorted(Comparator.reverseOrder())
          .forEach(
              p -> {
                try {
                  Files.deleteIfExists(p);
                } catch (IOException e) {
                  throw new UncheckedIOException(e);
                }
              });
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static boolean isExtension(Path path, String... extensions) {
    return isExtension(path, List.of(extensions));
  }

  public static boolean isExtension(Path path, Collection<String> extensions) {
    var fileSystem = FileSystems.getDefault();
    return extensions.stream()
        .map(s -> "glob:**." + s)
        .map(fileSystem::getPathMatcher)
        .anyMatch(m -> m.matches(path));
  }

  private FileUtil() {
    throw new AssertionError();
  }
}
