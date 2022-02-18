# Contributing

This document outlines best-practices and contributing guidelines to the Quilt Standard Libraries.
## Pull Request Process
To get a pull request merged into QSL, it must get a certain number of approvals from the teams of each library the pull request targets (determined by a CODEOWNERS file), and then it will enter a Final Comment Period. If the pull request passes the final comment period without opposition, the PR will be merged. Otherwise, the PR will return to being in review. 

The exact number of reviews needed, and the length of the Final Comment Period, varies depending on the scope and complexity of the pull request. The numbers for each category are listed below.

Note: members of the @quiltmc/quilt-standard-libraries team, technical leads, and admins all contribute towards Required Approvals. Only QSL Core Library team members have push access to the QSL repository.
### `T: new API`
#### Description
For pull requests that add new APIs to QSL, defined as anything in a `$modulename.api` package or subfolders.

**Required Approvals**: 2
- At least 1 approval must come directly from each library team whos code the pull request modifies.

**Final Comment Period**: 7 days

### `T: refactor`
#### Description
For pull requests that make internal refactors and do not change any API, such as bugfixes or buildscript changes.

**Required Approvals**: 1
- At least 1 approval must come directly from each library team whos code the pull request modifies.
**Final Comment Period**: 3 days

### `T: urgent`
#### Description
For pull requests that must be merged quickly, like ports of critical core modules and game- or build-breaking bugs.

**Required Approvals**: 2
- Only members of the QSL Core Team, technical leads, and admins count for approvals in urgent PRs to prevent abuse. 

**Final Comment Period**: N/A

*This is only a summary of the process. The exact rules are defined in [RFC 39](https://github.com/QuiltMC/rfcs/blob/master/structure/0039-pr-policy.md)*

### Other
Trivial fixes that do not require review (e.g. typos) are exempt from this policy. QSL team members should double check with other members of the team on Discord before pushing a commit without going through this process.

In the event that a QSL subteam has less than two active members, a QSL Core team member may waive the requirement for that team to review a PR.
## Naming conventions

### Use of the `$` character

The `$` character may be used in mixins to mark a semantic separation in the name,
in other words it allows to separate the actual name of the variable and the namespace.

`@Unique` fields must be prefixed with `quilt$`, but `@Unique` methods don't need prefixes.

In the case of a pseudo-local variable (a field used briefly to pass around a local variable of a method between 2 injections of said method),
the field should be named with the namespace first, then the name of the injected method, and finally the name of the local (`quilt$injectedMethod$localName`).
