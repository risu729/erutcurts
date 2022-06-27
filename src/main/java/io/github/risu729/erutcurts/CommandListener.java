/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.util.EnumSet;
import java.util.function.Predicate;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import io.github.risu729.erutcurts.generator.StructureAddon;

final class CommandListener extends ListenerAdapter {

  private static final Pattern COMMANDS_SEPARATOR = Pattern.compile(" ");

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    Message message = event.getMessage();
    if (message.getAuthor().isBot()) {
      return;
    }
    EnumSet<Command> commandSet = COMMANDS_SEPARATOR.splitAsStream(event.getMessage().getContentRaw())
          .filter(Predicate.not(String::isBlank))
          .filter(s -> s.charAt(0) == Command.PREFIX)
          .map(Command::fromString)
          .collect(Collectors.toCollection(() -> EnumSet.noneOf(Command.class)));
    List<Message.Attachment> structureList = message.getAttachments()
        .stream()
        .filter(e -> StructureAddon.STRUCTURE_EXTENSIONS.contains(e.getFileExtension()))
        .collect(Collectors.toList());
    if (commandSet.isEmpty()) {
      if (!structureList.isEmpty()) {
        BehaviorCommands.replyMulti(message, structureList);
      }
      return;
    }
    if (commandSet.contains(Command.MULTI)) {
      BehaviorCommands.replyMulti(message, structureList);
    }
    if (commandSet.contains(Command.SINGLE)) {
      BehaviorCommands.replySingle(message, structureList);
    }
    if (commandSet.contains(Command.HELP)) {
      UtilCommands.replyHelp(message);
    }
  }
}