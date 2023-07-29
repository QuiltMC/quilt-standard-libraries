# Contributing

This document outlines best-practices and contributing guidelines to the Quilt Standard Libraries.

By contributing to the Quilt Standard Libraries you agree with the [Developer Certificate of Origin (DCO)][DCO].

## Guide: Pull Requests

1. ### Discuss your feature
    Be sure to talk to us (either through an issue or on [Discord](https://discord.quiltmc.org/toolchain)) before working on your feature! We can help you with any questions you may have, and save some time during the review process.
2. ### Open your PR and wait for reviews
    Once you have forked QSL and opened a pull request, you need to wait for people to review it. When you get reviews, try to thoughtfully address any concerns other people have. If you get confused, be sure to ask questions!
3. ### Entering a Final Comment Period
    Once your PR has no "changes requested" reviews, the minimum number of approvals for its [triage category](#guide-triage-categories), and nobody has their review requested, it is eligible to enter a Final Comment Period (FCP). A FCP is a last call for any reviewers to look at your PR before it is merged. The minimum length of your PR's FCP is determined by its triage category, but if any further changes are requested, the FCP might be lengthened, or if the concerns are significant, the FCP cancelled until the concerns are resolved.
4. ### Request a merge!
    Once the minimum time on the Final Comment Period has passed, and you have resolved any concerns reviewers have raised during that time, leave a comment on your PR requesting for it to be merged. A QSL Core Team member will take a final look over your PR, and if everything looks good, merge it!


## Guide: Triage Categories

Triage categories ensure that important, but small PRs--like bugfixes--are merged quickly, while large changes--like new stable APIs--are thoroughly reviewed before they are merged.

A "required approval" comes from any member of the QSL Triage team, except for `T: urgent` PRs.

## PR Policy Definitions
Everything within this section is the definitions for the actual PR policy followed by the QSL team, in accordance with [RFC 39](https://github.com/QuiltMC/rfcs/blob/master/structure/0039-pr-policy.md)

### `T: new API`

**Description**: Used for pull requests that add new APIs to QSL, defined as anything in a `$modulename.api` package or subfolders.

**Required Approvals**: 2

**Final Comment Period**: 5 days

### `T: refactor`

**Description**: Used for pull requests that make internal refactors and do not change any API, such as bugfixes or buildscript changes.

**Required Approvals**: 2

**Final Comment Period**: 3 days

### `T: documentation`

**Description**: Used for pull requests that make additions or revisions to the codebase's documentation.

**Required Approvals**: 2

**Final Comment Period**: 3 days

### `T: urgent`

**Description**: For pull requests that must be merged quickly, like ports of critical core modules and game- or build-breaking bugs.

**Required Approvals**: 2
- Only members of the QSL Core Team count for approvals in urgent PRs to prevent abuse.

**Final Comment Period**: N/A

### Other

If the determined FCP length is judged to be inadequate for one or more PRs, at least two members of the QSL Core team may agree to either shorten, extend, or skip them, provided that all QSL Core team members had a reasonable chance to respond. This applies to both PRs that are yet to begin its FCP and to PRs whose FCP is already in motion.

Trivial fixes that do not require review (e.g. typos) are exempt from this policy. QSL Core team members should double check with other members of the team on Discord before pushing a commit or merging a PR without going through this process.

PRs that do not fit under any of these categories but are not "trivial fixes" are merged at the consensus of the QSL Core team, using whatever criteria they determine to be appropriate. (For example, amending the PR policy may require every core member to approve, and have a 1-week FCP).

*This is only a summary of QSL's PR process and an explanation of QSL-specific exceptions to it. For exact definitions and more information, see [RFC 39](https://github.com/QuiltMC/rfcs/blob/master/structure/0039-pr-policy.md).*

## Conventions

### General

Use `UpperCamelCase` for class names.
Use `lowerCamelCase` for method names, variable names, and names of fields that are not both static and final.
Use `UPPER_SNAKE_CASE` for names of fields that are both static and final, excluding atomics like `AtomicInteger`, `AtomicBoolean` or `AtomicReference`.

Method names should generally be verb phrases (`tick`, `getCarversForStep`), except for "withX", "toX", "fromX", "of"
and builder methods.
Class names and non-boolean field and variable names should be noun phrases (`ChunkRegion`, `color`).
Boolean field and variable names should always be adjective phrases or present tense verb phrases (`powered`, `canOpen`), avoiding the `is` and `has` prefixes when possible (`colored`, not `isColored` or `hasColor`).

To make code as easy to read as possible, keep names in the natural language order. For example, a class representing a
chest block entity should be named `ChestBlockEntity` rather than `BlockEntityChest`. Though prefix naming may be
helpful for grouping classes together in an IDE's tree view, reading and writing code is done much more often than
browsing files.

### Spelling

Use American English for consistency throughout QSL.

If there are two acceptable spellings of the same word, first check if one is already being used in QSL, Quilt Mappings
or by Mojang, and if not, use the most common spelling.

### Conciseness

Omit words that are made redundant by parameter names or owner class names. For example, use `getChunk(BlockPos pos)`
rather than `getChunkAtPosition(BlockPos pos)` and `Box.create` rather than `Box.createBox`. Don't avoid overloading
methods or shadowing fields.

However, it's more important for a name to be descriptive rather than short, so don't omit important words. When naming
something always look at all its usages, including overriding methods and inheriting classes.

It's important to be concise especially with names used in many places throughout the code, while more obscure names can
be longer and more descriptive.

### Abbreviations

Avoid abbreviations unless it's a common one everyone knows and other Quilt Mappings names involving the same word use
its abbreviated form. Full names are easier to read quickly and remember ("Which words were abbreviated?") and they
often don't take more time to type thanks to IDE autocompletion. Common abbreviations you should use are:

- "id" for "identifier"
- "pos" for "position"
- "nbt" for "named binary tag"
- "init" for "initialize"
- "min"/"max" for "minimum"/"maximum"
- Any abbreviations used by Java or libraries ("json", "html", etc.)
- "o" for the parameter of `equals(Ljava/lang/Object;)Z` methods
- "prev" for "previous"

Treat acronyms as single words rather than capitalizing every letter. This improves readability (compare `JsonObject`
and
`JSONObject`) and it's consistent with Mojang naming (a known name is `NbtIo`).

Abbreviations you shouldn't use are:

- "loc" for "location"

Please avoid the use of abbreviations in javadocs, except if those describe the name of a format, a library, etc.

### Packages

Package names should always be singular to respect Java conventions. The package structure is as follows:

```
org.quiltmc.qsl.<module_name (separate spaces with dots)>.
 |- api - API package
 |   |- client - Client-related package
 |   |- server - Dedicated-related package
 |   \- At the root of the API package is where common APIs reside.
 |- impl - Implementation package, every classes in it should be annotated with `@ApiStatus.Internal`
 |   |- client - Client-related package
 |   |- server - Dedicated-related package
 |   \- At the root of the implementation package is where common implementations reside.
 \- mixin - Mixin package
     |- client - Client-related package
     |- server - Dedicated-related package
     \- At the root of the mixin package is where common mixins reside.
```

This is a general structure, sub-packages can be made if needed.

### Consistency

Consistency is important as it makes code more readable and names easier to memorize. When possible, use terms that are
present in QSL, in other Quilt Mappings names, in libraries used by Minecraft, or in vanilla strings. The rest of this
section lists common names and name patterns you should use.

#### Ticks and updates

Use "tick" for updates done once per tick. Use "update" for other kind of updates.

#### Value last tick

Use the word "last" for the value that something had last tick (`lastX`, `lastWidth`, etc.).

#### Getters, setters, withers, and creators

Use "get" for non-boolean getters and other methods that calculate some property with no side effects other than caching
a value in a private field. For boolean getters, use "is".

Use "set" for methods that set some property. Name the parameter the same as the property (`setColor(color)`, not
`setColor(newColor)`).

Use "with" for methods that return a copy of an object with a different value for some property. Name the parameter the
same as the property.

Use "create" for methods that create a new instance of some object. Use "get or create" for methods that create a new
instance only if one does not already exist. Don't use "get or create" for lazy initialization, though.

#### Serialization

Use "serializer" for objects whose purpose is serializing or deserializing some type of object (`RecipeSerializer`).
Use "serialize" and "deserialize" for methods only when serializing or deserializing an object other than the one the
method is in.

Use "from" for static methods that create an object of the method owner's type (`fromJson`, `fromNbt`, `fromString`).
Use "to" for methods that convert an object to another type (`toString`, `toLong`, `toNbt`).

Use "read" for non-static methods that load data into the object. Use "write" for methods that save data to an *
existing* object passed as a parameter.

#### Factories and builders

Use "factory" for objects whose purpose is creating other objects.

Use "builder" for objects whose purpose is helping with the creation of an immutable object. Name builder methods the
same as the field they're setting, without any prefix.

#### Collections

Use a plural name for collections and maps rather than the words "list", "set", "array", etc., unless it's a collection
of collections or there are several collections of different types containing the same objects (`entities`
, `entityLists`).

When it's enough, name maps based on the value type. Otherwise, name it in the "`valuesByKeys`" format.

#### Coordinates

Coordinates can be named `x`, `y`, and `z` when it's clear what they represent. If clarification is needed, add a word
in front of the coordinate (`velocityX`, not `xVelocity`).

Name screen coordinates `x` and `y`, rather than `left` and `top`.

### Javadocs

Write sentences for class, method and field javadocs, starting with an uppercase and ending with a period. Start method
docs with verbs, like `Gets` or `Called`. Use HTML tags such as `<p>` between paragraphs if the docs have several, as
line wraps are converted to spaces in the generated documentation. Feel free to start a new line whenever you feel the
current line is too long.

Parameter and `@return` documentation should use quick descriptions without initial capitalization or punctuation, such
as `{@code true} if the block placement was successful, {@code false} otherwise`.
`{@return}` used in the first sentence can duplicate enclosed text to the return description.

Use `{@index}` to allow enclosed text to be indexed by the Javadoc search.

Javadoc will take the first sentence, ended by the first `.`, as a brief description of the member you are documenting.
Note that `.` from abbreviations, such as `i.e.`, count.

### Use of the `$` character

The `$` character may be used in mixins to mark a semantic separation in the name, in other words it allows to separate
the actual name of the variable and the namespace.

All `@Unique` mixin fields and methods and all methods coming from duck interfaces must be prefixed with `quilt$` in order to aid with debugging as well as preventing issues.

In the case of a pseudo-local variable (a field used briefly to pass around a local variable of a method between 2
injections of said method), the field should be named with the namespace first, then the name of the injected method,
and finally the name of the local (`quilt$injectedMethod$localName`).

## Gradle Conventions

### Declaring dependencies between modules

In the `qslModule` extension, there is a `moduleDependencies` field. Dependencies are declared in a tree like structure reflecting how QSL libraries and modules are laid out.
This field can be configured like:
```groovy
qslModule {
    // ...
    moduleDependencies {
        // The QSL Library to depend on
        library_name {
            // API dependencies are put on the classpath of mods that depend on this module.
            // Use an API dependency when you expose a class from this dependency in this module's
            // public API--for example, a method that returns a `FooBar`, defined in `module_name`.
            // When in doubt, use an API dependency.
            api("module_name")

            // Impl dependencies are dependencies that are only used internally in a module,
            // and classes from it are never exposed through this module's public API.
            // For example, a dependency on lifecycle events would usually be impl.
            impl("module_2")

            // This module is only depended on when testing the code
            testmodOnly("module_3")

            // This module is only used to compile the module. It is an optional runtime dependency,
            // and should be also have a testmodOnly dependency as well to test features with and without the dependency
            compileOnly("module_4")
            // testmodOnly disabled to test module without optional dependency
            // testmodOnly("module_4")
        }
    }
}
```


## Licensing & DCO

QSL is licensed under [Apache 2.0][LICENSE], and have a [Developer Certificate of Origin (DCO)][DCO]
which you need to agree with to contribute. Commit author may be sufficient,
but [a sign-off can be added too](https://git-scm.com/docs/git-commit#Documentation/git-commit.txt--s), and legal names
are not required for privacy reasons.

### License Headers

Every Java source file in QSL has a license header with a copyright notice that keeps track of its creation date and
last modification date.

Two gradle tasks are dedicated to them:

- `:checkLicenses` will check the presence and validity of the license header in each Java source file;
- `:applyLicenses` will automatically apply the correct license header to every Java source file; it can be used to add
  missing headers and update copyright dates across the whole code base.

So, before committing, remember to run `:applyLicenses`!

#### Derivative Work

Please refer to the [DCO].

##### My work comes from Fabric API

If your work comes from Fabric API, then you should put `/// FABRIC` at the beginning of your file, or in the case
you're copying an entire file in it, you can just keep the same header without modification.

Make sure to execute the `:applyLicenses` task as it will apply a special license header to Fabric API-derived files
mentioning Fabric's copyright notice.
**Files derived from Fabric must use this license header!**

[LICENSE]: ./LICENSE "Quilt Standard Libraries license file"

[DCO]: ./DEVELOPER_CERTIFICATE_OF_ORIGIN.md "Developer Certificate of Origin file"
