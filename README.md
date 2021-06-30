# QSL Buildscript Prototype

## Goals

### Short-term
- Don't be as ugly as the Fabric buildscript
- Avoid hash hell and require sensible versioning for modules

### Long-term
- Make merging QSL updates as "hands-off" as possible

# Quilt Standard Libraries

## Repository structure

The repository has 2 main parts:

- The `library` folder. This contains all the libraries that are part of the Quilt Standard Libraries.
- The `build-logic` folder. This is an included build in Gradle and contains most of the buildscript used inside the
  libraries. This keeps the buildscripts inside the `library` folder as minimal as possible; definitions of data rather
  than logic.

