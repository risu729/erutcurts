/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.time.Duration;

import net.dv8tion.jda.api.entities.MessageChannel;

final class PackageCommands {

  static final Duration FIRST_PACKAGE_MODE_ALERT = Duration.ofHours(3);
  static final Duration SECOND_PACKAGE_MODE_ALERT = Duration.ofDays(1);

  public static void sendLongStandbyAlert(MessageChannel channel) {
    channel.sendMessage("LongStandbyAlert").queue(); // TODO
  }

  public static void sendRebootAlert(MessageChannel channel) {
    channel.sendMessage("RebootAlert").queue(); // TODO
  }

  private PackageCommands() {
    throw new AssertionError();
  }
}