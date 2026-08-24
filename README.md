# Continuum Mantle

> Shared library code for the NeoForge 26.1 Tinkers' Construct port.

**Continuum Mantle** is the working name for this community-maintained NeoForge 26.1 port of **Mantle**, the shared library used by Tinkers' Construct and related SlimeKnights projects.

This is not an official SlimeKnights release. The original Mantle project, source code, assets, design, and license remain credited to SlimeKnights. The `mantle` mod id is intentionally preserved so dependent mods can continue to load against the library.

## Port target

| Component | Version |
|---|---|
| Minecraft | 26.1.2 |
| NeoForge | 26.1.2.78 |
| Java | 25 |
| Gradle | 9.1 |

## Current support

This port focuses on the library surface needed by the current NeoForge 26.1 Tinkers' Construct port:

- Mantle registration helpers and object wrappers.
- Data generation helpers used by TCon resources, recipes, tags, and loot.
- Fluid, transfer, inventory, and recipe utility classes needed by the port.
- Client and model helpers that have been updated for the current NeoForge/Minecraft APIs.
- JEI compile/runtime support for development where the current port still uses Mantle-side integration points.

Historical integrations or APIs are only considered supported when they are used by the NeoForge 26.1 port and have been tested against current dependencies.

## Building from source

Requirements:

- Git available on the system `PATH`.
- JDK 25.
- A working internet connection for Gradle dependencies and Minecraft/NeoForge artifacts.

From the Mantle repository root, run:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat processResources
```

To start a development client:

```powershell
.\gradlew.bat runClient
```

To generate data:

```powershell
.\gradlew.bat runData
```

Build artifacts are written under `build/libs`. Generated resources are written under `src/generated`. Do not edit generated files manually; update the corresponding data provider or source resource and run datagen again.

## Issue reporting

Please include the following information:

- Minecraft version: `26.1.2`.
- NeoForge version/build: `26.1.2.78`.
- Mantle port version or commit.
- Versions of dependent mods, especially the Tinkers' Construct port.
- Versions of other mods that may be related to the issue.
- Exact steps to reproduce the problem.
- Relevant screenshots or video.
- For crashes or runtime errors, attach the relevant `latest.log`, `debug.log`, or crash report.

Please mention whether the issue happens with this NeoForge 26.1 port only, or also happens in an official upstream Mantle build.

## Documentation

For original Mantle source and official upstream releases, see the [SlimeKnights Mantle repository](https://github.com/SlimeKnights/Mantle).

For Tinkers' Construct documentation and addon/datapack references, see the [SlimeKnights documentation](https://slimeknights.github.io/docs/).

## Credits and license

Mantle is an original project by [SlimeKnights](https://github.com/SlimeKnights).

This NeoForge 26.1 community port is maintained by **justduck**.

The MIT License (MIT)
Copyright (c) 2013-2022 Slime Knights (mDiyo, fuj1n, Sunstrike, progwml6, pillbox, alexbegt, KnightMiner)

Code, textures, binaries, and documentation are licensed under the [MIT License](LICENSE), unless a different license is noted in the relevant file or asset. The copyright notice and license text must be included in all copies or substantial portions of the software.

Any alternate licenses are noted where appropriate.
