/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import static io.github.risu729.erutcurts.CustomizedButton.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.apache.commons.io.FileUtils;

import io.github.risu729.erutcurts.structure.StructureAddon;

final class BehaviorCommands {

  private static final ActionRow MULTI_ACTION_ROW = ActionRow.of(SINGLE.toButton(), INDEX.toButton(), DELETE.toButton(),
      HELP.toButton());
  private static final ActionRow SINGLE_ACTION_ROW = ActionRow.of(INDEX.toButton(), DELETE.toButton(), HELP.toButton());

  public static Message replyMulti(Message message, Collection<Path> structures) {
    Path tempDir;
    Path packPath;
    try {
      tempDir = Files.createTempDirectory(Erutcurts.TEMP_DIR, "BehaviorCommands");
      packPath = StructureAddon.of(structures).generateBehavior(tempDir);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    var result = AttachmentUtil.replySingleFile(message, packPath, MULTI_ACTION_ROW);
    FileUtils.deleteQuietly(tempDir.toFile());
    return result;
  }

  public static List<Message> replySingle(Message message, Collection<Path> structures) {
    if (structures.isEmpty()) {
      throw new IllegalArgumentException("structures are empty: " + structures);
    }
    Path tempDir;
    List<Path> packPath = new ArrayList<>();
    try {
      tempDir = Files.createTempDirectory(Erutcurts.TEMP_DIR, "BehaviorCommands");
      for (var p : structures) {
        packPath.add(StructureAddon.of(p).generateBehavior(tempDir));
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    var result = AttachmentUtil.replyFiles(message, packPath, SINGLE_ACTION_ROW);
    FileUtils.deleteQuietly(tempDir.toFile());
    return result;
  }
}