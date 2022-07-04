/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.lang.reflect.UndeclaredThrowableException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.ExecutionException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.TimeUtil;

import io.github.risu729.erutcurts.generator.StructureAddon;

final class CommandListener extends ListenerAdapter {

  private static final int HISTORY_LIMIT = 100;
  private static final Duration MAX_PACKAGE_MODE_DURATION = /*Duration.ofDays(1);*/ Duration.ofSeconds(3);

  private final Map<MessageChannel, OffsetDateTime> packageModeChannels = new HashMap<>();

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {

    MessageChannel channel = event.getChannel();
    Message message = event.getMessage();
    boolean isPackageMode = packageModeChannels.containsKey(channel);

    // send alert if MAX_PACKAGE_MODE_DURATION passed since package command is sent
    if (isPackageMode &&
        Duration.between(packageModeChannels.get(channel), OffsetDateTime.now()).compareTo(MAX_PACKAGE_MODE_DURATION) > 0) {
      PackageCommands.sendLongStandbyAlert(channel);
    }

    // ignore bot messages
    if (message.getAuthor().isBot()) {
      return;
    }

    Command command = Command.fromString(message.getContentRaw());
    if (command == null) {
      if (isPackageMode) {
        return;
      }
      command = Command.GENERATE;
    }
    switch(command) {
      case HELP -> UtilCommands.replyHelp(message);
      case PACKAGE -> {
        packageModeChannels.put(channel, TimeUtil.getTimeCreated(message));
        message.addReaction("U+2705").queue();
      }
      case GENERATE -> {
        List<Message> targetMessages = new LinkedList<>();
        targetMessages.add(message);
        if (message.getReferencedMessage() != null) {
          targetMessages.add(message.getReferencedMessage());
        }
        
        // add all structures since package command is sent
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
          return;
        }
        BehaviorCommands.replyMulti(message, structures);
      }
      default -> throw new UnsupportedOperationException("Unsuppported Command: " + command);
    }
  }

  public Thread getShutdownHook() {
    return new Thread(() -> packageModeChannels.keySet().forEach(PackageCommands::sendRebootAlert));
  }
}