/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.awt.Color;
import java.time.Duration;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;

final class PackageCommands {

  static final Duration FIRST_PACKAGE_MODE_ALERT = Duration.ofHours(3);
  static final Duration SECOND_PACKAGE_MODE_ALERT = Duration.ofDays(1);

  private static final EmbedBuilder ALERT_EMBED_BUILDER = new EmbedBuilder()
      .setTitle("Alert")
      .setColor(Color.ORANGE);
  private static final MessageEmbed LONG_STANDBY_EMBED = new EmbedBuilder(ALERT_EMBED_BUILDER)
      .setDescription("""
          Long standby since the Package command.
          To continue in package mode, press the Dismiss button.""")
      .addField("Note", """
          The maximum number of messages retrieved by the Generate command is 100, including those that do not contain structure files.
          The former messages will be ignored.""", false)
      .build();
  private static final MessageEmbed RESTART_EMBED = new EmbedBuilder(ALERT_EMBED_BUILDER)
      .setDescription("""
          This bot will restart in a few seconds.
          The package mode will reset.""")
      .addField("Note", """
          If you wish to continue, please start over from the beginning.
          We apologize for the inconvenience.""", false)
      .build();
  private static final ActionRow LONG_STANDBY_ACTION_ROW = ActionRow.of(CustomizedButton.DISMISS, CustomizedButton.HELP);
  private static final ActionRow RESTART_ACTION_ROW = ActionRow.of(CustomizedButton.HELP);

  public static void sendLongStandbyAlert(MessageChannel channel) {
    channel.sendMessageEmbeds(LONG_STANDBY_EMBED)
        .setActionRows(LONG_STANDBY_ACTION_ROW)
        .mentionRepliedUser(false)
        .queue();
  }

  public static void sendRestartAlert(MessageChannel channel) {
    channel.sendMessageEmbeds(RESTART_EMBED)
        .setActionRows(RESTART_ACTION_ROW)
        .mentionRepliedUser(false)
        .queue();
  }

  private PackageCommands() {
    throw new AssertionError();
  }
}