/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.steveice10.opennbt.NBTIO;
import com.github.steveice10.opennbt.SNBTIO;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import io.github.risu729.erutcurts.command.*;
import io.github.risu729.erutcurts.util.*;

final class NBTTest {

  public static void replyNBT(Message message, Path nbt) {
    Path cacheDir = FileUtil.createTempDir();
    try {
      Path file = cacheDir.resolve(Path.of("NBT.txt"));
      SNBTIO.writeFile(NBTIO.readFile(nbt.toFile(), false, true), file, true);
      AttachmentUtil.replySingleFile(message, file, ActionRow.of(CustomizedButton.DELETE.toButton()));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    FileUtil.delete(cacheDir);
  }
}