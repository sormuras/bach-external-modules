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

public class LWJGLGenerator {
  public static void main(String... args) throws Exception {
    var version = "3.3.1";
    var generator =
        new Generator(
            version,
            __ -> "https://repo.maven.apache.org/maven2",
            __ -> "org.lwjgl",
            module -> module.replace('.', '-'));

    var modules =
        List.of(
            "lwjgl",
            "lwjgl.assimp",
            "lwjgl.bgfx",
            //  "lwjgl.bom",
            "lwjgl.cuda",
            "lwjgl.egl",
            "lwjgl.glfw",
            "lwjgl.jawt",
            "lwjgl.jemalloc",
            "lwjgl.libdivide",
            "lwjgl.llvm",
            "lwjgl.lmdb",
            "lwjgl.lz4",
            "lwjgl.meow",
            "lwjgl.meshoptimizer",
            "lwjgl.nanovg",
            "lwjgl.nfd",
            "lwjgl.nuklear",
            "lwjgl.odbc",
            "lwjgl.openal",
            "lwjgl.opencl",
            "lwjgl.opengl",
            "lwjgl.opengles",
            "lwjgl.openvr",
            "lwjgl.openxr",
            "lwjgl.opus",
            "lwjgl.ovr",
            "lwjgl.par",
            "lwjgl.remotery",
            "lwjgl.rpmalloc",
            "lwjgl.shaderc",
            "lwjgl.spvc",
            "lwjgl.sse",
            "lwjgl.stb",
            "lwjgl.tinyexr",
            "lwjgl.tinyfd",
            "lwjgl.tootle",
            "lwjgl.vma",
            "lwjgl.vulkan",
            "lwjgl.xxhash",
            "lwjgl.yoga",
            "lwjgl.zstd");

    var classifiers =
        List.of(
            // all (platform-agnostic)
            new Classifier("", ""),
            // linux
            new Classifier("natives-linux", "linux"),
            new Classifier("natives-linux-arm32", "linux-arm_32"),
            new Classifier("natives-linux-arm64", "linux-arm_64"),
            // macos
            new Classifier("natives-macos", "osx"),
            new Classifier("natives-macos-arm64", "osx-arm_64"),
            // windows
            new Classifier("natives-windows", "windows-x86_32"),
            new Classifier("natives-windows-x86", "windows-x86_64"),
            new Classifier("natives-windows-arm64", "windows-arm_64")
            //
            );

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
        var name = module + (empty ? "" : ".natives|" + classifier.normalized());
        lines.add("%s=%s#SIZE=%d".formatted(name, uri, size));
      }
    }
    var directory = Path.of("properties", "lwjgl");
    Files.createDirectories(directory);
    Files.write(directory.resolve("lwjgl@" + version + "-modules.properties"), lines);
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
