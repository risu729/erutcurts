/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.JDABuilder;

public final class Erutcurts {

  public static void main(String[] args) throws LoginException, InterruptedException {
    JDABuilder.createDefault(System.getenv().get("BOT_TOKEN"))
        .setActivity(Activity.playing(".help"))
        .addEventListeners(new CommandListener())
        .build()
        .awaitReady();
  }
}