/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.risu729.mcbe.manifest4j;

import java.nio.file.Path;
import java.util.UUID;

@Deprecated
public class ManifestTemplates {

  public static Manifest get(Module_.Type type, boolean isFull) {
    if (isFull) {
      return // switch(type) {
      // case RESOURCES -> {
      new Manifest.Builder()
          .formatVersion(2)
          .header(new Header.Builder()
              .name("Template Addon")
              .description("This is a template adddon.")
              .uuid(UUID.fromString("410e1ee7-23a9-4d89-a71c-f94792b43966"))
              .version(SemVer.of(1, 0, 0))
              .minEngineVersion(SemVer.of(1, 18, 30))
              .platformLocked(false)
              .packScope(Header.PackScope.GLOBAL)
              .build())
          .modules(new Module_.Builder()
              .type(Module_.Type.RESOURCES)
              .description("This is a resources module")
              .uuid(UUID.fromString("523f173b-a55c-4b6d-a28e-dec9b2d5628e"))
              .version(SemVer.of(1, 2, 5))
              .build())
          .addModules(new Module_.Builder()
              .type(Module_.Type.RESOURCES)
              .description("This is the 2nd resources module")
              .uuid(UUID.fromString("2d8f23e8-6c7a-49e2-bbd2-665131b228fa"))
              .version(SemVer.of(3, 0, 15))
              .build())
          .dependencies(new Dependency.Builder()
              .uuid(UUID.fromString("581b87dc-b8a0-4a5c-ab06-1b975ddd1fe3"))
              .version(SemVer.of(1, 0, 0))
              .build())
          .addDependencies(new Dependency.Builder()
              .uuid(UUID.fromString("024ff39e-7a78-4714-92cb-1921928e1145"))
              .version(SemVer.of(2, 10, 12))
              .build())
          .capabilities(Capability.EXPERIMENTAL_CUSTOM_UI)
          .addCapabilities(Capability.CHEMISTRY)
          .addCapabilities(Capability.RAYTRACED)
          .metadata(new Metadata.Builder()
              .authors("risu")
              .addAuthors("momonga")
              .url("https://github.com/risu-minecraft")
              .license("MIT License")
              .generatedWith(new Metadata.GeneratedWith.Builder()
                  .name("TestTool")
                  .versions(SemVer.of(0, 0, 0))
                  .addVersions(SemVer.of(1, 0, 0))
                  .build())
              .addGeneratedWith(Manifest.MANIFEST4J_GENERATED_WITH)
              .build())
          .subpacks(new Subpack.Builder()
              .folderName(Path.of("Alpha"))
              .name("Subpack Alpha")
              .memoryTier(1)
              .build())
          .addSubpacks(new Subpack.Builder()
              .folderName(Path.of("Beta"))
              .name("Subpack Beta")
              .memoryTier(5)
              .build())
          .build();
      // }
      // }

    } else {
      Module_ module;
      if (type == Module_.Type.SCRIPT) {
        module = new Module_.Builder()
            .type(type)
            .entry(Path.of("test.js"))
            .build();
      } else {
        module = new Module_.Builder()
            .type(type)
            .build();
      }
      return new Manifest.Builder()
          .header(new Header.Builder()
              .name("Template Addon")
              .build())
          .modules(module)
          .build();
    }
  }
}