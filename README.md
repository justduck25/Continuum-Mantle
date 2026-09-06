# Continuum Core

> Shared library code for the NeoForge 26.1 Continuum Construct port.

**Continuum Core** is a community-maintained NeoForge 26.1 fork of the original **Mantle** library. It provides the shared APIs, data helpers, recipe utilities, model helpers, and compatibility glue required by Continuum Construct and related ports.

This is not an official SlimeKnights release. The original Mantle project, source code, assets, design, and license remain credited to SlimeKnights. The technical `mantle` mod id is intentionally preserved so dependent mods can continue to load against the library, while the public fork branding has been renamed to Continuum Core.

## Port target

| Component | Version |
|---|---|
| Minecraft | 26.1.2 |
| NeoForge | 26.1.2.78+ |
| Java | 25 |
| Gradle | 9.1 |

## Current support

This port focuses on the library surface needed by the current NeoForge 26.1 Continuum Construct port:

- Registration helpers and object wrappers used by the port.
- Data generation helpers for resources, recipes, tags, and loot.
- Fluid, transfer, inventory, and recipe utility classes.
- Client, model, tooltip, and screen helpers updated for current NeoForge/Minecraft APIs.
- Recipe decode and sync safety fixes for modern NeoForge servers and large modpacks.
- JEI compile/runtime support for development where the port still uses library-side integration points.

Historical integrations or APIs are supported only when they are used by the NeoForge 26.1 port and have been tested against current dependencies.

## Building from source

Requirements:

- Git available on the system `PATH`.
- JDK 25.
- A working internet connection for Gradle dependencies and Minecraft/NeoForge artifacts.

From the repository root, run:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat assemble
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

Please include:

- Minecraft version: `26.1.2`.
- NeoForge version/build.
- Continuum Core version or commit.
- Versions of dependent mods, especially Continuum Construct.
- Versions of other mods that may be related to the issue.
- Exact steps to reproduce the problem.
- Relevant screenshots or video.
- For crashes or runtime errors, attach `latest.log`, `debug.log`, or the crash report.

Please mention whether the issue happens with this NeoForge 26.1 fork only, or also happens in an official upstream Mantle build.

## Documentation

For original Mantle source and official upstream releases, see the [SlimeKnights Mantle repository](https://github.com/SlimeKnights/Mantle).

For Tinkers' Construct documentation and addon/datapack references, see the [SlimeKnights documentation](https://slimeknights.github.io/docs/).

## Credits and license

Mantle is an original project by [SlimeKnights](https://github.com/SlimeKnights).

This NeoForge 26.1 community fork is maintained by **justduck** under the public name **Continuum Core**.

The MIT License (MIT)
Copyright (c) 2013-2022 Slime Knights (mDiyo, fuj1n, Sunstrike, progwml6, pillbox, alexbegt, KnightMiner)

Code, textures, binaries, and documentation are licensed under the [MIT License](LICENSE), unless a different license is noted in the relevant file or asset. The copyright notice and license text must be included in all copies or substantial portions of the software.

Any alternate licenses are noted where appropriate.
