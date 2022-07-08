/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

final class CustomizedButton {

  public static final Button SINGLE = Button.of(ButtonStyle.PRIMARY, "single", "Single");
  public static final Button INDEX = Button.of(ButtonStyle.PRIMARY, "index", "Index");
  public static final Button DELETE = Button.of(ButtonStyle.DANGER, "delete", "Delete");
  public static final Button HELP = Button.of(ButtonStyle.SECONDARY, "help", "Help");
  public static final Button DISMISS = Button.of(ButtonStyle.DANGER, "dismiss", "Dismiss");
  public static final Button HELP_URL = Button.of(
      ButtonStyle.LINK, "https://github.com/risu729/erutcurts/blob/main/README.md", "More");

  private CustomizedButton() {
    throw new AssertionError();
  }
}