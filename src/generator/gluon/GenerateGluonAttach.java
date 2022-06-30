package generator.gluon;

import generator.LineGenerator;
import generator.Maven;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// TODO Include classifiers, such as: android, desktop, ios
//  https://repo.maven.apache.org/maven2/com/gluonhq/attach/
public class GenerateGluonAttach {

  public static void main(String... args) throws Exception {
    if (args.length == 0) throw new Error("Usage: GenerateGluonAttach VERSION");
    var version = args[0];
    var generator = LineGenerator.of("gluon", "gluon-attach@" + version);

    var group = "com.gluonhq.attach";
    var uris = List.of(
            Maven.central(group, "accelerometer", version),
            Maven.central(group, "audio", version),
            Maven.central(group, "audio-recording", version),
            Maven.central(group, "augmented-reality", version),
            Maven.central(group, "barcode-scan", version),
            Maven.central(group, "battery", version),
            Maven.central(group, "ble", version),
            Maven.central(group, "browser", version),
            Maven.central(group, "cache", version),
            Maven.central(group, "compass", version),
            Maven.central(group, "device", version),
            Maven.central(group, "dialer", version),
            Maven.central(group, "in-app-billing", version),
            Maven.central(group, "keyboard", version),
            Maven.central(group, "lifecycle", version),
            Maven.central(group, "local-notifications", version),
            Maven.central(group, "magnetometer", version),
            Maven.central(group, "orientation", version),
            Maven.central(group, "pictures", version),
            Maven.central(group, "position", version),
            Maven.central(group, "push-notifications", version),
            Maven.central(group, "runtime-args", version),
            Maven.central(group, "settings", version),
            Maven.central(group, "share", version),
            Maven.central(group, "statusbar", version),
            Maven.central(group, "storage", version),
            Maven.central(group, "util", version),
            Maven.central(group, "version", version),
            Maven.central(group, "vibration", version),
            Maven.central(group, "video", version)

    );

    var lines = new ArrayList<String>();
    lines.add("#");
    lines.add("# Gluon Attach " + version);
    lines.add("#");

    for (var uri : uris) generator.generate("", uri).ifPresent(lines::add);

    var directory = Path.of("properties", "gluon");
    Files.createDirectories(directory);
    Files.write(directory.resolve("gluon-attach@" + version + "-modules.properties"), lines);
  }
}
