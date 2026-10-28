<p align="center">
  <a href="README.md">日本語</a> ・
  <strong>简体中文</strong> ・
  <a href="README-en_US.md">English</a>
</p>

# Meowtils 2.0.1

Minecraft 1.8.9 / Forge 11.15.1.2318 客户端工具 Mod。

**基础版本：** `Meowtils-2.0.1`（发布 JAR：`Meowtils-2.0.1.jar`，SHA-256：`BD79ABF372BC251F6AE461F690E89968799F55C2AA7A2B6B99E56D5D4E672916`）

本项目基于已发布的 **Meowtils 2.0.1** JAR 还原，并整理为可用 Gradle 构建的非官方源码。与原作者无关。

> 请阅读 `LICENSE`。公开或再分发完整源码前，可能需要取得原作者授权。

## 变更

- 还原 2.0.1 JAR 的类与资源
- 将 SRG 名称转换为 MCP stable 22
- 添加 Gradle 8.8 / Loom 构建环境和 Wrapper
- 修复反编译产生的编译错误

遇到问题或有功能建议，请[提交 Issue](../../issues/new)。

## 构建

```bash
./gradlew build
```

Windows：

```bat
gradlew.bat build
```

产物：

```text
build/libs/Meowtils-2.0.1.jar
```

首次构建需要联网下载依赖。

## 贡献

欢迎提交 Issue 或 Pull Request。

## 致谢

- 原项目：[femboytatp/meowtils](https://github.com/femboytatp/meowtils)
- 内嵌 agent：[curxxed-mc/lunar-agent](https://github.com/curxxed-mc/lunar-agent)
