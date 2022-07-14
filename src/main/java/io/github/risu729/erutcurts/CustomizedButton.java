/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.util.Arrays;
import java.util.Optional;

import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

enum CustomizedButton {

  SINGLE(ButtonStyle.PRIMARY, "single", "Single"),
  INDEX(ButtonStyle.PRIMARY, "index", "Index"),
  DELETE(ButtonStyle.DANGER, "delete", "Delete"),
  HELP(ButtonStyle.SECONDARY, "help", "Help"),
  DISMISS(ButtonStyle.DANGER, "dismiss", "Dismiss"),
  HELP_URL(ButtonStyle.LINK, "https://github.com/risu729/erutcurts/blob/main/README.md", "More");

  public static Optional<CustomizedButton> fromButton(Button button) {
    return fromID(button.getId());
  }

  public static Optional<CustomizedButton> fromID(String id) {
    return Arrays.stream(CustomizedButton.values())
        .filter(c -> id.equals(c.toButton().getId()))
        .findFirst();
  }

  private final Button button;

  private CustomizedButton(ButtonStyle style, String idOrURL, String label) {
    button = Button.of(style, idOrURL, label);
  }

  public Button toButton() {
    return button;
  }
}