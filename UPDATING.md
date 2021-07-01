# Updating QSL

**Note: Not done yet, contents of this file are not final!**

Whether it's bumping the QSL version of porting QSL to a new Minecraft version, the following steps should be taken:

## New Minecraft/Loader version

Change the constants in the [Versions] class. All modules and libraries will use the specified Minecraft and loader
versions in this file.

[Versions]: ./build-logic/src/main/java/qsl/internal/Versions.java
