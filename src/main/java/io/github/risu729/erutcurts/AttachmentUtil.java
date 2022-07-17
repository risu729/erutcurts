/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.awt.Color;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.LinkedList;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.FileProxy;

final class AttachmentUtil {

  private static final EmbedBuilder COUNT_EMBED_BUILDER = new EmbedBuilder()
      .setColor(Color.CYAN);

  public static List<Path> download(Message message, Path directory, String... extensions) {
    return download(message, directory, List.of(extensions));
  }

  public static List<Path> download(Message message, Path directory, Collection<String> extensions) {
    return download(List.of(message), directory, extensions);
  }

  public static List<Path> download(Collection<Message> messages, Path directory, String... extensions) {
    return download(messages, directory, List.of(extensions));
  }

  public static List<Path> download(Collection<Message> messages, Path directory, Collection<String> extensions) {
    return download(messages.stream()
        .map(Message::getAttachments)
        .flatMap(List::stream)
        .filter(a -> extensions.stream().anyMatch(e -> e.equalsIgnoreCase(a.getFileExtension())))
        .toList(), directory);
  }

  public static Path download(Message.Attachment attachment, Path directory) {
    return download(List.of(attachment), directory).get(0);
  }

  public static List<Path> download(Collection<Message.Attachment> attachments, Path directory) {
    if (!Files.isDirectory(directory)) {
      throw new IllegalArgumentException("path is not a directory: " + directory);
    }
    return attachments.stream()
        .map(a -> {
          try {
            return new FileProxy(a.getUrl()).downloadToPath(directory.resolve(a.getFileName())).get();
          } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
          }
        })
        .toList();
  }

  public static Message replySingleFile(Message message, Path file, ActionRow actionRow) {
    return replyFiles(message, List.of(file), actionRow).get(0);
  }

  public static List<Message> replyFiles(Message message, List<Path> files, ActionRow actionRow) {
    if (files.isEmpty()) {
      throw new IllegalArgumentException("files list is empty: " + files);
    }

    final long maxFileSize = message.getJDA().getSelfUser().getAllowedFileSize();
    List<Integer> separationIndex = new LinkedList<>(); // indexes of until, inclusive, of each separated files
    int fileCount = 0;
    long totalFileSize = 0L;

    for (int index = 0, fileAmount = files.size(); index < fileAmount; index++) {
      long fileSize;
      try {
        fileSize = Files.size(files.get(index));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
      if (fileSize > maxFileSize) {
        throw new IllegalArgumentException(
            String.format("File size exceeds the allowed size (%.2f MiB): %.2f MiB",
                (double) maxFileSize / 1024 / 1024, (double) fileSize / 1024 / 1024));
      }
      if (fileCount + 1 > Message.MAX_FILE_AMOUNT || totalFileSize + fileSize >= maxFileSize) {
        separationIndex.add(index);
        fileCount = 0;
        totalFileSize = 0L;
      }
      fileCount++;
      totalFileSize += fileSize;
      if (index >= fileAmount - 1) { // finally
        separationIndex.add(fileAmount);
      }
    }

    int messageAmount = separationIndex.size();
    List<Message> sentMessages = new ArrayList<>();
    for (int i = 0; i < messageAmount; i++) {
      int fromIndex = i <= 0 ? 0 : separationIndex.get(i - 1);
      int untilIndex = separationIndex.get(i);
      String count = fromIndex + 1 == untilIndex ? String.format("%d / %d", fromIndex + 1, files.size())
          : String.format("%d ~ %d / %d", fromIndex + 1, untilIndex, files.size());
      MessageAction messageAction = message.replyEmbeds(
              new EmbedBuilder(COUNT_EMBED_BUILDER).setTitle(count).build())
          .mentionRepliedUser(false)
          .setActionRows(actionRow);
      if (messageAmount == 1) {
        messageAction.setEmbeds(Collections.emptyList());
      }
      for (Path p : files.subList(fromIndex, untilIndex)) {
        messageAction.addFile(p.toFile());
      }
      sentMessages.add(messageAction.complete());
    }
    return sentMessages;
  }

  private AttachmentUtil() {
    throw new AssertionError();
  }
}