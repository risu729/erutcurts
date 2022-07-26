import io.github.risu729.erutcurts.util.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public final class Main {
  public static void main(String[] args) throws Exception {
    System.out.println(FileUtil.isExtension(Path.of("src.txt"), Set.of("txt")));
  }
}
