import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import net.lingala.zip4j.ZipFile;

public final class Main {
  public static void main(String[] args) throws Exception {
    Path resources = Path.of("src", "test", "resources");
    new ZipFile(resources.resolve("test.mcpack").toFile()).extractAll(resources.toString());
    Files.list(resources).forEach(System.out::println);
  }
}