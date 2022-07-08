/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.Iterator;
import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.FileProxy;
import org.apache.commons.io.FileUtils;

import io.github.risu729.erutcurts.generator.StructureAddon;

final class BehaviorCommands {

  private static final ActionRow MULTI_ACTION_ROW = ActionRow.of(
      CustomizedButton.SINGLE, CustomizedButton.INDEX, CustomizedButton.DELETE, CustomizedButton.HELP);
  private static final ActionRow SINGLE_ACTION_ROW = ActionRow.of(
      CustomizedButton.INDEX, CustomizedButton.DELETE, CustomizedButton.HELP);

  private static final Path TEMP_DIR = Path.of(System.getProperty("java.io.tmpdir"));

  public static void replyMulti(Message message, Collection<Message.Attachment> structures) {
    Path uniqueDir;
    Path packPath;
    try {
      uniqueDir = Files.createTempDirectory(TEMP_DIR, "ER");
      packPath = new StructureAddon.Builder()
          .structures(download(structures, uniqueDir))
          .build()
          .generateBehavior(uniqueDir);
      if (Files.size(packPath) > message.getJDA().getSelfUser().getAllowedFileSize()) {
        throw new IllegalArgumentException(
            String.format("Generated behavior pack size exceeds the allowed size: %.2f MiB",
                (double) Files.size(packPath) / 1024 / 1024));
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    replyPack(message, packPath, MULTI_ACTION_ROW);
    FileUtils.deleteQuietly(uniqueDir.toFile());
  }

  public static void replySingle(Message message, Collection<Message.Attachment> structures) {
    Path uniqueDir;
    try {
      uniqueDir = Files.createTempDirectory(TEMP_DIR, "ER");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    List<Path> packs = new ArrayList<>();
    long totalSize = 0L;
    final long maxFileSize = message.getJDA().getSelfUser().getAllowedFileSize();
    for (Iterator<Path> iterator = download(structures, uniqueDir).iterator(); iterator.hasNext();) {
      Path packPath;
      long fileSize;
      try {
        packPath = new StructureAddon.Builder()
            .structures(iterator.next())
            .build()
            .generateBehavior(uniqueDir);
        fileSize = Files.size(packPath);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
      if (fileSize > maxFileSize) {
        throw new IllegalArgumentException(
            String.format("Generated behavior pack size exceeds the allowed size: %.2f MiB",
                (double) fileSize / 1024 / 1024));
      }
      // split messages if the file amount or the total size exceeds the limit
      if (packs.size() + 1 > Message.MAX_FILE_AMOUNT || totalSize + fileSize > maxFileSize) {
        replyPack(message, packPath, SINGLE_ACTION_ROW);
        packs.clear();
        totalSize = 0L;
      }
      packs.add(packPath);
      totalSize += fileSize;
      if (!iterator.hasNext()) {
        replyPack(message, packPath, SINGLE_ACTION_ROW);
      }
      FileUtils.deleteQuietly(uniqueDir.toFile());
    }
  }

  private static List<Path> download(Collection<Message.Attachment> attachments, Path downloadDirectory) {
    return attachments.stream()
        .map(a -> {
          try {
            return new FileProxy(a.getUrl()).downloadToPath(downloadDirectory.resolve(a.getFileName())).get();
          } catch (ExecutionException | InterruptedException e) {
            throw new UndeclaredThrowableException(e);
          }
        })
        .toList();
  }

  private static void replyPack(Message message, Path pack, ActionRow actionRow) {
    replyPack(message, List.of(pack), actionRow);
  }

  private static void replyPack(Message message, List<Path> packs, ActionRow actionRow) {
    if (packs.isEmpty()) {
      throw new IllegalArgumentException();
    }
    MessageAction messageAction = message.reply(packs.remove(0).toFile())
        .mentionRepliedUser(false)
        .setActionRows(actionRow);
    for (var p : packs) {
      messageAction.addFile(p.toFile());
    }
    messageAction.queue();
  }

  private BehaviorCommands() {
    throw new AssertionError();
  }
}