import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

public class JavaFXGenerator {
  public static void main(String... args) throws Exception {
    var version = "18.0.1";
    var generator =
        new Generator(
            version,
            __ -> "https://repo.maven.apache.org/maven2",
            __ -> "org.openjfx",
            module -> "javafx-" + module.substring(7));

    var modules =
        List.of(
            "javafx.base",
            "javafx.controls",
            "javafx.fxml",
            "javafx.graphics",
            "javafx.media",
            "javafx.swing",
            "javafx.web");

    var classifiers =
        List.of(
            // all (platform-agnostic)
            new Classifier("", ""),
            // linux
            new Classifier("linux", "linux"),
            new Classifier("linux-aarch64", "linux-arm_64"),
            // macos
            new Classifier("mac", "osx"),
            new Classifier("mac-aarch64", "osx-arm_64"),
            // windows
            new Classifier("win", "windows"),
            new Classifier("win-x86", "windows-x86_32"));

    var http = HttpClient.newHttpClient();
    var lines = new ArrayList<String>();
    for (var module : modules) {
      System.out.println(module);
      lines.add("# " + module);
      for (var classifier : classifiers) {
        var uri = generator.generateUri(module, classifier.library());
        var request = HttpRequest.newBuilder(URI.create(uri)).HEAD().build();
        var response = http.send(request, HttpResponse.BodyHandlers.discarding());
        if (response.statusCode() != 200) continue;
        var contentLength = response.headers().firstValueAsLong("content-length");
        if (contentLength.isEmpty()) continue;
        var size = contentLength.getAsLong();
        if (size < 0) continue;
        var empty = classifier.library().isEmpty();
        var name = module + (empty ? "" : "|" + classifier.normalized());
        lines.add("%s=%s#SIZE=%d".formatted(name, uri, size));
      }
    }
    var directory = Path.of("properties", "javafx");
    Files.createDirectories(directory);
    Files.write(directory.resolve("javafx@" + version + "-modules.properties"), lines);
  }

  record Classifier(String library, String normalized) {}

  /** Generate (Java) Bill of Modules. */
  record Generator(
      String version,
      UnaryOperator<String> repositoryGenerator,
      UnaryOperator<String> groupIdGenerator,
      UnaryOperator<String> artifactIdGenerator) {

    String generateUri(String module, String classifier) {
      var repository = repositoryGenerator.apply(module);
      var group = groupIdGenerator.apply(module).replace('.', '/');
      var artifact = artifactIdGenerator.apply(module);
      return generateUri(repository, group, artifact, classifier);
    }

    String generateUri(String repository, String group, String artifact, String classifier) {
      var uri = new StringJoiner("/");
      uri.add(repository);
      uri.add(group);
      uri.add(artifact);
      uri.add(version);
      uri.add(generateFileName(artifact, classifier));
      return uri + ".jar";
    }

    String generateFileName(String artifact, String classifier) {
      var filename = new StringJoiner("-");
      filename.add(artifact);
      filename.add(version);
      if (!classifier.isEmpty()) filename.add(classifier);
      return filename.toString();
    }
  }
}
