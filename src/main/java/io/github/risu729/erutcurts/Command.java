/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

enum Command {

  DEBUG("debug"), // TODO: delete this
  HELP("help"),
  PACKAGE("package", "p"),
  GENERATE("generate", "g"),
  AUTO_GENERATE();

  public static final char PREFIX = '.';

  public static Optional<Command> fromString(String command) {
    for (var e : Command.values()) {
      if (e.getCommands().stream().anyMatch(command::equalsIgnoreCase)) {
        return Optional.of(e);
      }
    }
    return Optional.empty();
  }

  public static boolean isCommand(String str) {
    return Arrays.stream(Command.values())
        .map(Command::getCommands)
        .flatMap(Set::stream)
        .anyMatch(str::equalsIgnoreCase);
  }

  private final Set<String> commands;

  private Command() {
    this.commands = Collections.emptySet();
  }

  private Command(String... commands) {
    if (commands == null || commands.length == 0) {
      throw new IllegalArgumentException();
    }
    this.commands = Arrays.stream(commands).map(s -> PREFIX + s).collect(Collectors.toSet());
  }

  public Set<String> getCommands() {
    return commands;
  }
}