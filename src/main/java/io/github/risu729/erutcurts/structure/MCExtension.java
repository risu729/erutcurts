/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts.structure;

public enum MCExtension {
  MCADDON("mcaddon"),
  MCFUNCTION("mcfunction"),
  MCPACK("mcpack"),
  MCPERF("mcperf"),
  MCSHORTCUT("mcshortcut"),
  MCSTRUCTURE("mcstructure"),
  MCTEMPLATE("mctemplate"),
  MCWORLD("mcworld"),
  NBT("nbt");

  private final String value;

  private MCExtension(String value) {
    this.value = value;
  }

  public String toStringWithPeriod() {
    return "." + value;
  }

  @Override
  public String toString() {
    return value;
  }
}