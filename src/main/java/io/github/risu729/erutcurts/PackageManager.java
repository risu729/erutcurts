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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import io.github.risu729.erutcurts.util.SchedulerUtil;

final class PackageManager {

  private static final Duration FIRST_PACKAGE_ALERT = Duration.ofHours(3);
  private static final Duration SECOND_PACKAGE_ALERT = Duration.ofDays(1);
  private static final Duration WAIT_PACKAGE_TERMINATION = Duration.ofMinutes(1);

  private static final int HISTORY_LIMIT = 100;

  private static final EmbedBuilder ALERT_EMBED_BUILDER = new EmbedBuilder()
      .setTitle("Alert")
      .setColor(Color.ORANGE);
  private static final MessageEmbed LONG_STANDBY_EMBED = new EmbedBuilder(ALERT_EMBED_BUILDER)
      .setDescription("""
          Long standby since the Package command.
          If you would like to continue in package mode, press the Dismiss button.
          Otherwise, the package mode terminates automatically in 1 minute.""")
      .addField("Note","""
          The maximum number of messages retrieved by the Generate command is 100, including those that do not contain structure files.
          The former messages will be ignored.""", false)
      .build();
  private static final MessageEmbed RESTART_EMBED = new EmbedBuilder(ALERT_EMBED_BUILDER)
      .setDescription("""
          The package mode will reset in a few seconds due to the restart of the bot.
          If you wish to continue, please press the "Continue" button after a few minutes.""")
      .build();
  private static final ActionRow LONG_STANDBY_ACTION_ROW = ActionRow.of(
      OK_LONG_STANDBY.toButton(), DISMISS_LONG_STANDBY.toButton(), HELP.toButton());
  private static final ActionRow RESTART_ACTION_ROW = ActionRow.of(CONTINUE_RESTART.toButton(), DELETE.toButton(), HELP.toButton());

  // key is the ID of channel
  private final Map<Long, PackageModeChannel> packageModeChannels = new ConcurrentHashMap<>();

  private final ScheduledExecutorService longStandbyAlertScheduler = SchedulerUtil.newScheduledDaemonThreadPool(5);

  private final ScheduledExecutorService packageTerminationScheduler = SchedulerUtil.newScheduledDaemonThreadPool(1);
  
  public void enablePackageMode(MessageChannel channel, boolean isFirst) {
    disablePackageMode(channel);
    long channelID = channel.getIdLong();
    packageModeChannels.put(channelID, new PackageModeChannel(channel));
    scheduleLongStandbyAlert(channelID, (isFirst ? FIRST_PACKAGE_ALERT : SECOND_PACKAGE_ALERT).toMinutes());
  }

  public void disablePackageMode(MessageChannel channel) {
    if (packageModeChannels.containsKey(channel.getIdLong())) {
      packageModeChannels.remove(channel.getIdLong()).cancelAll();
    }
  }

  public boolean isPackageMode(MessageChannel channel) {
    return packageModeChannels.containsKey(channel.getIdLong());
  }

  public PackageModeChannel getPackageModeChannel(MessageChannel channel) {
    return packageModeChannels.get(channel.getIdLong());
  }

  public List<Message> getMessagesInPackage(Message message) {
    disablePackageMode(message.getChannel());
    List<Message> history;
    try {
      history = message.getChannel().getHistoryBefore(message, HISTORY_LIMIT)
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

  private void scheduleLongStandbyAlert(long channelID, long delayInMinute) {
    PackageModeChannel packageModeChannel = packageModeChannels.get(channelID);
    packageModeChannel.setLongStandbyAlert(longStandbyAlertScheduler.schedule(() -> {
      packageModeChannel.getChannel()
          .sendMessageEmbeds(LONG_STANDBY_EMBED)
          .setActionRows(LONG_STANDBY_ACTION_ROW)
          .mentionRepliedUser(false)
          .queue();
      packageModeChannel.setPackageTermination(packageTerminationScheduler.schedule(
          () -> disablePackageMode(packageModeChannel.getChannel()), WAIT_PACKAGE_TERMINATION.toMinutes(), TimeUnit.MINUTES));
    }, delayInMinute, TimeUnit.MINUTES));
  }

  private static void sendRestartAlert(MessageChannel channel) {
    channel.sendMessageEmbeds(RESTART_EMBED)
        .setActionRows(RESTART_ACTION_ROW)
        .mentionRepliedUser(false)
        .complete();
  }

  public void shutdown() {
    longStandbyAlertScheduler.shutdownNow();
    packageTerminationScheduler.shutdownNow();
    packageModeChannels.values().stream()
        .map(PackageModeChannel::getChannel)
        .forEach(PackageManager::sendRestartAlert);
  }
}