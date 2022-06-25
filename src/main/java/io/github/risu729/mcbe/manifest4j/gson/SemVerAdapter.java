/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.mcbe.manifest4j.gson;

import java.io.IOException;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.TypeAdapter;

import io.github.risu729.mcbe.manifest4j.SemVer;

final class SemVerAdapter extends TypeAdapter<SemVer> {

  @Override
  public SemVer read(JsonReader reader) throws IOException {
    int[] semVer = new int[3];
    reader.beginArray();
    for (int i = 0; i < 3; i++) {
      semVer[i] = reader.nextInt();
    }
    reader.endArray();
    return SemVer.of(semVer[0], semVer[1], semVer[2]);
  }

  @Override
  public void write(JsonWriter writer, SemVer value) throws IOException {
    writer.beginArray()
        .setIndent("");
    for (int n : value.toArray()) {
      writer.value(n);
    }
    writer.endArray()
        .setIndent("  ");
  }
}