<p align="center">
  <strong>日本語</strong> ・
  <a href="README-zh_CN.md">简体中文</a> ・
  <a href="README-en_US.md">English</a>
</p>

# Meowtils 2.0.1

Minecraft 1.8.9 / Forge 11.15.1.2318 向けのクライアントユーティリティ Mod です。

**Base version:** `Meowtils-2.0.1`（リリース JAR: `Meowtils-2.0.1.jar` / SHA-256: `BD79ABF372BC251F6AE461F690E89968799F55C2AA7A2B6B99E56D5D4E672916`）

配布されている **Meowtils 2.0.1** の JAR を復元し、Gradle でビルドできるように整理した非公式ソースです。原作者とは関係ありません。

> `LICENSE` の条件を確認してください。完全なソースの公開・再配布には、原作者の許諾が必要な場合があります。

## 変更点

- 2.0.1 JAR のクラスとリソースを復元
- SRG 名を MCP stable 22 に変換
- Gradle 8.8 / Loom のビルド環境と Wrapper を追加
- 逆コンパイル由来のコンパイルエラーを修正

問題や提案は [Issue](../../issues/new) で報告してください。

## ビルド

```bash
./gradlew build
```

Windows:

```bat
gradlew.bat build
```

生成物:

```text
build/libs/Meowtils-2.0.1.jar
```

初回ビルド時は依存関係のダウンロードにネットワーク接続が必要です。

## コントリビュート

Issue や Pull Request を歓迎します。

## クレジット

- Original: [femboytatp/meowtils](https://github.com/femboytatp/meowtils)
- Embedded agent: [curxxed-mc/lunar-agent](https://github.com/curxxed-mc/lunar-agent)
