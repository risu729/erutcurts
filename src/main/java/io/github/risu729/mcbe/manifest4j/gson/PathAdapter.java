/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.mcbe.manifest4j.gson;

import java.io.IOException;
import java.nio.file.Path;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.TypeAdapter;

final class PathAdapter extends TypeAdapter<Path> {

  @Override
  public Path read(JsonReader reader) throws IOException {
    return Path.of(reader.nextString());
  }

  @Override
  public void write(JsonWriter writer, Path value) throws IOException {
    writer.value(value.toString());
  }
}