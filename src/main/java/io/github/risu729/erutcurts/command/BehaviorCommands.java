/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts.command;

import static io.github.risu729.erutcurts.CustomizedButton.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import io.github.risu729.erutcurts.structure.StructureAddon;
import io.github.risu729.erutcurts.util.AttachmentUtil;
import io.github.risu729.erutcurts.util.FileUtil;

public final class BehaviorCommands {

  private static final ActionRow MULTI_ACTION_ROW = ActionRow.of(
      SINGLE.toButton(), INDEX.toButton(), DELETE.toButton(), HELP.toButton());
  private static final ActionRow SINGLE_ACTION_ROW = ActionRow.of(INDEX.toButton(), DELETE.toButton(), HELP.toButton());

  public static Message replyMulti(Message message, Collection<Path> structures) {
    Path tempDir = FileUtil.createTempDir();
    Path packPath;
    try (var addon = StructureAddon.of(structures)) {
      packPath = addon.generateBehavior(tempDir);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    var result = AttachmentUtil.replySingleFile(
        message, packPath, structures.size() == 1 ? SINGLE_ACTION_ROW : MULTI_ACTION_ROW);
    FileUtil.delete(tempDir);
    return result;
  }

  public static List<Message> replySingleFromBehaviors(Message message, Collection<Path> packs) {
    if (packs.isEmpty()) {
      throw new IllegalArgumentException("packs collection is empty: " + packs);
    }

    List<StructureAddon> addons = new LinkedList<>();
    try {
      return packs.stream()
        .map(p -> {
            try {
              var addon = StructureAddon.fromBehavior(p);
              addons.add(addon);
              return addon.getStructures();
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          })
          .flatMap(Collection::stream)
          .collect(Collectors.collectingAndThen(Collectors.toList(), l -> replySingle(message, l)));
    } finally {
      addons.forEach(StructureAddon::close);
    }
  }

  public static List<Message> replySingle(Message message, Collection<Path> structures) {
    if (structures.isEmpty()) {
      throw new IllegalArgumentException("structures collection is empty: " + structures);
    }
    Path tempDir = FileUtil.createTempDir();
    List<Path> packPath = new LinkedList<>();
    for (var p : structures) {
      try (var addon = StructureAddon.of(p)) {
        packPath.add(addon.generateBehavior(tempDir));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    var result = AttachmentUtil.replyFiles(message, packPath, SINGLE_ACTION_ROW);
    FileUtil.delete(tempDir);
    return result;
  }

  private BehaviorCommands() {
    throw new AssertionError();
  }
}