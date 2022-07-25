/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public enum Command {

  DEBUG(true), // TODO: delete these
  NBT(true),
  HELP("h"),
  INFO("i"),
  PACKAGE("p"),
  PACKAGE_CONTINUE("packagecon", "pcontinue", "pcon"),
  CANCEL("c"),
  GENERATE("g"),
  AUTO_GENERATE(false);

  public static final String PREFIX = "..";

  public static Optional<Command> fromString(String command) {
    return Arrays.stream(Command.values())
        .filter(c -> c.getCommands()
            .stream()
            .anyMatch(command::equalsIgnoreCase))
        .findFirst();
  }

  private final List<String> commands;

  private Command(boolean doIncludeIdentifier) {
    this(doIncludeIdentifier, new String[0]);
  }

  private Command(String... commands) {
    this(true, commands);
  }

  private Command(boolean doIncludeIdentifier, String... commands) {
    this.commands = Stream.concat(Stream.ofNullable(doIncludeIdentifier ? this.toString().replace("_", "") : null), Arrays.stream(commands))
        .distinct()
        .map(s -> s.toLowerCase(Locale.ENGLISH))
        .sorted(Comparator.comparingInt(String::length).reversed())
        .map(s -> PREFIX + s)
        .toList();
  }

  public List<String> getCommands() {
    return commands;
  }

  public String getFullFormCommand() {
    if (commands.isEmpty()) {
      throw new IllegalStateException("no command strings: " + this);
    }
    return commands.get(0);
  }
}