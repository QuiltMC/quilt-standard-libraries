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

QSL is licensed under [Apache 2.0][LICENSE], which means contributions should not introduce incompatibly licensed code.

### License Headers

Every Java source file in QSL has a license header with a copyright notice that keeps track of its creation date and last modification date.

Two gradle tasks are dedicated to them:
 - `:checkLicenses` will check the presence and validity of the license header in each Java source file;
 - `:applyLicenses` will automatically apply the correct license header to every Java source file;
    it can be used to add missing headers and update copyright dates across the whole code base.

So, before committing, remember to run `:applyLicenses`!

#### Derivative Work

If your work isn't entirely original, then the default license header should not apply. Here's what to do in different cases:

##### My work comes from Fabric API

If your work comes from Fabric API, then you should put `/// FABRIC` at the beginning of your file,
or in the case you're copying an entire file in it, you can just keep the same header without modification.

Make sure to execute the `:applyLicenses` task as it will apply a special license header to Fabric API-derived files mentioning Fabric's copyright notice. **Files derived from Fabric must use this license header!**

##### My work comes from somewhere else

If the original work is entirely your own, then nothing needs to be done as long as you don't mind it being licensed under [Apache 2][LICENSE].

Otherwise, you should make sure that this work is compatible with the [Apache 2][LICENSE] license
and discuss with a QSL Team member whether the work can be included and how.

[LICENSE]: ./LICENSE "Quilt Standard Libraries license file"
