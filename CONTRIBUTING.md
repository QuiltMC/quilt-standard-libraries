# Contributing

This document outlines best-practices and contributing guidelines to the Quilt Standard Libraries.

## Naming conventions

### Use of the `$` character

The `$` character may be used in mixins to mark a semantic separation in the name,
in other words it allows to separate the actual name of the variable and the namespace.

`@Unique` fields must be prefixed with `quilt$`, but `@Unique` methods don't need prefixes.

In the case of a pseudo-local variable (a field used briefly to pass around a local variable of a method between 2 injections of said method),
the field should be named with the namespace first, then the name of the injected method, and finally the name of the local (`quilt$injectedMethod$localName`).
