# Continuum Core

[![Minecraft](https://img.shields.io/badge/Minecraft-26.1.2-blue.svg)](https://minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-26.1.2.106+-orange.svg)](https://neoforged.net/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

> Shared foundational library for **Continuum Construct** on Minecraft 26.1 / NeoForge.

**Continuum Core** is a community-maintained NeoForge 26.1 fork of the classic **Mantle** library by SlimeKnights. It delivers the core APIs, custom model infrastructure, fluid mechanics, modular in-game book engine, data generation helpers, and registration utilities required by Continuum Construct and affiliated addon ports.

> [!NOTE]
> **Mod ID & Compatibility:** The technical mod ID `mantle` and Java package `slimeknights.mantle` are intentionally retained for binary, save-game, and data pack compatibility. The public branding is **Continuum Core**. This is a community fork and not an official SlimeKnights release.

---

## 🎯 Port Target & Compatibility Matrix

| Component | Target Version | Supported Range |
|---|---|---|
| **Minecraft** | `26.1.2` | `[26.1.2, 26.2)` |
| **NeoForge** | `26.1.2.106` | `[26.1.2.78, 26.2)` |
| **Java Toolchain** | Java 25 | JDK 21+ required to run Gradle |
| **Gradle** | `9.1` | Wrapper included (`gradlew.bat`) |
| **Continuum Core Version** | `1.12.3` | Artifact: `ContinuumCore-26.1.2-1.12.3.jar` |

---

## ⚙️ Core Architecture & Features

### 1. Advanced Model Loaders
Ported to modern Minecraft 26.1 unbaked geometry standards (`AbstractUnbakedModel` + `ExtendedUnbakedGeometry` with `CuboidModelElement` deserialization):
* **`item_layer`** (`MantleItemLayerModel` / `MantleItemLayerGeometry`): Multi-pass tinted quad generation for complex layered item sprites.
* **`retextured`** (`RetexturedModel`): Dynamic block model texture swapping and retextured metadata.
* **`connected`** (`ConnectedModel`): Connection textures, dynamic face predicates, and border culling.
* **`colored_block`** (`ColoredBlockModel`): Per-element color mapping (`ColorData`) without full model duplicates.
* **`nbt_key`** (`NBTKeyModel`): Dynamic texture selection driven by item/block NBT data keys.

### 2. Modular In-Game Book Engine (`slimeknights.mantle.client.book`)
* Full framework for interactive guidebooks with custom page layouts, section indices, and styling.
* **3D Isometric Structure Preview** (`StructurePreviewRenderer`): Real-time isometric projection preview with depth sorting (painter's algorithm) and height-based lighting.
* **Performance Optimizations**: Asynchronous lazy-loading of structure templates and book pages to prevent client thread stutters when browsing catalogs.
* Multi-language localization support with complete upstream synchronization.

### 3. Fluid & Inventory Infrastructure
* **`FluidRenderer`**: Built for Minecraft 26.1's modern Blaze3D pipeline, providing cuboid fluid rendering and camera submersion quads (`RenderTypes.entityTranslucent`).
* Transfer and fluid handling utilities aligned with NeoForge capability patterns.
* Accurate fluid unit formatting, tooltips, and container interaction helpers.

### 4. Registration & Data Loading
* Modern `RecordLoadable` and codec infrastructure for robust, crash-resilient JSON serialization and network synchronization.
* Safe registry wrappers and lifecycle handlers built on NeoForge `DeferredRegister` / `DeferredHolder`.
* Full Data Provider framework for recipes, tags, loot tables, and client assets.

### 5. Third-Party Integrations
* **JEI**: Custom entity ingredient rendering (`EntityIngredientRenderer`) utilizing `EntityRenderDispatcher` and `GuiGraphicsExtractor`.

---

## 🏗️ Building from Source

### Prerequisites
* **Git** installed and available on system `PATH`.
* **JDK 21** or higher installed (Gradle will auto-provision Java 25 via toolchain if needed).
* Active internet connection for fetching dependencies and Minecraft/NeoForge mappings.

### Build Commands

From the `Mantle` directory:

```powershell
# Compile Java source code
.\gradlew.bat compileJava

# Build and assemble the release JAR
.\gradlew.bat assemble

# Run data generation
.\gradlew.bat runData

# Launch the development client
.\gradlew.bat runClient
```

The resulting library JAR will be generated under:
```
build/libs/ContinuumCore-${minecraft_version}-${mantle_version}.jar
```

> [!IMPORTANT]
> **Consuming in Continuum Construct:** When modifying Core, always run `.\gradlew.bat assemble` in `Mantle` before compiling `Tcon4`. Continuum Construct consumes this exact built JAR from `../Mantle/build/libs/`.

---

## 🐛 Issue Reporting & Feedback

When reporting issues or bugs, please provide:
1. **Minecraft & NeoForge Versions**: Exact build numbers (e.g., Minecraft `26.1.2`, NeoForge `26.1.2.106`).
2. **Mod Versions**: Specific Continuum Core and Continuum Construct build/commit IDs.
3. **Environment**: Client (singleplayer), LAN, or Dedicated Server.
4. **Logs**: Complete `logs/latest.log` or crash report (`crash-reports/`).
5. **Reproduction Steps**: Step-by-step instructions to reproduce the issue, along with any relevant screenshots.

---

## 📜 Credits and License

* **Original Project**: Mantle is an original project created and designed by [SlimeKnights](https://github.com/SlimeKnights) (mDiyo, fuj1n, Sunstrike, progwml6, pillbox, alexbegt, KnightMiner).
* **Port Maintainer**: Maintained for NeoForge 26.1 by **justduck** under the public name **Continuum Core**.
* **License**: Code, textures, and assets are licensed under the [MIT License](LICENSE).
