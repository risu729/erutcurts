/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.mcbe.manifest4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.TreeSet;

import io.github.risu729.mcbe.manifest4j.gson.ManifestGson;

public final class Metadata {

  private final TreeSet<String> authors;
  private final URL url;
  private final String license;
  private final TreeSet<GeneratedWith> generatedWith;

  @SuppressWarnings("unchecked")
  public TreeSet<String> getAuthors() {
    return (TreeSet<String>) authors.clone();
  }

  public URL getURL() {
    return url;
  }

  public String getLicense() {
    return license;
  }

  @SuppressWarnings("unchecked")
  public TreeSet<GeneratedWith> getGeneratedWith() {
    return (TreeSet<GeneratedWith>) generatedWith.clone();
  }

  public static final class GeneratedWith implements Comparable<GeneratedWith> {
    
    private static final TreeSet<SemVer> DEFAULT_VERSIONS = new TreeSet<>(Set.of(SemVer.DEFAULT));

    static final Comparator<GeneratedWith> STRICT_COMPARATOR =
        Comparator.comparing(GeneratedWith::getName, Comparator.nullsFirst(Comparator.naturalOrder()));
    private static final Comparator<GeneratedWith> COMPARATOR = STRICT_COMPARATOR
        .thenComparing(GeneratedWith::getVersions, Comparator.nullsFirst(new Comparator<TreeSet<SemVer>> () {
          @Override
          public int compare(TreeSet<SemVer> o1, TreeSet<SemVer> o2) {
            return Arrays.compare(o1.toArray(new SemVer[o1.size()]), o2.toArray(new SemVer[o2.size()]));
          }
        }));
    
    // necessary, must be 32 characters maximum
    // must contain only alphabets, numbers, underscores, and hyphens
    private final String name;
    private final TreeSet<SemVer> versions; // necessary at least one

    public String getName() {
      return name;
    }

    @SuppressWarnings("unchecked")
    public TreeSet<SemVer> getVersions() {
      return (TreeSet<SemVer>) versions.clone();
    }

    public static class Builder {

      private static final Pattern NAME_REGEX = Pattern.compile("^[\\w-]{0,32}$");
      
      private String name;
      private TreeSet<SemVer> versions;

      public Builder() {
      }

      public Builder(GeneratedWith other) {
        name(other.name);
        versions(other.versions);
      }

      public Builder name(String name) {
        Objects.requireNonNull(name, "name must not be null");
        if (!NAME_REGEX.matcher(name).matches()) {
          throw new IllegalArgumentException(
              "name must be 32 characters maximum and must contain only alphabets, numbers, underscores, and hyphens : "
                  + name);
        }
        this.name = name;
        return this;
      }

      public Builder versions(SemVer... versions) {
        return versions(versions == null ? null : Set.of(versions));
      }

      public Builder versions(Collection<SemVer> versions) {
        this.versions = null;
        return addVersions(versions);
      }

      public Builder addVersions(SemVer... versions) {
        return addVersions(versions == null ? null : Set.of(versions));
      }

      public Builder addVersions(Collection<SemVer> versions) {
        if (versions == null || versions.isEmpty()) {
          return this;
        }
        for (var e : versions) {
          Objects.requireNonNull(e, "version must not be null");
        }
        if (this.versions == null) {
          this.versions = new TreeSet<>(versions);
          return this;
        }
        this.versions.addAll(versions);
        return this;
      }

      public GeneratedWith build() {
        return new GeneratedWith(this);
      }
    }

    @SuppressWarnings("unchecked")
    private GeneratedWith(Builder builder) {
      this.name = Objects.requireNonNull(builder.name, "name must not be null");
      if (builder.versions == null) {
        this.versions = DEFAULT_VERSIONS;
      } else {
        this.versions = (TreeSet<SemVer>) builder.versions.clone(); 
      }
    }

    @Override
    public int compareTo(GeneratedWith other) {
      return COMPARATOR.compare(this, other);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      return (obj instanceof GeneratedWith other)
          && Objects.equals(name, other.name)
          && Objects.equals(versions, other.versions);
    }

    @Override
    public int hashCode() {
      int hash = 1;
      hash = hash * 31 + Objects.hashCode(name);
      hash = hash * 31 + Objects.hashCode(versions);
      return hash;
    }

    @Override
    public String toString() {
      return ManifestGson.SERIALIZE_NULLS.toJson(this);
    }
  }

  public static class Builder {
    
    private TreeSet<String> authors;
    private URL url;
    private String license;
    private TreeSet<GeneratedWith> generatedWith;

    public Builder() {
    }

    public Builder(Metadata metadata) {
      authors(metadata.authors);
      url(metadata.url);
      license(metadata.license);
      generatedWith(metadata.generatedWith);
    }

    public Builder authors(String... authors) {
      return authors(authors == null ? null : Set.of(authors));
    }

    public Builder authors(Collection<String> authors) {
      this.authors = null;
      return addAuthors(authors);
    }

    public Builder addAuthors(String... authors) {
      return addAuthors(authors == null ? null : Set.of(authors));
    }

    public Builder addAuthors(Collection<String> authors) {
      if (authors == null || authors.isEmpty()) {
        return this;
      }
      for (var e : authors) {
        Objects.requireNonNull(e, "author must not be null");
      }
      if (this.authors == null) {
        this.authors = new TreeSet<>(authors);
        return this;
      }
      this.authors.addAll(authors);
      return this;
    }

    public Builder url(URL url) {
      this.url = url;
      return this;
    }

    public Builder url(String url) {
      try {
        return url(url == null ? null : new URL(url));
      } catch (MalformedURLException e) {
        throw new IllegalArgumentException(e);
      }
    }

    public Builder license(String license) {
      this.license = license;
      return this;
    }

    public Builder generatedWith(GeneratedWith... generatedWith) {
      return generatedWith(generatedWith == null ? null : Set.of(generatedWith));
    }

    public Builder generatedWith(Collection<GeneratedWith> generatedWith) {
      this.generatedWith = null;
      return addGeneratedWith(generatedWith);
    }

    public Builder addGeneratedWith(GeneratedWith... generatedWith) {
      return addGeneratedWith(generatedWith == null ? null : Set.of(generatedWith));
    }

    public Builder addGeneratedWith(Collection<GeneratedWith> generatedWith) {
      if (generatedWith == null || generatedWith.isEmpty()) {
        return this;
      }
      for (var e : generatedWith) {
        Objects.requireNonNull(e, "generated_with must not be null");
      }
      if (this.generatedWith == null) {
        this.generatedWith = new TreeSet<>(GeneratedWith.STRICT_COMPARATOR);
      }
      this.generatedWith.addAll(generatedWith);
      return this;
    }

    public Metadata build() {
      return new Metadata(this);
    }
  }

  @SuppressWarnings("unchecked")
  private Metadata(Builder builder) {
    if (builder.authors == null) {
      this.authors = null;
    } else {
      this.authors = (TreeSet<String>) builder.authors.clone();
    }
    this.url = builder.url;
    this.license = builder.license;
    if (builder.generatedWith == null) {
      this.generatedWith = null;
    } else {
      this.generatedWith = (TreeSet<GeneratedWith>) builder.generatedWith.clone();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return (obj instanceof Metadata other)
        && Objects.equals(authors, other.authors)
        && Objects.equals(url, other.url)
        && Objects.equals(license, other.license)
        && Objects.equals(generatedWith, other.generatedWith);
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 31 + Objects.hashCode(authors);
    hash = hash * 31 + Objects.hashCode(url);
    hash = hash * 31 + Objects.hashCode(license);
    hash = hash * 31 + Objects.hashCode(generatedWith);
    return hash;
  }

  @Override
  public String toString() {
    return ManifestGson.SERIALIZE_NULLS.toJson(this);
  }
}