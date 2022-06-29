package generator;

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;
import java.util.Optional;

public record LineGenerator(Path cache, HttpClient http) {
  public static LineGenerator of(String... cache) {
    return new LineGenerator(Path.of(".cache", cache), HttpClient.newHttpClient());
  }

  public Optional<String> generate(String suffix, URI uri) throws Exception {
    var jar = HexFormat.of().toHexDigits(uri.hashCode()) + ".jar";
    var directory = Files.createDirectories(cache);
    var file = directory.resolve(jar);
    if (Files.notExists(file)) {
      var request = HttpRequest.newBuilder(uri).GET().build();
      System.out.println(request);
      var response = http.send(request, HttpResponse.BodyHandlers.ofFile(file));
      if (response.statusCode() != 200) {
        Files.deleteIfExists(file);
        Files.createFile(file);
        return Optional.empty();
      }
    }
    var size = Files.size(file);
    if (size == 0) return Optional.empty();
    var finder = ModuleFinder.of(file);
    var names =
        finder.findAll().stream()
            .map(ModuleReference::descriptor)
            .map(ModuleDescriptor::name)
            .toList();
    var key = "%s%s".formatted(String.join(",", names), suffix);
    var value = "%s#SIZE=%d".formatted(uri, size);
    return Optional.of(key + '=' + value);
  }
}
