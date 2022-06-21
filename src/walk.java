import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 *
 *
 * <ul>
 *   <li>{@code mvn --file properties/lwjgl/lwjgl.xml dependency:resolve}
 *   <li>{@code mvn --file properties/lwjgl/lwjgl.xml -Dclassifier=natives-windows-x86
 *       dependency:resolve}
 * </ul>
 *
 * <ul>
 *   <li>{@code java src/walk.java glob:**lwjgl*3.3.1.jar}</li>
 *   <li>{@code java src/walk.java glob:**lwjgl*3.3.1-natives-windows-x86.jar}</li>
 *   <li>{@code java src/walk.java glob:**junit-platform*1.8.2.jar glob:**junit-jupiter*5.8.2.jar glob:**junit-vintage*5.8.2.jar glob:**apiguardian-api-1.1.2.jar glob:**opentest4j-1.2.0.jar}
 * </ul>
 */
class walk {
  public static void main(String... args) throws Exception {
    var remote = System.getProperty("remote-repo", "https://repo.maven.apache.org/maven2/");
    var home = System.getProperty("user.home");
    var root = Path.of(home, ".m2", "repository");
    var local = System.getProperty("local-repo", root.toUri().toString());

    var matcher = MultiMatcher.of(args);
    var map = new TreeMap<String, String>();
    try (var stream = Files.find(root, 9, (p, a) -> matcher.matches(p))) {
      for (var path : stream.toList()) {
        var modules =
            ModuleFinder.of(path).findAll().stream()
                .map(ModuleReference::descriptor)
                .map(ModuleDescriptor::name)
                .sorted()
                .toList();
        var key = String.join(",", modules);
        var value = path.toUri().toString().replace(local, remote) + "#SIZE=" + Files.size(path);
        map.put(key, value);
      }
    }
    for (var entry : map.entrySet()) {
      System.out.printf("%s=%s%n", entry.getKey(), entry.getValue());
    }
  }

  record MultiMatcher(List<PathMatcher> matchers) {
    static MultiMatcher of(String... args) {
      var system = FileSystems.getDefault();
      return new MultiMatcher(Stream.of(args).map(system::getPathMatcher).toList());
    }

    boolean matches(Path path) {
      return matchers.stream().anyMatch(matcher -> matcher.matches(path));
    }
  }
}
