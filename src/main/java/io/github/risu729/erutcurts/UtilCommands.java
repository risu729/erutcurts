/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import static io.github.risu729.erutcurts.CustomizedButton.*;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;

final class UtilCommands {

  private static final MessageEmbed HELP_EMBED = new EmbedBuilder()
      .setTitle("Help")
      .setColor(Color.LIGHT_GRAY)
      .setDescription("Maybe written later...?")
      .build();
  private static final EmbedBuilder ERROR_EMBED_BUILDER = new EmbedBuilder()
      .setTitle("ERROR")
      .setColor(Color.RED);
  private static final ActionRow HELP_ACTION_ROW = ActionRow.of(DELETE.toButton(), HELP_URL.toButton());
  private static final ActionRow ERROR_ACTION_ROW = ActionRow.of(DELETE.toButton(), HELP.toButton());

  public static void replyHelp(Message message) {
    long time = System.currentTimeMillis();
    message.replyEmbeds(HELP_EMBED)
        .setActionRows(HELP_ACTION_ROW)
        .mentionRepliedUser(false)
        .queue(m -> m.editMessageEmbeds(new EmbedBuilder(HELP_EMBED)
                .setFooter(String.format(
                    "version: %s\nping: &d ms", Erutcurts.VERSION, (System.currentTimeMillis() - time) + "ms"))
                .build())
            .queue());
  }

  public static void replyHelp(IReplyCallback callback) {
    long time = System.currentTimeMillis();
    callback.replyEmbeds(HELP_EMBED)
        .addActionRows(HELP_ACTION_ROW)
        .mentionRepliedUser(false)
        .setEphemeral(true)
        .queue(m -> m.editOriginalEmbeds(new EmbedBuilder(HELP_EMBED)
                .setFooter(String.format(
                    "version: %s\nping: &d ms", Erutcurts.VERSION, (System.currentTimeMillis() - time) + "ms"))
                .build())
            .queue());
  }

  public static void replyError(Message message, Throwable throwable) {
    message.replyEmbeds(new EmbedBuilder(ERROR_EMBED_BUILDER)
            .setDescription(throwable.getMessage())
            .build())
        .setActionRows(ERROR_ACTION_ROW)
        .mentionRepliedUser(false)
        .queue();
  }

  private UtilCommands() {
    throw new AssertionError();
  }
}