import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import net.lingala.zip4j.ZipFile;

import io.github.risu729.erutcurts.util.*;

public final class Main {
  public static void main(String[] args) throws Exception {
    System.out.println(FileUtil.isExtension(Path.of("src.txt"), Set.of("txt")));
  }
}