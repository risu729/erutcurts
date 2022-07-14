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
import java.time.Duration;
import java.util.Comparator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.io.FileUtils;

import io.github.risu729.erutcurts.generator.StructureAddon;

final class Listener extends ListenerAdapter {

  private static final Duration WAIT_CACHE_DELETION = Duration.ofMinutes(1);// ofHours(1); debug

  private final PackageManager packageManager = new PackageManager();

  // key is the ID of sent message
  private final Map<Long, List<Path>> structureCaches = new HashMap<>();
  private final ScheduledExecutorService cacheDeleteScheduler = SchedulerUtil.newScheduledDaemonThreadPool(5);

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {

    Message message = event.getMessage();
    boolean isPackageMode = packageManager.isPackageMode(event.getChannel());

    // ignore bot messages
    if (message.getAuthor().isBot()) {
      return;
    }

    Optional<Command> commandOptional = Command.fromString(message.getContentRaw());
    if (commandOptional.isEmpty() && isPackageMode) {
      return;
    }
    Command command = commandOptional.orElse(Command.AUTO_GENERATE);

    switch (command) {
      case DEBUG -> UtilCommands.replyError(message, new AssertionError("test"));

      case HELP -> UtilCommands.replyHelp(message);

      case PACKAGE -> packageManager.enablePackageMode(message, true);

      case PACKAGE_CONTINUE -> packageManager.enablePackageMode(message, false);

      case GENERATE, AUTO_GENERATE -> {
        TreeSet<Message> targetMessages = new TreeSet<>(Comparator.comparing(Message::getTimeCreated));
        targetMessages.add(message);

        // add a referenced message
        Message referencedMessage = message.getReferencedMessage();
        if (referencedMessage != null) {
          targetMessages.add(referencedMessage);
        }

        if (isPackageMode) {
          targetMessages.addAll(packageManager.getMessagesInPackage(message));
          packageManager.disablePackageMode(message.getChannel());
        }

        List<Message.Attachment> attachments = targetMessages.stream()
            .filter(m -> !m.getAuthor().isBot())
            .map(Message::getAttachments)
            .flatMap(List::stream)
            .filter(e -> StructureAddon.STRUCTURE_EXTENSIONS.contains(e.getFileExtension()))
            .toList();
        if (attachments.isEmpty()) {
          if (command == Command.GENERATE) {
            throw new IllegalArgumentException("No structure files are found");
          }
          return;
        }

        Message reference;
        if (!isPackageMode && referencedMessage == null) {
          reference = message;
        } else {
          // reply to the first message contains structures in target messages
          reference = targetMessages.stream()
              .filter(m -> !m.getAuthor().isBot())
              .filter(m -> m.getAttachments().stream()
                  .map(Message.Attachment::getFileExtension)
                  .anyMatch(StructureAddon.STRUCTURE_EXTENSIONS::contains))
              .findFirst()
              .orElseThrow(AssertionError::new);
        }

        Path cacheDir;
        try {
          cacheDir = Files.createTempDirectory(Files.createDirectories(Erutcurts.TEMP_DIR), "Listener");
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
        List<Path> structures = AttachmentUtil.download(attachments, cacheDir);
        Message sentMessage = BehaviorCommands.replyMulti(reference, structures);
        structureCaches.put(sentMessage.getIdLong(), structures);
        cacheDeleteScheduler.schedule(() -> {
          structureCaches.remove(sentMessage.getIdLong());
          FileUtils.deleteQuietly(cacheDir.toFile());
        }, WAIT_CACHE_DELETION.toMinutes(), TimeUnit.MINUTES);
      }
      default -> throw new UnsupportedOperationException("Unsuppported Command: " + command);
    }
  }

  @Override
  public void onButtonInteraction(ButtonInteractionEvent event) {
    Message message = event.getMessage();
    MessageChannel channel = event.getChannel();

    // ignore if the button is not created by this bot
    if (message.getAuthor().getIdLong() != event.getJDA().getSelfUser().getIdLong()) {
      return;
    }

    CustomizedButton button = CustomizedButton.fromID(event.getComponentId()).orElseThrow();
    switch (button) {

      case HELP -> UtilCommands.replyHelp(message);

      case SINGLE -> {
        BehaviorCommands.replySingle(message, structureCaches.get(message.getIdLong()));
      }

      // TODO: support INDEX button

      case OK, DELETE -> UtilCommands.deleteMessage(message);

      case OK_LONG_STANDBY -> {
        packageManager.cancelTermination(channel);
        packageManager.disablePackageMode(channel);
        UtilCommands.deleteMessage(message);
      }

      case DISMISS_LONG_STANDBY -> {
        packageManager.cancelTermination(channel);
        packageManager.enablePackageMode(message, false);
        UtilCommands.deleteMessage(message);
      }

      default -> throw new UnsupportedOperationException("Unsuppported Button: " + button);
    }

    // avoid "This interaction failed"
    event.deferEdit().queue();
  }

  public void shutdown() {
    cacheDeleteScheduler.shutdownNow();
    packageManager.shutdown();
  }
}