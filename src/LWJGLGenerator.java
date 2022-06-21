import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.UnaryOperator;

public class LWJGLGenerator {
  public static void main(String... args) throws Exception {
    var modules =
        Set.of(
            "lwjgl",
            "lwjgl.assimp",
            "lwjgl.bgfx",
            "lwjgl.bom",
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

    var generator =
        new Generator(
            "3.3.1",
            modules,
            __ -> "https://repo.maven.apache.org/maven2",
            __ -> "org.lwjgl",
            module -> module.replace('.', '-'));

    var classifiers =
        Set.of(
            // all
            "",
            // linux
            "natives-linux",
            "natives-linux-arm32",
            "natives-linux-arm64",
            // macos
            "natives-macos",
            "natives-macos-arm64",
            // windows
            "natives-windows",
            "natives-windows-x86",
            "natives-windows-arm64");

    var http = HttpClient.newHttpClient();

    for (var classifier : new TreeSet<>(classifiers)) {
      System.out.println();
      System.out.println('"' + classifier + '"');
      var map = generator.generate(classifier);
      for (var entry : map.entrySet()) {
        var module = entry.getKey();
        var uri = entry.getValue();
        var request = HttpRequest.newBuilder(URI.create(uri)).HEAD().build();
        var response = http.send(request, HttpResponse.BodyHandlers.discarding());
        if (response.statusCode() != 200) continue;
        var contentLength = response.headers().firstValueAsLong("content-length");
        if (contentLength.isEmpty()) continue;
        var size = contentLength.getAsLong();
        if (size < 0) continue;
        System.out.printf("%s -> %s#SIZE=%d%n", module, uri, size);
      }
    }
  }

  /** Generate (Java) Bill of Modules. */
  record Generator(
      String version,
      Set<String> modules,
      UnaryOperator<String> repositoryGenerator,
      UnaryOperator<String> groupIdGenerator,
      UnaryOperator<String> artifactIdGenerator) {

    Map<String, String> generate(String classifier) {
      var map = new TreeMap<String, String>();
      for (var module : modules) {
        var repository = repositoryGenerator.apply(module);
        var group = groupIdGenerator.apply(module).replace('.', '/');
        var artifact = artifactIdGenerator.apply(module);
        var uri = generateUri(repository, group, artifact, classifier);
        map.put(module, uri + ".jar");
      }
      return map;
    }

    String generateFileName(String artifact, String classifier) {
      var filename = new StringJoiner("-");
      filename.add(artifact);
      filename.add(version);
      if (!classifier.isEmpty()) filename.add(classifier);
      return filename.toString();
    }

    String generateUri(String repository, String group, String artifact, String classifier) {
      var uri = new StringJoiner("/");
      uri.add(repository);
      uri.add(group);
      uri.add(artifact);
      uri.add(version);
      uri.add(generateFileName(artifact, classifier));
      return uri.toString();
    }
  }
}
