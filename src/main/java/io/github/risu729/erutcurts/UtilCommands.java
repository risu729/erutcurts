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

  private static final EmbedBuilder HELP_EMBED_BUILDER = new EmbedBuilder()
      .setTitle("Help")
      .setColor(Color.LIGHT_GRAY)
      .setDescription("Maybe written later...?");
  private static final EmbedBuilder ERROR_EMBED_BUILDER = new EmbedBuilder()
      .setTitle("ERROR")
      .setColor(Color.RED);
  private static final ActionRow HELP_ACTION_ROW = ActionRow.of(CustomizedButton.DELETE, CustomizedButton.HELP_URL);
  private static final ActionRow ERROR_ACTION_ROW = ActionRow.of(CustomizedButton.DELETE, CustomizedButton.HELP);

  public static void replyHelp(Message message) {
    long time = System.currentTimeMillis();
    message.replyEmbeds(HELP_EMBED_BUILDER.build())
        .setActionRows(HELP_ACTION_ROW)
        .mentionRepliedUser(false)
        .queue(m -> m.editMessageEmbeds(HELP_EMBED_BUILDER.setFooter(
            "ping " + (System.currentTimeMillis() - time) + "ms").build()).queue());
  }
  
  public static void replyError(Message message, Throwable throwable) {
    message.replyEmbeds(ERROR_EMBED_BUILDER.setDescription(throwable.getMessage()).build())
      .setActionRows(ERROR_ACTION_ROW)
      .mentionRepliedUser(false)
      .queue();
  }

  private UtilCommands() {
    throw new AssertionError();
  }
}