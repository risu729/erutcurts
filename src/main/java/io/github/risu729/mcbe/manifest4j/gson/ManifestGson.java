/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.mcbe.manifest4j.gson;

import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.risu729.mcbe.manifest4j.SemVer;

public final class ManifestGson {
  
  private static final GsonBuilder BUILDER = new GsonBuilder()
      .setPrettyPrinting()
      .setFieldNamingStrategy(new SnakeCaseField())
      .registerTypeAdapterFactory(new SnakeCaseEnum())
      .registerTypeAdapterFactory(new GeneratedWithSetAdapterFactory())
      .registerTypeHierarchyAdapter(Path.class, new PathAdapter().nullSafe())
      .registerTypeAdapter(SemVer.class, new SemVerAdapter().nullSafe());

  public static final Gson NORMAL = BUILDER.create();

  public static final Gson SERIALIZE_NULLS = BUILDER.serializeNulls().create();
}