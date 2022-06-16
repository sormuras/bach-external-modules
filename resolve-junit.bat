@ECHO OFF

cls

for %%x in (
  5.8.0
  5.8.1
  5.8.2
  5.9.0-M1
) do (
  echo Resolving modules for JUnit %%x
  call mvn --file properties\junit\junit.xml --batch-mode -quiet -Dversion=%%x -DoutputFile=junit@%%x-modules.maven -DoutputAbsoluteArtifactFilename=true dependency:resolve
  java src\maven2properties.java properties\junit\junit@%%x-modules.maven > properties\junit\junit@%%x-modules.properties
  del properties\junit\junit@%%x-modules.maven
)
