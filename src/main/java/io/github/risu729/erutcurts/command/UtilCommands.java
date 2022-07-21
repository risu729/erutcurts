/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts.command;

import static io.github.risu729.erutcurts.CustomizedButton.*;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

import io.github.risu729.erutcurts.CustomizedButton;
import io.github.risu729.erutcurts.Erutcurts;
import io.github.risu729.erutcurts.util.AttachmentUtil;
import io.github.risu729.erutcurts.util.FileUtil;

public final class UtilCommands {

  private static final MessageEmbed HELP_EMBED = new EmbedBuilder()
      .setTitle("Help")
      .setColor(Color.LIGHT_GRAY)
      .setDescription("Maybe written later...?")
      .build();
  private static final MessageEmbed INFO_EMBED = new EmbedBuilder()
      .setTitle("Info")
      .setColor(Color.BLACK)
      .addField("Version", Erutcurts.VERSION, true)
      .addField("Last Restart", Erutcurts.LAST_RESTART.truncatedTo(ChronoUnit.SECONDS).toString(), true)
      .build();
  private static final EmbedBuilder ERROR_EMBED_BUILDER = new EmbedBuilder()
      .setTitle("ERROR")
      .setColor(Color.RED);
  private static final EmbedBuilder DEBUG_INFO_EMBED_BUILDER = new EmbedBuilder()
      .setTitle("DEBUG")
      .setColor(Color.BLACK);
  private static final ActionRow HELP_ACTION_ROW = ActionRow.of(DELETE.toButton(), HELP_URL.toButton());
  private static final ActionRow INFO_ACTION_ROW = ActionRow.of(DELETE.toButton());
  private static final ActionRow ERROR_ACTION_ROW = ActionRow.of(OK.toButton(), DETAIL_ERROR.toButton(), HELP.toButton());
  private static final ActionRow STACK_TRACE_ACTION_ROW = ActionRow.of(OK.toButton(), HELP.toButton());
  private static final String STACK_TRACE_FILE_NAME = "stacktrace";

  public static void replyHelp(Message message) {
    long time = System.currentTimeMillis();
    message.replyEmbeds(HELP_EMBED)
        .setActionRows(HELP_ACTION_ROW)
        .mentionRepliedUser(false)
        .queue();
  }

  public static void replyInfo(Message message) {
    long time = System.currentTimeMillis();
    message.replyEmbeds(INFO_EMBED)
        .setActionRows(INFO_ACTION_ROW)
        .mentionRepliedUser(false)
        .queue(m -> m.editMessageEmbeds(new EmbedBuilder(INFO_EMBED)
                .setFooter(String.format("ping: %d ms", System.currentTimeMillis() - time))
                .build())
            .queue());
  }

  public static void replyError(Message message, Throwable throwable) {
    Path tempDir = FileUtil.createTempDir();
    Path stackTrace = tempDir.resolve(Path.of(STACK_TRACE_FILE_NAME));
    try (var sw = new StringWriter();
        var pw = new PrintWriter(sw)) {
      throwable.printStackTrace(pw);
      pw.flush();
      Files.writeString(stackTrace, sw.toString());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    message.replyEmbeds(new EmbedBuilder(ERROR_EMBED_BUILDER).setDescription(throwable.toString()).build())
        .addFile(stackTrace.toFile())
        .setActionRows(ERROR_ACTION_ROW)
        .mentionRepliedUser(false)
        .queue();
    FileUtil.delete(tempDir);
  }

  public static void replyStackTrace(Message message) {
    List<Message.Attachment> attachments = message.getAttachments();
    if (attachments.size() != 1 || !attachments.get(0).getFileName().equals(STACK_TRACE_FILE_NAME)) {
      throw new IllegalArgumentException("message has several attachments or doesn't have a stacktrace attachment: " + message);
    }
    Path tempDir = FileUtil.createTempDir();
    String stackTrace;
    try {
      stackTrace = Files.readString(AttachmentUtil.download(attachments.get(0), tempDir));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    message.replyEmbeds(new EmbedBuilder(ERROR_EMBED_BUILDER).setDescription(stackTrace).build())
        .setActionRows(STACK_TRACE_ACTION_ROW)
        .mentionRepliedUser(false)
        .queue();
  }

  public static void replyDebugInfo(Message message, Object o) {
    message.replyEmbeds(new EmbedBuilder(DEBUG_INFO_EMBED_BUILDER).setDescription(String.valueOf(o)).build())
        .setActionRows(INFO_ACTION_ROW)
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