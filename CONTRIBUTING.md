# Contributing

This document outlines best-practices and contributing guidelines to the Quilt Standard Libraries.

## Naming conventions

### Use of the `$` character

The `$` character may be used in mixins to mark a semantic separation in the name,
in other words it allows to separate the actual name of the variable and the namespace.

`@Unique` fields must be prefixed with `quilt$`, but `@Unique` methods don't need prefixes.

In the case of a pseudo-local variable (a field used briefly to pass around a local variable of a method between 2 injections of said method),
the field should be named with the namespace first, then the name of the injected method, and finally the name of the local (`quilt$injectedMethod$localName`).

## Licensing

Quilt Standard Libraries are licensed under [Apache 2.0][LICENSE], which means your contributions should not introduce code of incompatible licensing.

### License Headers

Every Java source file in QSL has a license header, it allows keeping track of creation date,
and last modification for the copyright notice of each file individually.

Two gradle tasks are dedicated to them:
 - `:checkLicenses` will check the presence and validity of the license header in each Java source file;
 - `:applyLicenses` will automatically apply to Java source file the correct license header,
    it can be used to append the header if it's missing, to update dates, and to update in the whole code base easily
    every license headers if necessary.

So, before committing, remember executing `:applyLicenses`!

#### Derivative Work

If your work isn't entirely original, then the default license header should not apply, here's what to do in different cases:

##### My work comes from Fabric API

If your work comes from Fabric API, then you should put `/// FABRIC` at the beginning of your file,
or in the case you're copying an entire file in it, you can just keep the same header without modification.

Make sure to execute the `:applyLicenses` task as it will apply a special license header to Fabric API-derived files mentioning Fabric's copyright notice.

##### My work comes from somewhere else

If it's your own, then not much is needed to be done if you don't mind it being licensed under [Apache 2][LICENSE].

Otherwise, you should make sure that this work is compatible with the [Apache 2][LICENSE] license,
and discuss with a QSL Team member whether the work should be included and how.

[LICENSE]: ./LICENSE "Quilt Standard Libraries license file"
