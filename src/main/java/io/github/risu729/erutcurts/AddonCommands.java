/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.erutcurts;

import static io.github.risu729.erutcurts.CustomizedButton.*;

import java.awt.Color;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import io.github.risu729.erutcurts.structure.StructureAddon;

final class AddonCommands {

  private static final EmbedBuilder INDEX_EMBED_BUILDER = new EmbedBuilder()
      .setTitle("Index")
      .setColor(Color.CYAN);
  private static final ActionRow INDEX_ACTION_ROW = ActionRow.of(DELETE.toButton(), HELP.toButton());

  public static void replyIndex(Message message, Collection<Path> packs) {
    var builder = new EmbedBuilder(INDEX_EMBED_BUILDER);
    for (var p : packs) {
      try (var addon = StructureAddon.fromBehavior(p)) {
        builder.addField(addon.getPackName(), addon.getStructureNames().stream().collect(Collectors.joining("\n")), false);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    message.replyEmbeds(builder.build())
        .setActionRows(INDEX_ACTION_ROW)
        .mentionRepliedUser(false)
        .queue();
  }

  private AddonCommands() {
    throw new AssertionError();
  }
}