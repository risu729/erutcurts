package io.github.risu729.erutcurts;

import net.dv8tion.jda.api.entities.MessageChannel;

final class PackageCommands {

  private PackageCommands() {
    throw new AssertionError();
  }

  public static void sendLongStandbyAlert(MessageChannel channel) {
    channel.sendMessage("currently unsupported").queue(); // TODO
  }

  public static void sendRebootAlert(MessageChannel channel) {
    channel.sendMessage("currently unsupported").queue(); // TODO
  }
}