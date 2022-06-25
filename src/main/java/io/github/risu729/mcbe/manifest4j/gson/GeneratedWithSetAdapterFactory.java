/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.mcbe.manifest4j.gson;

import java.io.IOException;
import java.util.TreeSet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;

import io.github.risu729.mcbe.manifest4j.Metadata;
import io.github.risu729.mcbe.manifest4j.SemVer;

final class GeneratedWithSetAdapterFactory implements TypeAdapterFactory {

  private static final TypeToken<TreeSet<Metadata.GeneratedWith>> GENERATED_WITH_SET = new TypeToken<TreeSet<Metadata.GeneratedWith>>() {
  };
  private static final TypeAdapter<Metadata.GeneratedWith> ELEMENT_ADAPTER = new GeneratedWithAdapter().nullSafe();

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {

    if (!type.equals(GENERATED_WITH_SET)) {
      return null;
    }

    return new TypeAdapter<T>() {
      @Override
      @SuppressWarnings("unchecked")
      public T read(JsonReader reader) throws IOException {
        TreeSet<Metadata.GeneratedWith> set = new TreeSet<>();
        reader.beginObject();
        while (reader.hasNext()) {
          set.add(ELEMENT_ADAPTER.read(reader));
        }
        reader.endObject();
        return (T) set;
      }

      @Override
      @SuppressWarnings("unchecked")
      public void write(JsonWriter writer, T value) throws IOException {
        writer.beginObject();
        for (Metadata.GeneratedWith e : (TreeSet<Metadata.GeneratedWith>) value) {
          ELEMENT_ADAPTER.write(writer, e);
        }
        writer.endObject();
      }
    }.nullSafe();
  }

  private static final class GeneratedWithAdapter extends TypeAdapter<Metadata.GeneratedWith> {

    @Override
    public Metadata.GeneratedWith read(JsonReader reader) throws IOException {
      var builder = new Metadata.GeneratedWith.Builder()
          .name(reader.nextName());
      reader.beginArray();
      while (reader.hasNext()) {
        builder.addVersions(SemVer.fromString(reader.nextString()));
      }
      reader.endArray();
      return builder.build();
    }

    @Override
    public void write(JsonWriter writer, Metadata.GeneratedWith value) throws IOException {
      writer.name(value.getName())
          .beginArray()
          .setIndent("");
      for (SemVer semVer : value.getVersions()) {
        writer.value(semVer.toString());
      }
      writer.endArray()
          .setIndent("  ");
    }
  }
}