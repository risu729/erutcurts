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
import java.util.concurrent.ExecutionException;
import java.util.List;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileProxy;
import org.apache.commons.io.FileUtils;

import io.github.risu729.erutcurts.generator.StructureAddon;

final class BehaviorCommands {

  public static void replyMulti(Message message, List<Message.Attachment> structures) {
    Path tempDir = Path.of(System.getenv().get("TEMP_DIR"));
    Path attachmentsDir;
    Path packPath;
    try {
      attachmentsDir = Files.createTempDirectory(Files.createDirectories(tempDir), "ER");
      packPath = new StructureAddon.Builder(tempDir)
          .structures(structures.stream()
              .map(a -> {
                try {
                  return new FileProxy(a.getUrl()).downloadToPath(attachmentsDir.resolve(a.getFileName())).get();
                } catch (ExecutionException | InterruptedException e) {
                  throw new UndeclaredThrowableException(e);
                }
              })
              .collect(Collectors.toList()))
          .build()
          .generateBehavior(attachmentsDir);
      if (Files.size(packPath) > message.getJDA().getSelfUser().getAllowedFileSize()) {
        throw new IllegalArgumentException("generated behavior pack size exceeds the maximum allowed size: " + Files.size(packPath));
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    message.reply(packPath.toFile())
        .mentionRepliedUser(false)
        .setActionRow(ButtonUtils.SINGLE, ButtonUtils.MULTI.asDisabled(),
            ButtonUtils.INDEX, ButtonUtils.DELETE, ButtonUtils.HELP)
        .queue();
    FileUtils.deleteQuietly(attachmentsDir.toFile());
  }

  public static void replySingle(Message message, List<Message.Attachment> structures) {
    message.reply("currently unsupported").queue();
  }

  private BehaviorCommands() {
    throw new AssertionError();
  }
}