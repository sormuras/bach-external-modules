import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;
import java.util.regex.Pattern;

class maven2properties {

  static final Pattern RESOLVED_LINE_PATTERN =
      Pattern.compile(
          " {3}" // "header"
              + "(?<G>.+?):(?<A>.+?):jar:(?<V>.+?)" // GA:type:V
              + ":.+?:" // scope: compile or runtime
              + "(?<PATH>.+?) -- module " // absolute path
              + "(?<MODULE>\\S+)" // module name
              + "(?:.*|$)" // optional source of module name
          );

  public static void main(String... args) throws Exception {
    if (args.length == 0) throw new Error("Usage: maven2properties FILE");
    var remote = System.getProperty("remote-repo", "https://repo.maven.apache.org/maven2/");
    // System.out.println("remote = " + remote);
    var home = System.getProperty("user.home");
    var m2 = Path.of(home, ".m2", "repository").toUri().toString();
    var local = System.getProperty("local-repo", m2);
    // System.out.println("resolve = " + resolve);
    var lines = Files.readAllLines(Path.of(args[0]));
    var map = new TreeMap<String, String>();
    for (var line : lines) {
      var matcher = RESOLVED_LINE_PATTERN.matcher(line);
      if (matcher.matches()) {
        var module = matcher.group("MODULE");
        // System.out.println("    Module: " + module);
        // System.out.println("   GroupId: " + matcher.group("G"));
        // System.out.println("ArtifactId: " + matcher.group("A"));
        // System.out.println("   Version: " + matcher.group("V"));
        var path = Path.of(matcher.group("PATH"));
        // System.out.println("      Path: " + path);
        var size = Files.size(path);
        // System.out.println("      Size: " + size);
        var uri = path.toUri().toString().replace(local, remote);
        // System.out.println("       URI: " + uri);
        map.put(module, uri + "#SIZE=" + size);
      }
    }
    map.forEach((module, uri) -> System.out.printf("%s=%s%n", module, uri));
  }
}
