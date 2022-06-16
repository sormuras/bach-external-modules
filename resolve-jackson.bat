@ECHO OFF

cls

for %%x in (
  2.13.3
) do (
  echo Resolving modules for FasterXML %%x
  call mvn --file properties\jackson\jackson.xml --batch-mode -quiet -Dversion=%%x -DoutputFile=jackson@%%x-modules.maven -DoutputAbsoluteArtifactFilename=true -DincludeGroupIds=com.fasterxml dependency:resolve
  java src\maven2properties.java properties\jackson\jackson@%%x-modules.maven > properties\jackson\jackson@%%x-modules.properties
  del properties\jackson\jackson@%%x-modules.maven
)
