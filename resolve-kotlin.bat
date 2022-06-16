@ECHO OFF

cls

for %%x in (
  1.7.0
) do (
  echo Resolving modules for Kotlin %%x
  call mvn --file properties\kotlin\kotlin.xml --batch-mode -quiet -Dversion=%%x -DoutputFile=kotlin@%%x-modules.maven -DoutputAbsoluteArtifactFilename=true dependency:resolve
  java src\maven2properties.java properties\kotlin\kotlin@%%x-modules.maven > properties\kotlin\kotlin@%%x-modules.properties
  del properties\kotlin\kotlin@%%x-modules.maven
)
