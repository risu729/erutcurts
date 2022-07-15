/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.apache.commons.io.FileUtils;

public final class Erutcurts {

  public static final String VERSION = "0.4.4";
  public static final Path TEMP_DIR = Path.of(System.getProperty("java.io.tmpdir")).resolve(Path.of("Erutcurts"));
  public static final OffsetDateTime LAST_RESTART = OffsetDateTime.now(ZoneOffset.UTC);

  public static void main(String[] args) throws LoginException, InterruptedException {
    var listener = new Listener();
    JDA jda = JDABuilder.createDefault(System.getenv().get("BOT_TOKEN"))
        .setActivity(Activity.playing(Command.HELP.getFullFormCommand()))
        .addEventListeners(listener)
        .setEnableShutdownHook(false) // disable shutdown hook of jda to define order of hooks
        .build()
        .awaitReady();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      FileUtils.deleteQuietly(TEMP_DIR.toFile());
      listener.shutdown();
      jda.shutdown();
    }, "Shutdown Hook"));
  }
}