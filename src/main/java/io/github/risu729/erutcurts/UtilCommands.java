/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import static io.github.risu729.erutcurts.CustomizedButton.*;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

final class UtilCommands {

  private static final MessageEmbed HELP_EMBED = new EmbedBuilder()
      .setTitle("Help")
      .setColor(Color.LIGHT_GRAY)
      .setDescription("Maybe written later...?")
      .build();
  private static final EmbedBuilder ERROR_EMBED_BUILDER = new EmbedBuilder()
      .setTitle("ERROR")
      .setColor(Color.RED);
  private static final EmbedBuilder DEBUG_INFO_EMBED_BUILDER = new EmbedBuilder()
      .setTitle("DEBUG")
      .setColor(Color.BLACK);
  private static final ActionRow HELP_ACTION_ROW = ActionRow.of(DELETE.toButton(), HELP_URL.toButton());
  private static final ActionRow ERROR_ACTION_ROW = ActionRow.of(OK.toButton(), HELP.toButton());
  private static final ActionRow DEBUG_INFO_ACTION_ROW = ActionRow.of(DELETE.toButton());

  public static void replyHelp(Message message) {
    long time = System.currentTimeMillis();
    message.replyEmbeds(HELP_EMBED)
        .setActionRows(HELP_ACTION_ROW)
        .mentionRepliedUser(false)
        .queue(m -> m.editMessageEmbeds(new EmbedBuilder(HELP_EMBED)
                .setFooter(String.format("version: %s\nlast restart: %s\nping: %d ms", Erutcurts.VERSION,
                    Erutcurts.LAST_RESTART.truncatedTo(ChronoUnit.SECONDS).toString(), System.currentTimeMillis() - time))
                .build())
            .queue());
  }

  public static void replyError(Message message, Throwable throwable) {
    String stackTrace;
    try (var sw = new StringWriter();
        var pw = new PrintWriter(sw)) {
      throwable.printStackTrace(pw);
      pw.flush();
      stackTrace = sw.toString();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    message.replyEmbeds(new EmbedBuilder(ERROR_EMBED_BUILDER).setDescription(stackTrace).build())
        .setActionRows(ERROR_ACTION_ROW)
        .mentionRepliedUser(false)
        .queue();
  }

  public static void replyDebugInfo(Message message, Object o) {
    message.replyEmbeds(new EmbedBuilder(DEBUG_INFO_EMBED_BUILDER).setDescription(String.valueOf(o)).build())
        .setActionRows(DEBUG_INFO_ACTION_ROW)
        .mentionRepliedUser(false)
        .queue();
  }

  public static void deleteMessage(Message message) {
    message.delete().queue();
  }

  public static void reactCheckMark(Message message) {
    message.addReaction(Emoji.fromUnicode("U+2705")).queue();
  }

  public static void disableButtons(Message message, CustomizedButton... buttons) {
    EnumSet<CustomizedButton> targetButtons = EnumSet.noneOf(CustomizedButton.class);
    targetButtons.addAll(Arrays.asList(buttons));
    List<ActionRow> newActionRows = message.getActionRows()
        .stream() // Stream<ActionRow>
        .map(actionRow -> actionRow.getComponents()
            .stream()
            .map(component -> {
              if (component instanceof Button button) {
                var cb = CustomizedButton.fromButton(button).orElseThrow();
                return targetButtons.contains(cb) ? cb.toButtonDisabled() : cb.toButton();
              } else {
                return component;
              }
            })
            .collect(Collectors.collectingAndThen(Collectors.toList(), ActionRow::of)))
        .toList();
    message.editMessageComponents(newActionRows).queue();
  }

  private UtilCommands() {
    throw new AssertionError();
  }
}