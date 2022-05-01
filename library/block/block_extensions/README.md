# Quilt Block Extensions

This module provides extensions for creating and working with blocks.

## Access Widener

This module contains an auto-generated access widener to transitively access-widen
block constructors.

The non-auto-generated part of this access widener can be found in `template.accesswidener`
which should be modified instead of directly modifying the generated access widener as it would get overwritten.
The auto-generated part is done through the `build.gradle` of this module.
