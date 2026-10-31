# Magnetic Cells

Portable Applied Energistics 2 storage cells with a built-in item magnet. The magnet collects nearby dropped items into a cell carried in the player's inventory.

## Version branches

| Branch | Minecraft | Mod loader | Java |
| --- | --- | --- | --- |
| [`main`](https://github.com/tmageko/magneticcells/tree/main) | 1.21.1 | NeoForge | 21 |
| [`forge-1.20`](https://github.com/tmageko/magneticcells/tree/forge-1.20) | 1.20.1 | Forge | 17 |

Each branch contains a standalone Gradle project at its repository root.

## Features

- Ten storage tiers, from 1K to 256M items
- Built-in magnet that stores nearby dropped items in the cell
- Whitelist and blacklist item filters
- Forge Energy support
- Compatible with ME Drives
- MEGA Cells integration for tiers above 64K

## Build

Install the Java version listed for the selected branch, then run the Gradle wrapper from the repository root:

```bash
./gradlew build
```

On Windows, run `gradlew.bat build`. The built mod JAR is written to `build/libs`.

## Dependencies

Applied Energistics 2 is required. MEGA Cells is optional and required only for the 1M–256M storage tiers.

## Links

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/magnetic-cells)
- [Source code](https://github.com/tmageko/magneticcells)

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
