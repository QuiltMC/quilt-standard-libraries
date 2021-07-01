# Quilt Standard Libraries

**Note: At the moment, the Quilt Standard Libraries are not ready for general use. Please make an issue or talk to the
QSL team on discord before writing any PRs.**

## Repository structure

The repository has 2 main parts:

- The `library` folder. This contains all the libraries that are part of the Quilt Standard Libraries.
- The `build-logic` folder. This is an included build in Gradle and contains most of the buildscript used inside the
  libraries. This keeps the buildscripts inside the `library` folder as minimal as possible; definitions of data rather
  than logic.
