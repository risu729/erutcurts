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
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.github.steveice10.opennbt.NBTIO;
import com.github.steveice10.opennbt.SNBTIO;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import io.github.risu729.erutcurts.command.*;
import io.github.risu729.erutcurts.util.*;

final class NBTTest {

  public static void replyNBT(Message message, Path nbt) {
    Path cacheDir = FileUtil.createTempDir();
    try {
      // Path file = cacheDir.resolve(Path.of("NBT.txt"));
      UtilCommands.replyDebugInfo(message, NBTIO.readFile(nbt.toFile(), false, true)
        .<CompoundTag>get("structure")
        .<ListTag>get("block_indices")
        .get(0)
        .getValue()
        .stream()
        .mapToInt(IntTag::getValue)
        .filter(i -> i != 0)
        .collect(Collectors.groupingBy(UnaryOperator.identity(), Collectors.counting())));
      // SNBTIO.writeFile(, file.toFile(), true);
      // AttachmentUtil.replySingleFile(message, file, ActionRow.of(CustomizedButton.DELETE.toButton()));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    FileUtil.delete(cacheDir);
  }
}