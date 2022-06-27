/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

enum Command {
  
  HELP("help"),
  MULTI("multi", "m"),
  SINGLE("single", "s");
  // INDEX("index", "i"),
  // DELETE("delete", "d"),
  // PACKAGE(),
  // END();

  public static final char PREFIX = '.';

  public static Command fromString(String command) {
    for (var e : Command.values()) {
      if (e.getCommands().stream().anyMatch(command::equalsIgnoreCase)) {
        return e;
      }
    }
    throw new IllegalArgumentException("undefined command: " + command);
  }

  private final Set<String> commands;

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