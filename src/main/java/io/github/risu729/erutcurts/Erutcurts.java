/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import io.github.risu729.erutcurts.util.FileUtil;

public final class Erutcurts {

  public static final String VERSION = "0.5.6";
  public static final Path TEMP_DIR = Path.of(System.getProperty("java.io.tmpdir")).resolve(Path.of("Erutcurts"));
  public static final OffsetDateTime LAST_RESTART = OffsetDateTime.now(ZoneOffset.UTC);

  public static void main(String[] args) {
    var listener = new Listener();
    JDA jda;
    try {
      jda = JDABuilder.createDefault(System.getenv().get("BOT_TOKEN"))
          .setActivity(Activity.playing(Command.HELP.getFullFormCommand()))
          .addEventListeners(listener)
          .setEnableShutdownHook(false) // disable shutdown hook of jda to define order of hooks
          .build()
          .awaitReady();
    } catch (LoginException | InterruptedException e) {
      throw new RuntimeException(e);
    }
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {      
      FileUtil.delete(TEMP_DIR);
      listener.shutdown();
      jda.shutdown();
    }, "Shutdown Hook"));
    try {
      Files.createDirectories(TEMP_DIR);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}