package generator;

import java.net.URI;
import java.util.StringJoiner;

public record Maven(
    String repository,
    String group,
    String artifact,
    String version,
    String classifier,
    String type) {

  public static final String CENTRAL_REPOSITORY = "https://repo.maven.apache.org/maven2";
  public static final String DEFAULT_CLASSIFIER = "", DEFAULT_TYPE = "jar";

  public static URI central(String group, String artifact, String version) {
    return new Maven(CENTRAL_REPOSITORY, group, artifact, version, DEFAULT_CLASSIFIER, DEFAULT_TYPE)
        .toUri();
  }

  public static URI central(String group, String artifact, String version, String classifier) {
    return new Maven(CENTRAL_REPOSITORY, group, artifact, version, classifier, DEFAULT_TYPE)
        .toUri();
  }

  public URI toUri() {
    var joiner = new StringJoiner("/").add(repository);
    joiner.add(group.replace('.', '/')).add(artifact).add(version);
    var file = artifact + '-' + (classifier.isBlank() ? version : version + '-' + classifier);
    return URI.create(joiner.add(file + '.' + type).toString());
  }
}
