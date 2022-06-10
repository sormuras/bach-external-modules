import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.TreeSet;

/**
 * Convert (Maven) Bill of Materials to (Java) Bill of Modules.
 *
 * <ul>
 *   <li>{@code bom2bom org.junit junit-bom 5.8.2}
 *   <li>{@code bom2bom org.lwjgl lwjgl-bom 3.3.1}
 * </ul>
 */
class bom2bom {
  public static void main(String... args) throws Exception {
    if (args.length != 3) {
      System.err.println("Usage: bom2bom GroupId ArtifactId Version");
      return;
    }
    var pom = Maven.builder(args[0], args[1], args[2]).type("pom");
    var xml = read(pom.toDownloadUrl());
    var dependencies = dependencies(xml);

    var lines = new TreeSet<>();
    var temp = Path.of(System.getProperty("java.io.tmpdir"), "bom2bom", pom.toCoordinates("+"));
    for (var dependency : dependencies) {
      var file = temp.resolve(dependency.artifact() + "." + dependency.type());
      var size = copy(dependency.toDownloadUrl(), file);
      var modules =
          ModuleFinder.of(file).findAll().stream()
              .map(ModuleReference::descriptor)
              .map(ModuleDescriptor::name)
              .sorted()
              .toList();
      var key = String.join(",", modules);
      var value = dependency.toDownloadUrl() + "#SIZE=" + size;
      lines.add(key + '=' + value);
    }
    lines.forEach(System.out::println);
  }

  static List<Maven.Builder> dependencies(String xml) {
    if (xml.contains("<packaging>jar</packaging>") || !xml.contains("<packaging>")) {
      return List.of(builder(xml));
    }
    if (!xml.contains("<packaging>pom</packaging>")) throw new Error();
    var builders = new ArrayList<Maven.Builder>();
    var beginDependencies = xml.indexOf("<dependencies>");
    var endDependencies = xml.lastIndexOf("</dependencies>");
    var string = xml.substring(beginDependencies + 14, endDependencies);
    while (true) {
      var beginDependency = string.indexOf("<dependency>");
      if (beginDependency == -1) break;
      var endDependency = string.indexOf("</dependency>", beginDependency);
      var gav = string.substring(beginDependency + 12, endDependency);
      string = string.substring(endDependency + 13); // consume dependency element
      builders.add(builder(gav));
    }
    return List.copyOf(builders);
  }

  static Maven.Builder builder(String gav) {
    var g = gav.substring(gav.indexOf("<groupId>") + 9, gav.indexOf("</groupId>"));
    var a = gav.substring(gav.indexOf("<artifactId>") + 12, gav.indexOf("</artifactId>"));
    var v = gav.substring(gav.indexOf("<version>") + 9, gav.indexOf("</version>"));
    var builder = Maven.builder(g, a, v);
    if (gav.contains("<classifier>")) {
      var c = gav.substring(gav.indexOf("<classifier>") + 12, gav.indexOf("</classifier>"));
      builder = builder.classifier(c);
    }
    return builder;
  }

  static String read(String url) throws Exception {
    try (var in = new URL(url).openStream()) {
      byte[] bytes = in.readAllBytes();
      return new String(bytes);
    }
  }

  static long copy(String uri, Path target) throws Exception {
    if (Files.exists(target)) return Files.size(target);
    try (var stream = URI.create(uri).toURL().openStream()) {
      var parent = target.getParent();
      if (parent != null) Files.createDirectories(parent);
      return Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
      // out.printf("Loaded %,12d %s%n", size, target.getFileName());
    }
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
