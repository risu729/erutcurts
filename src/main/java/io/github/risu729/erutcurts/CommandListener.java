/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.lang.reflect.UndeclaredThrowableException;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.TimeUtil;

import io.github.risu729.erutcurts.generator.StructureAddon;

final class CommandListener extends ListenerAdapter {

  private static final int HISTORY_LIMIT = 100;

  private final Map<MessageChannel, OffsetDateTime> packageModeChannels = new HashMap<>();

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {

    MessageChannel channel = event.getChannel();
    Message message = event.getMessage();
    boolean isPackageMode = packageModeChannels.containsKey(channel);

    // send alert if MAX_PACKAGE_MODE_DURATION passed since package command is sent
    if (isPackageMode && packageModeChannels.get(channel).isBefore(OffsetDateTime.now())) {
      packageModeChannels.remove(channel);
      PackageCommands.sendLongStandbyAlert(channel);
    }

    // ignore bot messages
    if (message.getAuthor().isBot()) {
      return;
    }

    Optional<Command> commandOptional = Command.fromString(message.getContentRaw());
    if (commandOptional.isEmpty() && isPackageMode) {
      return;
    }
    Command command = commandOptional.orElse(Command.AUTO_GENERATE);

    switch(command) {
      case DEBUG -> {
        PackageCommands.sendLongStandbyAlert(channel);
        PackageCommands.sendRestartAlert(channel);
      }
      case HELP -> UtilCommands.replyHelp(message);
      case PACKAGE -> {
        packageModeChannels.put(channel,
            TimeUtil.getTimeCreated(message).plus(PackageCommands.FIRST_PACKAGE_MODE_ALERT));
        message.addReaction("U+2705").queue();
      }
      case GENERATE, AUTO_GENERATE -> {
        TreeSet<Message> targetMessages = new TreeSet<>(Comparator.comparing(Message::getTimeCreated));
        targetMessages.add(message);
        
        // add a referenced message
        Message referencedMessage = message.getReferencedMessage();
        if (referencedMessage != null) {
          targetMessages.add(referencedMessage);
        }
        
        // add all messages since package command is sent
        if (isPackageMode) {
          packageModeChannels.remove(channel);
          List<Message> history;
          try {
            history = channel.getHistoryBefore(message, HISTORY_LIMIT)
                .submit()
                .get()
                .getRetrievedHistory();
          } catch (ExecutionException | InterruptedException e) {
            throw new UndeclaredThrowableException(e);
          }
          int packageIndex = history.stream()
              .map(Message::getContentRaw)
              .map(Command::fromString)
              .filter(Optional::isPresent)
              .map(Optional::orElseThrow)
              .toList()
              .indexOf(Command.PACKAGE);
          targetMessages.addAll(history.subList(0, packageIndex == -1 ? history.size() : packageIndex + 1));
        }
        
        List<Message.Attachment> structures = targetMessages.stream()
            .filter(m -> !m.getAuthor().isBot())
            .map(Message::getAttachments)
            .flatMap(List::stream)
            .filter(e -> StructureAddon.STRUCTURE_EXTENSIONS.contains(e.getFileExtension()))
            .toList();
        if (structures.isEmpty()) {
          if (command == Command.GENERATE) {
            throw new IllegalArgumentException("No structure files are found.");
          }
          return;
        }

        Message reference;
        if (!isPackageMode && referencedMessage == null) {
          reference = message;
        } else {
          reference = targetMessages.stream()
              .filter(m -> !m.getAuthor().isBot())
              .filter(m -> m.getAttachments().stream()
                  .map(Message.Attachment::getFileExtension)
                  .anyMatch(StructureAddon.STRUCTURE_EXTENSIONS::contains))
              .findFirst()
              .orElseThrow(AssertionError::new);
        }
        
        BehaviorCommands.replyMulti(reference, structures);
      }
      default -> throw new UnsupportedOperationException("Unsuppported Command: " + command);
    }
  }

  public Thread getShutdownHook() {
    return new Thread(() -> packageModeChannels.keySet().forEach(PackageCommands::sendRebootAlert));
  }
}