package generator.javafx;

import generator.Artifact;
import generator.Classifier;
import generator.LineGenerator;
import generator.Maven;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GenerateJavaFX {

  private static final String MAVEN_GROUP = "org.openjfx";

  public static void main(String... args) throws Exception {
    var version = args.length == 0 ? "18" : args[0];
    var generator = LineGenerator.of("javafx", "javafx@" + version);

    var artifacts =
        List.of(
            new Artifact("javafx-base"),
            new Artifact("javafx-controls"),
            new Artifact("javafx-fxml"),
            new Artifact("javafx-graphics"),
            new Artifact("javafx-media"),
            new Artifact("javafx-swing"),
            new Artifact("javafx-web"));

    var classifiers =
        List.of(
            // linux
            new Classifier("linux-x86_64", "linux"),
            new Classifier("linux-arm_64", "linux-aarch64"),
            // macos
            new Classifier("mac-x86_64", "mac"),
            new Classifier("mac-arm_64", "mac-aarch64"),
            // windows
            new Classifier("windows-x86_32", "win-x86"),
            new Classifier("windows-x86_64", "win"));

    var lines = new ArrayList<String>();
    lines.add("#");
    lines.add("# JavaFX " + version);
    lines.add("#");

    for (var artifact : artifacts) {
      lines.add("");
      lines.add("# " + artifact.identifier());
      for (var classifier : classifiers) {
        var suffix = '|' + classifier.normalized();
        var uri = Maven.central(MAVEN_GROUP, artifact.identifier(), version, classifier.identifier());
        generator.generate(suffix, uri).ifPresent(lines::add);
      }
    }

    var directory = Path.of("properties", "javafx");
    Files.createDirectories(directory);
    Files.write(directory.resolve("javafx@" + version + "-modules.properties"), lines);
  }
}
