package generator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GenerateLWJGL {
  public static void main(String[] args) throws Exception {
    var group = "org.lwjgl";
    var version = "3.3.1";
    var generator = LineGenerator.of("lwjgl@" + version);

    var artifacts =
        List.of(
            new Artifact("lwjgl"),
            new Artifact("lwjgl-assimp"),
            new Artifact("lwjgl-bgfx"),
            new Artifact("lwjgl-cuda"),
            new Artifact("lwjgl-egl"),
            new Artifact("lwjgl-glfw"),
            new Artifact("lwjgl-jawt"),
            new Artifact("lwjgl-jemalloc"),
            new Artifact("lwjgl-libdivide"),
            new Artifact("lwjgl-llvm"),
            new Artifact("lwjgl-lmdb"),
            new Artifact("lwjgl-lz4"),
            new Artifact("lwjgl-meow"),
            new Artifact("lwjgl-meshoptimizer"),
            new Artifact("lwjgl-nanovg"),
            new Artifact("lwjgl-nfd"),
            new Artifact("lwjgl-nuklear"),
            new Artifact("lwjgl-odbc"),
            new Artifact("lwjgl-openal"),
            new Artifact("lwjgl-opencl"),
            new Artifact("lwjgl-opengl"),
            new Artifact("lwjgl-opengles"),
            new Artifact("lwjgl-openvr"),
            new Artifact("lwjgl-openxr"),
            new Artifact("lwjgl-opus"),
            new Artifact("lwjgl-ovr"),
            new Artifact("lwjgl-par"),
            new Artifact("lwjgl-remotery"),
            new Artifact("lwjgl-rpmalloc"),
            new Artifact("lwjgl-shaderc"),
            new Artifact("lwjgl-spvc"),
            new Artifact("lwjgl-sse"),
            new Artifact("lwjgl-stb"),
            new Artifact("lwjgl-tinyexr"),
            new Artifact("lwjgl-tinyfd"),
            new Artifact("lwjgl-tootle"),
            new Artifact("lwjgl-vma"),
            new Artifact("lwjgl-vulkan"),
            new Artifact("lwjgl-xxhash"),
            new Artifact("lwjgl-yoga"),
            new Artifact("lwjgl-zstd"));

    var classifiers =
        List.of(
            // linux
            new Classifier("linux-x86_64", "natives-linux"),
            new Classifier("linux-arm_32", "natives-linux-arm32"),
            new Classifier("linux-arm_64", "natives-linux-arm64"),
            // macos
            new Classifier("mac-x86_64", "natives-macos"),
            new Classifier("mac-arm_64", "natives-macos-arm64"),
            // windows
            new Classifier("windows-x86_32", "natives-windows-x86"),
            new Classifier("windows-x86_64", "natives-windows"),
            new Classifier("windows-arm_64", "natives-windows-arm64"));

    var lines = new ArrayList<String>();
    lines.add("#");
    lines.add("# LWJGL " + version);
    lines.add("#");

    for (var artifact : artifacts) {
      lines.add("");
      lines.add("#");
      lines.add("# " + "org." + artifact.identifier().replace('-', '.'));
      lines.add("#");
      generator
          .generate("", Maven.central(group, artifact.identifier(), version))
          .map(lines::add)
          .orElseThrow();
      for (var classifier : classifiers) {
        var suffix = '|' + classifier.normalized();
        var uri = Maven.central(group, artifact.identifier(), version, classifier.identifier());
        generator.generate(suffix, uri).ifPresent(lines::add);
      }
    }

    var directory = Path.of("properties", "lwjgl");
    Files.createDirectories(directory);
    Files.write(directory.resolve("lwjgl@" + version + "-modules.properties"), lines);
  }
}
