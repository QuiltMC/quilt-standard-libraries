# Contributing

This document outlines best-practices and contributing guidelines to the Quilt Standard Libraries.

By contributing to the Quilt Standard Libraries you agree with the [Developer Certificate of Origin (DCO)][DCO].

## Naming conventions

### Use of the `$` character

The `$` character may be used in mixins to mark a semantic separation in the name,
in other words it allows to separate the actual name of the variable and the namespace.

`@Unique` fields must be prefixed with `quilt$`, but `@Unique` methods don't need prefixes.

In the case of a pseudo-local variable (a field used briefly to pass around a local variable of a method between 2 injections of said method),
the field should be named with the namespace first, then the name of the injected method, and finally the name of the local (`quilt$injectedMethod$localName`).

## Licensing & DCO

QSL is licensed under [Apache 2.0][LICENSE], and have a [Developer Certificate of Origin (DCO)][DCO]
which you need to agree with to contribute.
Commit author may be sufficient, but [a sign-off can be added too](https://git-scm.com/docs/git-commit#Documentation/git-commit.txt--s),
and legal names are not required for privacy reasons.

### License Headers

Every Java source file in QSL has a license header with a copyright notice that keeps track of its creation date and last modification date.

Two gradle tasks are dedicated to them:
 - `:checkLicenses` will check the presence and validity of the license header in each Java source file;
 - `:applyLicenses` will automatically apply the correct license header to every Java source file;
    it can be used to add missing headers and update copyright dates across the whole code base.

So, before committing, remember to run `:applyLicenses`!

#### Derivative Work

Please refer to the [DCO].

##### My work comes from Fabric API

If your work comes from Fabric API, then you should put `/// FABRIC` at the beginning of your file,
or in the case you're copying an entire file in it, you can just keep the same header without modification.

Make sure to execute the `:applyLicenses` task as it will apply a special license header to Fabric API-derived files mentioning Fabric's copyright notice.
**Files derived from Fabric must use this license header!**

[LICENSE]: ./LICENSE "Quilt Standard Libraries license file"
[DCO]: ./DEVELOPER_CERTIFICATE_OF_ORIGIN.md "Developer Certificate of Origin file"
