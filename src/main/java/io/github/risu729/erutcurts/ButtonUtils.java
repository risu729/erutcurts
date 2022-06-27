/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import net.dv8tion.jda.api.interactions.components.buttons.Button;

final class ButtonUtils {

  public static final Button MULTI = Button.primary("multi", "Multi");
  public static final Button SINGLE = Button.primary("single", "Single");
  public static final Button INDEX = Button.primary("index", "Index");
  public static final Button DELETE = Button.danger("delete", "Delete");
  public static final Button HELP = Button.secondary("help", "Help");
  public static final Button HELP_URL = Button.link(
      "https://github.com/risu729/erutcurts/blob/main/README.md", "Help");

  private ButtonUtils() {
    throw new AssertionError();
  }
}