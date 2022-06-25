/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.mcbe.manifest4j.gson;

import java.lang.reflect.Field;
import java.util.function.Predicate;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.FieldNamingStrategy;

final class SnakeCaseField implements FieldNamingStrategy {

  private static final Pattern CAMEL_SEPARATOR = Pattern.compile(
      "(?<=[A-Z])(?=[A-Z][a-z])|(?<=[^A-Z])(?=[A-Z])|(?<=[0-9])(?=[^A-Z0-9])|(?=[0-9])(?<=[^0-9])|(?<=[^A-Za-z0-9])(?=[a-z])|(?=[^A-Za-z0-9])(?<=[A-Za-z])");

  @Override
  public String translateName(Field f) {
    return camelToSnake(f.getName());
  }

  private String camelToSnake(String str) {
    return CAMEL_SEPARATOR.splitAsStream(str)
        .filter(Predicate.not(String::isBlank))
        .map(s -> s.toLowerCase(Locale.ENGLISH))
        .collect(Collectors.joining("_"));
  }
}