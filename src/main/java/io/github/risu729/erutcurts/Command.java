/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

enum Command {

  DEBUG("debug"), // TODO: delete this
  HELP("help", "h"),
  PACKAGE("package", "p"),
  PACKAGE_CONTINUE("packagecontinue", "pcontinue", "pc"),
  GENERATE("generate", "g"),
  AUTO_GENERATE();

  public static final String PREFIX = "!.";

  public static Optional<Command> fromString(String command) {
    return Arrays.stream(Command.values())
        .filter(c -> c.getCommands()
            .stream()
            .anyMatch(command::equalsIgnoreCase))
        .findFirst();
  }

  private final List<String> commands;

  private Command() {
    this.commands = Collections.emptyList();
  }

  private Command(String... commands) {
    this.commands = Arrays.stream(commands)
        .sorted(Comparator.comparingInt(String::length).reversed())
        .distinct()
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