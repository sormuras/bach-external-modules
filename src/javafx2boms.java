import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Generate (Java) Bill of Modules for JavaFX.
 *
 * <ul>
 *   <li>{@code javafx2boms 18}
 *   <li>{@code javafx2boms 18.0.1}
 * </ul>
 */
class javafx2boms {

  private static final String MAVEN_GROUP = "org.openjfx";

  public static void main(String... args) throws Exception {
    if (args.length != 1) {
      System.err.println("Usage: javafx2boms Version");
      return;
    }

    var classifiers =
        List.of(
            // base
            "",
            // Linux
            "linux",
            "linux-aarch64",
            "linux-arm32-monocle",
            // Mac OS
            "mac",
            "mac-aarch64",
            // Windows
            "win",
            "win-x86");

    var modules =
        Set.of(
            "javafx.base",
            "javafx.controls",
            "javafx.fxml",
            "javafx.graphics",
            "javafx.media",
            "javafx.swing",
            "javafx.web");

    var version = args[0];
    var directory = Path.of("properties", "javafx", "javafx@" + version);
    Files.createDirectories(directory);
    for (var classifier : classifiers) {
      var suffix = classifier.isEmpty() ? "" : "_" + classifier.replace('-', '_');
      var filename = "javafx@%s%s.properties".formatted(version, suffix);
      System.out.println(filename);
      var lines = new ArrayList<String>();
      for (var module : modules.stream().sorted().toList()) {
        var artifact = "javafx-" + module.substring(7); // replace "javafx."
        var location = Maven.central(MAVEN_GROUP, artifact, version, classifier);
        var size = size(location);
        if (size == -1) continue;
        lines.add(module + "=" + location + "#SIZE=" + size);
      }
      Files.write(directory.resolve(filename), lines);
    }
  }

  static long size(String uri) throws Exception {
    var http = (HttpURLConnection) new URL(uri).openConnection();
    http.setRequestMethod("HEAD");
    var code = http.getResponseCode();
    if (200 <= code && code < 300) {
      return http.getContentLengthLong();
    }
    return -1;
  }

  static class Maven {

    public static final String CENTRAL_REPOSITORY = "https://repo.maven.apache.org/maven2";
    public static final String DEFAULT_CLASSIFIER = "", DEFAULT_TYPE = "jar";

    public static Builder builder(String group, String artifact, String version) {
      return new Builder(group, artifact, version);
    }

    public static String central(String group, String artifact, String version) {
      return Maven.builder(group, artifact, version).toDownloadUrl();
    }

    public static String central(String group, String artifact, String version, String classifier) {
      return Maven.builder(group, artifact, version).classifier(classifier).toDownloadUrl();
    }

    public record Builder(
        String repository,
        String group,
        String artifact,
        String version,
        String classifier,
        String type) {

      public Builder(String group, String artifact, String version) {
        this(CENTRAL_REPOSITORY, group, artifact, version, DEFAULT_CLASSIFIER, DEFAULT_TYPE);
      }

      public String toDownloadUrl() {
        var joiner = new StringJoiner("/").add(repository);
        joiner.add(group.replace('.', '/')).add(artifact).add(version);
        var file = artifact + '-' + (classifier.isBlank() ? version : version + '-' + classifier);
        return joiner.add(file + '.' + type).toString();
      }

      public String toCoordinates(String delimiter) {
        return new StringJoiner(delimiter).add(group).add(artifact).add(version).toString();
      }

      public Builder repository(String repository) {
        return new Builder(repository, group, artifact, version, classifier, type);
      }

      public Builder group(String group) {
        return new Builder(repository, group, artifact, version, classifier, type);
      }

      public Builder artifact(String artifact) {
        return new Builder(repository, group, artifact, version, classifier, type);
      }

      public Builder version(String version) {
        return new Builder(repository, group, artifact, version, classifier, type);
      }

      public Builder classifier(String classifier) {
        return new Builder(repository, group, artifact, version, classifier, type);
      }

      public Builder type(String type) {
        return new Builder(repository, group, artifact, version, classifier, type);
      }
    }

    /** Hidden default constructor. */
    private Maven() {}
  }
}
