# FTBLoader (Fabric Mod)

[![Build](https://github.com/Jeff53978/FTBLoader/actions/workflows/build.yml/badge.svg)](https://github.com/Jeff53978/FTBLoader/actions/workflows/build.yml)
[![Release](https://github.com/Jeff53978/FTBLoader/actions/workflows/release.yml/badge.svg)](https://github.com/Jeff53978/FTBLoader/actions/workflows/release.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Minecraft 1.21.1](https://img.shields.io/badge/minecraft-1.21.1-62B47D.svg)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/modloader-Fabric-4C3C3C.svg)](https://fabricmc.net/)

## Overview

**FTBLoader** is a lightweight Fabric mod that automatically checks for and downloads missing FTB (Feed The Beast) mods. It displays a convenient GUI on the Minecraft title screen if any expected mods are missing, allowing players to download them with a single click.

### Features

- ✅ Automatic detection of missing FTB mods (FTB Library, FTB Chunks, FTB Teams)
- ✅ One-click mod download and installation
- ✅ Configurable mod versions and download links
- ✅ Cloth Config integration for in-game settings
- ✅ ModMenu support for easy config access
- ✅ Auto-restart after download
- ✅ Built on Fabric (supports Minecraft 1.21.1)

### Supported Mods

FTBLoader checks for and can download:
- **FTB Library** — Core library for FTB ecosystem
- **FTB Chunks** (formerly Chunks/Claims) — Chunk claiming and protection
- **FTB Teams** — Team management and collaboration

## Installation

1. Download the latest `.jar` from [Releases](https://github.com/Jeff53978/FTBLoader/releases).
2. Place it in your Minecraft `mods` folder.
3. Launch the game. If any FTB mods are missing, a download screen will appear.

## Configuration

FTBLoader stores configuration in `config/ftbloader.toml`. You can enable/disable checks for individual mods and specify custom download URLs:

```toml
library = true                                  # Check for FTB Library
claims = true                                   # Check for FTB Chunks
teams = true                                    # Check for FTB Teams

[versions]
libraryVersion = "ftb-library-fabric-2101.1.28.jar"
claimsVersion = "ftb-chunks-fabric-2101.1.13.jar"
teamsVersion = "ftb-teams-fabric-2101.1.7.jar"
```

You can also edit these settings in-game via ModMenu if installed.

## Development

### Requirements

- **Java 21** (required by Fabric Loom)
- **Git**

### Building

Clone the repo and build:

```bash
git clone https://github.com/Jeff53978/FTBLoader.git
cd FTBLoader/FBTLoader
```

On Unix/macOS:
```bash
./gradlew clean build
```

On Windows (PowerShell):
```powershell
.\gradlew.bat clean build
```

Built jars are placed in `build/libs/`.

## Release & CI

**On push to `main`:**
- The workflow automatically builds the mod.
- A new release is created with an auto-incremented patch version (e.g., `1.0.0` → `1.0.1`).
- Built jars are attached to the release.

**On tag push (e.g., `git tag 1.0.5 && git push origin 1.0.5`):**
- The workflow builds the mod using that tag.
- A release is created with the tag name (e.g., `1.0.5`).
- Built jars are attached to the release.

### Example Workflow

```bash
# Make changes, commit, push to main → auto-creates release 1.0.1 with binaries
git add .
git commit -m "Add feature X"
git push origin main

# Later, create a stable release by tagging
git tag 1.1.0
git push origin 1.1.0  # Creates release 1.1.0 with jars attached
```

## Security Notes

- Downloads are performed from hardcoded URLs (see `ModConfig.java`).
- Verify URLs and versions before updating to ensure integrity.
- Use at your own risk in production environments.

## Contributing

Issues and PRs welcome! Please keep contributions focused and tested.

## License

MIT — see [LICENSE](LICENSE).

---

For questions or issues, open a GitHub issue or contact the maintainer
    
