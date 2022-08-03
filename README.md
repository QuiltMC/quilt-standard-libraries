# Quilt Standard Libraries

**Note: At the moment, the Quilt Standard Libraries are not ready for general use. Please make an issue or talk to the
QSL team on discord before writing any PRs.**

## Repository structure

The repository has 2 main parts:

- The `library` folder. This contains all the libraries that are part of the Quilt Standard Libraries.
- The `build-logic` folder. This is an included build in Gradle and contains most of the buildscript used inside the
  libraries. This keeps the buildscripts inside the `library` folder as minimal as possible; definitions of data rather
  than logic.

## Features

Here are multiple charts of features available in QSL which also serves as a comparison chart with Fabric API.

The charts are organized by QSL libraries.

### Core Library

| Feature                           | QSL |  Fabric API   |
|:----------------------------------|:---:|:-------------:|
| Auto Test Server argument         |  ✔  |       ❌       |
| Event API                         |  ✔  |       ✔       |
| Event API - Phases                |  ✔  |       ✔       |
| Event API - Events as Entrypoints |  ✔  |       ❌       |
| Gametest API                      |  ❌  |       ✔       |
| Initializer Entrypoints           |  ✔  | ✔ (in loader) |
| Networking API                    |  ✔  |       ✔       |

#### Crash Report

| Feature                        | QSL | Fabric API |
|:-------------------------------|:---:|:----------:|
| Crash report extra context     |  ✔  |     ✔      |
| Crash report extra context API |  ✔  |     ❌      |

### Core - Lifecycle Events

| Feature                    | QSL | Fabric API |
|:---------------------------|:---:|:----------:|
| Client Lifecycle Events    |  ✔  |     ✔      |
| Client Tick Events         |  ✔  |     ✔      |
| Client World Tick Events   |  ✔  |     ✔      |
| Client Block Entity Events |  ❌  |     ✔      |
| Client Chunk Entity Events |  ❌  |     ✔      |
| Client Entity Events       |  ❌  |     ✔      |
| Common Lifecycle Events    |  ❌  |     ✔      |
| Server Lifecycle Events    |  ✔  |     ✔      |
| Server Tick Events         |  ✔  |     ✔      |
| Server World Load Events   |  ✔  |     ✔      |
| Server World Tick Events   |  ✔  |     ✔      |
| Server Block Entity Events |  ❌  |     ✔      |
| Server Chunk Entity Events |  ❌  |     ✔      |
| Server Entity Events       |  ❌  |     ✔      |

### Core - Registry

| Feature                                     |      QSL       | Fabric API |
|:--------------------------------------------|:--------------:|:----------:|
| Addition Events                             |       ✔        |     ✔      |
| Addition Events Helper                      |       ✔        |     ❌      |
| Registry Syncing                            | :construction: |     ✔      |
| Registry Syncing - Exclude Specific Entries | :construction: |     ❌      |

### Core - Resource Loader

| Feature                               |      QSL       |            Fabric API            |
|:--------------------------------------|:--------------:|:--------------------------------:|
| Load mod resources.                   |       ✔        |                ✔                 |
| Resource Loader Events                |       ✔        | ✔ (in lifecycle, non equivalent) |
| Built-in resource pack API            |       ✔        |                ✔                 |
| Programmer Art API                    |       ✔        |                ✔                 |
| Group resource pack API               |       ✔        |                ❌                 |
| Resource Pack Provider API            |       ✔        |                ❌                 |
| Resource Reloaders                    |       ✔        |                ✔                 |
| Resource Reloaders - Advanced Sorting | :construction: |                ❌                 |

### Block Library

| Feature                                                 | QSL |  Fabric API   |
|:--------------------------------------------------------|:---:|:-------------:|
| Extended Block Settings                                 |  ✔  |       ✔       |
| Extended Material Builder                               |  ✔  |       ✔       |
| Block Render Layers API                                 |  ✔  |       ✔       |
| All Block Constructors Are Public                       |  ✔  |       ✔       |
| Block Entity Type registration helper                   |  ✔  |       ✔       |
| Block Entity Type post-creation supported block editing |  ✔  |       ❌       |
| Block Entity Syncing Helper                             |  ✔  |       ❌       |

### Data Library

| Feature                                                               | QSL |   Fabric API   |
|:----------------------------------------------------------------------|:---:|:--------------:|
| Advancement Criterion Registration Helper                             |  ✔  |       ✔        |
| Recipe API                                                            |  ✔  |       ❌        |
| Registry Entry Attachments                                            |  ✔  |       ❌        |
| Client-fallback/Client-only tags                                      |  ✔  | :construction: |
| Client-fallback/Client-only tags - integration within Vanilla methods |  ✔  |       ❌        |

### Entity Library

| Feature                         |      QSL       |   Fabric API   |
|:--------------------------------|:--------------:|:--------------:|
| EntityType registration helpers | :construction: |       ✔        |
| Entity Events                   | :construction: |       ✔        |
| Multipart Entity API            |       ✔        |       ❌        |

### GUI Library

| Feature                   |      QSL       | Fabric API |
|:--------------------------|:--------------:|:----------:|
| Screen API                |       ✔        |     ✔      |
| Item Tooltip Event        |       ✔        |     ✔      |
| Tooltip Component - Event |       ✔        |     ✔      |
| Key Binds API             | :construction: |     ✔      |

### Item Library

| Feature                                         | QSL | Fabric API |
|:------------------------------------------------|:---:|:----------:|
| Item Groups                                     |  ✔  |     ✔      |
| Item Settings                                   |  ✔  |     ✔      |
| Item Settings - Custom Item Setting             |  ✔  |     ❌      |
| Item Content Registry - Composter               |  ✔  |     ✔      |
| Item Content Registry - Composter (data-driven) |  ✔  |     ❌      |
| Item Content Registry - Fuel                    |  ✔  |     ✔      |
| Item Content Registry - Fuel (data-driven)      |  ✔  |     ❌      |

### Management Library

| Feature         | QSL | Fabric API |
|:----------------|:---:|:----------:|
| Commands        |  ✔  |     ✔      |
| Client Commands |  ✔  |     ✔      |
| Game Rules      |  ❌  |     ✔      |

### Worldgen Library

| Feature                 | QSL | Fabric API |
|:------------------------|:---:|:----------:|
| Biome Modifications API |  ✔  |     ✔      |
| Add Nether Biomes       |  ✔  |     ✔      |
| Add End Biomes          |  ✔  |     ✔      |
| Dimension API           |  ✔  |     ✔      |
| Surface Rule API        |  ✔  |     ❌      |
