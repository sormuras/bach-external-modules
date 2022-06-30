package generator.fxgl;

import generator.LineGenerator;
import generator.Maven;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GenerateFXGL {

  public static void main(String... args) throws Exception {
    if (args.length == 0) throw new Error("Usage: GenerateFXGL VERSION");
    var version = args[0];
    var generator = LineGenerator.of("fxgl", "fxgl@" + version);

    var group = "com.github.almasb";
    var uris = List.of(
            Maven.central(group, "fxgl", version),
            Maven.central(group, "fxgl-core", version),
            Maven.central(group, "fxgl-controllerinput", version),
            Maven.central(group, "fxgl-entity", version),
            Maven.central(group, "fxgl-gameplay", version),
            Maven.central(group, "fxgl-io", version),
            // Maven.central(group, "fxgl-samples", version),
            Maven.central(group, "fxgl-scene", version),
            // Maven.central(group, "fxgl-test", version),
            Maven.central(group, "fxgl-tools", version),
            Maven.central(group, "fxgl-zdeploy", version)
    );

    var lines = new ArrayList<String>();
    lines.add("#");
    lines.add("# FXGL " + version);
    lines.add("#");

    for (var uri : uris) generator.generate("", uri).ifPresent(lines::add);

    var directory = Path.of("properties", "fxgl");
    Files.createDirectories(directory);
    Files.write(directory.resolve("fxgl@" + version + "-modules.properties"), lines);
  }
}
