# Quilt Testing API

This module is a set of testing utilities to allow modders to test the game and their mods
to ensure that everything is working up to specs.

## System properties

| Property                  |  Type   |          Default           | Description                                                                                                                                                       |
|:--------------------------|:-------:|:--------------------------:|:------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `quilt.game_test`         | boolean |          `false`           | Defines if game tests are enabled or not. On a dedicated server this cancels the launch and launch a test-only server that shutdowns once the tests are executed. |
| `quilt.game_test.command` | boolean | Value of `quilt.game_test` | Defines whether the `/test` command should be enabled or not.                                                                                                     |
