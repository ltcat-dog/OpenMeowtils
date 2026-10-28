<p align="center">
  <a href="README.md">日本語</a> ・
  <a href="README-zh_CN.md">简体中文</a> ・
  <strong>English</strong>
</p>

# Meowtils 2.0.1

A Minecraft 1.8.9 / Forge 11.15.1.2318 client utility mod.

**Based on:** `Meowtils-2.0.1` (release JAR: `Meowtils-2.0.1.jar`, SHA-256: `BD79ABF372BC251F6AE461F690E89968799F55C2AA7A2B6B99E56D5D4E672916`)

This repository restores the published **Meowtils 2.0.1** JAR and reorganizes it into a Gradle-buildable, unofficial source tree. It is not affiliated with the original author.

> Read `LICENSE` before publishing or redistributing the complete source. Permission from the original author may be required.

## Changes

- Restored classes and resources from the 2.0.1 JAR
- Remapped SRG names to MCP stable 22
- Added a Gradle 8.8 / Loom build setup and Wrapper
- Fixed compilation errors caused by decompilation

If you encounter an issue or have a feature suggestion, please [create an issue](../../issues/new).

## Building

```bash
./gradlew build
```

Windows:

```bat
gradlew.bat build
```

Output:

```text
build/libs/Meowtils-2.0.1.jar
```

The first build requires network access to download dependencies.

## Contributing

Issues and pull requests are welcome.

## Credits

- Original: [femboytatp/meowtils](https://github.com/femboytatp/meowtils)
- Embedded agent: [curxxed-mc/lunar-agent](https://github.com/curxxed-mc/lunar-agent)
