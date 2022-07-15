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
import net.dv8tion.jda.api.utils.TimeUtil;
import org.apache.commons.io.FileUtils;

import io.github.risu729.erutcurts.structure.StructureAddon;

final class Listener extends ListenerAdapter {

  private static final Duration WAIT_CACHE_DELETION = Duration.ofMinutes(1);// ofHours(1); debug

  private final PackageManager packageManager = new PackageManager();

  // key is the ID of sent message
  private final Map<Long, List<Path>> structureCaches = new HashMap<>();
  private final ScheduledExecutorService cacheDeleteScheduler = SchedulerUtil.newScheduledDaemonThreadPool(5);

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {

    Message message = event.getMessage();
    MessageChannel channel = event.getChannel();
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

    try {
      switch (command) {
        case DEBUG -> {
          UtilCommands.replyDebugInfo(message, packageManager.getPackageModeChannel(channel));
        }

        case HELP -> UtilCommands.replyHelp(message);

        case PACKAGE, PACKAGE_CONTINUE, CANCEL -> {
          switch (command) {
            case PACKAGE -> packageManager.enablePackageMode(channel, true);
            case PACKAGE_CONTINUE -> packageManager.enablePackageMode(channel, false);
            case CANCEL -> packageManager.disablePackageMode(channel);
            default -> throw new AssertionError();
          }
          UtilCommands.reactCheckMark(message);
        }

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
    } catch (RuntimeException | Error e) {
      UtilCommands.replyError(message, e);
      throw e;
    }
  }

  @Override
  public void onButtonInteraction(ButtonInteractionEvent event) {
    // avoid "This interaction failed"
    event.deferEdit().queue();
    
    Message message = event.getMessage();
    MessageChannel channel = event.getChannel();

    CustomizedButton button = CustomizedButton.fromID(event.getComponentId()).orElseThrow();
    
    try {
      switch (button) {

        case HELP -> UtilCommands.replyHelp(message);

        case SINGLE -> {
          BehaviorCommands.replySingle(message, structureCaches.get(message.getIdLong()));
        }

        // TODO: support INDEX button

        case OK, DELETE -> UtilCommands.deleteMessage(message);

        case OK_LONG_STANDBY -> {
          packageManager.disablePackageMode(channel);
          UtilCommands.deleteMessage(message);
        }

        case DISMISS_LONG_STANDBY -> {
          packageManager.enablePackageMode(channel, false);
          UtilCommands.deleteMessage(message);
        }

        case CONTINUE_RESTART -> {
          // if not yet restarted after the restart alert
          if (TimeUtil.getTimeCreated(message).isAfter(Erutcurts.LAST_RESTART)) {
            throw new UnsupportedOperationException("Continue is not yet available: " + button);
          }
          packageManager.enablePackageMode(channel, false);
          UtilCommands.disableButtons(message, CustomizedButton.CONTINUE_RESTART);
        }

        default -> throw new UnsupportedOperationException("Unsuppported Button: " + button);
      }
    } catch (RuntimeException | Error e) {
      UtilCommands.replyError(message, e);
      throw e;
    }
  }

  public void shutdown() {
    cacheDeleteScheduler.shutdownNow();
    packageManager.shutdown();
  }
}