/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;

final class UtilCommands {

  private static final ActionRow HELP_ACTION_ROW = ActionRow.of(CustomizedButton.DELETE, CustomizedButton.HELP_URL);
  private static final ActionRow ERROR_ACTION_ROW = ActionRow.of(CustomizedButton.DELETE, CustomizedButton.HELP);

  public static void replyHelp(Message message) {
    var embedBuilder = new EmbedBuilder()
        .setTitle("Help")
        .setDescription("Maybe written later...?")
        .setColor(Color.LIGHT_GRAY);
    long time = System.currentTimeMillis();
    message.replyEmbeds(embedBuilder.build())
        .mentionRepliedUser(false)
        .setActionRows(HELP_ACTION_ROW)
        .queue(m -> m.editMessageEmbeds(embedBuilder.setFooter(
            "ping " + (System.currentTimeMillis() - time) + "ms").build()).queue());
  }
  
  public static void replyError(Message message, Throwable throwable) {
    message.replyEmbeds(new EmbedBuilder()
        .setTitle("ERROR")
        .setColor(Color.RED)
        .setDescription(throwable.getMessage())
        .build())
    .mentionRepliedUser(false)
    .setActionRows(ERROR_ACTION_ROW)
    .queue();
  }

  private UtilCommands() {
    throw new AssertionError();
  }
}