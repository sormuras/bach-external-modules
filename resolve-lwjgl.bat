@ECHO OFF

cls

for %%V in (
  3.3.1
) do (
  echo Resolving modules for LWJGL %%V
  rem mvn --file properties/lwjgl/lwjgl.xml --batch-mode -quiet -Dversion=%%V dependency:resolve
  java src/walk.java glob:**lwjgl*%%V.jar > properties/lwjgl/lwjgl@%%V-modules.properties
  for %%C in (
    natives-linux
    natives-linux-arm32
    natives-linux-arm64
    natives-macos
    natives-macos-arm64
    natives-windows
    natives-windows-x86
    natives-windows-arm64
  ) do (
    echo Resolving modules for LWJGL %%V-%%C
    rem mvn --file properties/lwjgl/lwjgl.xml --batch-mode -quiet -Dversion=%%V -Dclassifier=%%C dependency:resolve
    java src/walk.java glob:**lwjgl*%%V-%%C.jar > properties/lwjgl/lwjgl@%%V-%%C-modules.properties
  )
)
