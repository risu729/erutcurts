/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import static io.github.risu729.erutcurts.CustomizedButton.*;

import java.awt.Color;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;

final class PackageManager {

  public static final Duration FIRST_PACKAGE_ALERT = Duration.ofHours(3);
  public static final Duration SECOND_PACKAGE_ALERT = Duration.ofDays(1);

  private static final int HISTORY_LIMIT = 100;

  private static final EmbedBuilder ALERT_EMBED_BUILDER = new EmbedBuilder()
      .setTitle("Alert")
      .setColor(Color.ORANGE);
  private static final MessageEmbed LONG_STANDBY_EMBED = new EmbedBuilder(ALERT_EMBED_BUILDER)
      .setDescription(
          """
              Long standby since the Package command.
              To continue in package mode, press the Dismiss button. Otherwise, the package mode terminates automatically.""")
      .addField("Note",
          """
              The maximum number of messages retrieved by the Generate command is 100, including those that do not contain structure files.
              The former messages will be ignored.""",
          false)
      .build();
  private static final MessageEmbed RESTART_EMBED = new EmbedBuilder(ALERT_EMBED_BUILDER)
      .setDescription("The package mode will reset in a few seconds due to the restart of the bot.")
      .addField("Note", """
          If you wish to continue, please start over from the beginning.
          We apologize for the inconvenience.""", false)
      .build();
  private static final ActionRow LONG_STANDBY_ACTION_ROW = ActionRow.of(DISMISS.toButton(), HELP.toButton());
  private static final ActionRow RESTART_ACTION_ROW = ActionRow.of(HELP.toButton());

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

  // key is the ID of channel
  private final Map<Long, MessageChannel> packageModeChannels = new HashMap<>();

  private final ScheduledExecutorService longStandbyAlertScheduler = SchedulerUtil.newScheduledDaemonThreadPool(1);

  public void enablePackageMode(Message message, boolean isFirst) {
    MessageChannel channel = message.getChannel();
    packageModeChannels.put(channel.getIdLong(), channel);
    long delay = (isFirst ? FIRST_PACKAGE_ALERT : SECOND_PACKAGE_ALERT).toMinutes();
    longStandbyAlertScheduler.schedule(() -> sendLongStandbyAlert(channel), delay, TimeUnit.MINUTES);
    message.addReaction(Emoji.fromUnicode("U+2705")).queue();
  }

  public void disablePackageMode(MessageChannel channel) {
    packageModeChannels.remove(channel.getIdLong());
  }

  public boolean isPackageMode(MessageChannel channel) {
    return packageModeChannels.containsKey(channel.getIdLong());
  }

  public List<Message> getMessagesInPackage(Message message) {
    MessageChannel channel = message.getChannel();
    disablePackageMode(channel);
    List<Message> history;
    try {
      history = channel.getHistoryBefore(message, HISTORY_LIMIT)
          .submit()
          .get()
          .getRetrievedHistory();
    } catch (ExecutionException | InterruptedException e) {
      throw new IllegalStateException(e);
    }
    int packageIndex = history.stream()
        .map(Message::getContentRaw)
        .map(Command::fromString)
        .map(o -> o.orElse(null))
        .toList()
        .indexOf(Command.PACKAGE);
    return history.subList(0, packageIndex == -1 ? history.size() : packageIndex + 1);
  }

  public void shutdown() {
    longStandbyAlertScheduler.shutdownNow();
    packageModeChannels.values().forEach(PackageManager::sendRestartAlert);
  }
}