/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import com.github.steveice10.opennbt.NBTIO;
import net.dv8tion.jda.api.entities.Message;

import io.github.risu729.erutcurts.command.*;

final class NBTTest {

  public static void replyNBT(Message message, Path nbt) {
    try {
      UtilCommands.replyDebugInfo(message, NBTIO.readFile(nbt.toFile(), false, true).getValue());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}