/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public enum CustomizedButton {
  SINGLE(ButtonStyle.PRIMARY, "Single"),
  INDEX(ButtonStyle.PRIMARY, "Index"),
  DELETE(ButtonStyle.DANGER, "Delete"),
  HELP(ButtonStyle.SECONDARY, "Help"),
  HELP_URL(ButtonStyle.LINK, "https://github.com/risu729/erutcurts/blob/main/README.md", "More"),
  OK(ButtonStyle.SUCCESS, "OK"),
  OK_LONG_STANDBY(ButtonStyle.SUCCESS, "OK"),
  DISMISS_LONG_STANDBY(ButtonStyle.DANGER, "Dismiss"),
  CONTINUE_RESTART(ButtonStyle.SUCCESS, "Continue"),
  DETAIL_ERROR(ButtonStyle.PRIMARY, "Detail");

  public static Optional<CustomizedButton> fromButton(Button button) {
    return fromID(button.getId());
  }

  public static Optional<CustomizedButton> fromID(String id) {
    return Arrays.stream(CustomizedButton.values())
        .filter(c -> id.equals(c.toButton().getId()))
        .findFirst();
  }

  private final ButtonStyle style;
  private final String idOrURL;
  private final String label;

  private CustomizedButton(ButtonStyle style, String label) {
    this(style, null, label);
  }

  private CustomizedButton(ButtonStyle style, String idOrURL, String label) {
    this.style = style;
    this.idOrURL = Objects.requireNonNullElseGet(idOrURL, this::toString);
    this.label = label;
  }

  public Button toButton() {
    return Button.of(style, idOrURL, label);
  }

  public Button toButtonDisabled() {
    return toButton().asDisabled();
  }
}
