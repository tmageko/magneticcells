# Magnetic Cells

Magnetic Cells adds portable Applied Energistics 2 storage cells with a built-in item magnet. The magnet collects nearby dropped items into a cell carried in the player's inventory.

## Supported versions

| Directory | Minecraft | Mod loader | Java |
| --- | --- | --- | --- |
| [`1.20.1-forge`](1.20.1-forge) | 1.20.1 | Forge | 17 |
| [`1.21.1-neoforge`](1.21.1-neoforge) | 1.21.1 | NeoForge | 21 |

Each directory is a standalone Gradle project with its own wrapper settings and source code.

## Features

- Ten storage tiers, from 1K to 256M items
- Built-in magnet that stores nearby dropped items in the cell
- Whitelist and blacklist item filters
- Forge Energy support
- Compatible with ME Drives
- MEGA Cells integration for tiers above 64K

## Build

Install the Java version listed for the Minecraft version, then run the Gradle wrapper from that version's directory:

```bash
cd 1.20.1-forge
./gradlew build
```

On Windows, use `gradlew.bat build`. For Minecraft 1.21.1, run the same commands from `1.21.1-neoforge` with Java 21 installed.

The built mod JAR is written to that project's `build/libs` directory.

## Dependencies

Applied Energistics 2 is required. MEGA Cells is optional and required only for the 1M–256M storage tiers.

## Links

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/magnetic-cells)
- [Source code](https://github.com/tmageko/magneticcells)

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
