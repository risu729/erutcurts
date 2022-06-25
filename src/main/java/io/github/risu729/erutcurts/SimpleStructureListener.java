/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.List;
import java.lang.reflect.UndeclaredThrowableException;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileProxy;
import org.apache.commons.io.FileUtils;

import io.github.risu729.erutcurts.generator.StructureAddon;

public final class SimpleStructureListener extends ListenerAdapter {

  private final Path tempDir;

  SimpleStructureListener(Path tempDir) {
    this.tempDir = tempDir;
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return;
    }
    List<Message.Attachment> attachments = event.getMessage().getAttachments();
    if (attachments.isEmpty()) {
      return;
    }
    try {
      Path attachmentsDir = Files.createTempDirectory(Files.createDirectories(tempDir), "ER");
      for (var e : attachments) {
        if (StructureAddon.STRUCTURE_EXTENSIONS.contains(e.getFileExtension())) {
          Path pack = new StructureAddon.Builder(tempDir)
              .structures(new FileProxy(e.getUrl()).downloadToPath(tempDir.resolve(e.getFileName())).get())
              .build()
              .generateBehavior(attachmentsDir);
          event.getMessage().reply(pack.toFile()).queue();
        }
      }
      FileUtils.deleteQuietly(attachmentsDir.toFile());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (ExecutionException | InterruptedException e) {
      throw new UndeclaredThrowableException(e);
    }
  }
}
