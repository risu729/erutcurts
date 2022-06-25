/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public final class PingListener extends ListenerAdapter {

  private static final String pingCommand = "!ping";
  
  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return;
    }
    if (event.getMessage().getContentRaw().equalsIgnoreCase(pingCommand)) {
      long time = System.currentTimeMillis();
      event.getChannel()
        .sendMessage("pong! (" + (System.currentTimeMillis() - time) + "ms)")
        .queue(m -> m.editMessageFormat("pong! (%d ms)", System.currentTimeMillis() - time).queue());
    }
  }
}
