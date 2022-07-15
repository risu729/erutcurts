/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.util.concurrent.ScheduledFuture;
import java.util.Objects;

import net.dv8tion.jda.api.entities.MessageChannel;

final class PackageModeChannel {

  private final MessageChannel channel;
  private ScheduledFuture<?> longStandbyAlert;
  private ScheduledFuture<?> packageTermination;

  public PackageModeChannel(MessageChannel channel) {
    this.channel = channel;
  }

  public MessageChannel getChannel() {
    return channel;
  }

  public void setLongStandbyAlert(ScheduledFuture<?> longStandbyAlert) {
    this.longStandbyAlert = longStandbyAlert;
  }

  public ScheduledFuture<?> getLongStandbyAlert() {
    return Objects.requireNonNull(longStandbyAlert);
  }

  public boolean cancelLongStandbyAlert() {
    if (longStandbyAlert == null) {
      return false;
    }
    return longStandbyAlert.cancel(true);
  }

  public void setPackageTermination(ScheduledFuture<?> packageTermination) {
    this.packageTermination = packageTermination;
  }

  public ScheduledFuture<?> getPackageTermination() {
    return Objects.requireNonNull(packageTermination);
  }

  public boolean cancelPackageTermination() {
    if (packageTermination == null) {
      return false;
    }
    return packageTermination.cancel(true);
  }

  public boolean cancelAll() {
    return cancelLongStandbyAlert() && cancelPackageTermination();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return (obj instanceof PackageModeChannel other)
        && this.getChannel().getIdLong() == other.getChannel().getIdLong();
  }

  @Override
  public int hashCode() {
    return Long.hashCode(this.getChannel().getIdLong());
  }

  @Override
  public String toString() {
    var str = new StringBuilder();
    str.append("channel: ");
    str.append(channel);
    str.append("\nlongStandbyAlert: ");
    str.append(longStandbyAlert);
    str.append("\ncancelPackageTermination: ");
    str.append(packageTermination);
    return str.toString();
  }
}