#!/usr/bin/env python3
"""Remap SRG member identifiers in Java source to MCP names.

The release JAR was reobfuscated by Forge, so its bytecode references SRG
members (func_/field_). A normal ForgeGradle/Loom dev workspace uses MCP
names. This script performs a lexical replacement in normal code only:
comments, string literals and character literals are left untouched.

Usage:
    python tools/remap_srg.py <src-dir> <methods.csv> <fields.csv>
"""
from __future__ import annotations

import csv
import pathlib
import sys


def load_map(path: pathlib.Path) -> dict[str, str]:
    with path.open("r", encoding="utf-8-sig", newline="") as fh:
        rows = csv.DictReader(fh)
        return {
            row["searge"].strip(): row["name"].strip()
            for row in rows
            if row.get("searge") and row.get("name")
        }


def remap_text(text: str, members: dict[str, str]) -> str:
    out: list[str] = []
    i = 0
    n = len(text)
    state = "code"  # code | line_comment | block_comment | string | char

    while i < n:
        ch = text[i]
        nxt = text[i + 1] if i + 1 < n else ""

        if state == "code":
            if ch == "/" and nxt == "/":
                state = "line_comment"
                out.append("//")
                i += 2
                continue
            if ch == "/" and nxt == "*":
                state = "block_comment"
                out.append("/*")
                i += 2
                continue
            if ch == '"':
                state = "string"
                out.append(ch)
                i += 1
                continue
            if ch == "'":
                state = "char"
                out.append(ch)
                i += 1
                continue
            if ch.isalpha() or ch in "_$":
                j = i + 1
                while j < n and (text[j].isalnum() or text[j] in "_$"):
                    j += 1
                token = text[i:j]
                out.append(members.get(token, token))
                i = j
                continue
            out.append(ch)
            i += 1
            continue

        if state == "line_comment":
            out.append(ch)
            i += 1
            if ch == "\n":
                state = "code"
            continue

        if state == "block_comment":
            if ch == "*" and nxt == "/":
                out.append("*/")
                i += 2
                state = "code"
            else:
                out.append(ch)
                i += 1
            continue

        # string / char literal
        out.append(ch)
        i += 1
        if ch == "\\" and i < n:
            out.append(text[i])
            i += 1
        elif (state == "string" and ch == '"') or (state == "char" and ch == "'"):
            state = "code"

    return "".join(out)


def main() -> int:
    if len(sys.argv) != 4:
        print(__doc__)
        return 2
    src = pathlib.Path(sys.argv[1])
    members = load_map(pathlib.Path(sys.argv[2]))
    members.update(load_map(pathlib.Path(sys.argv[3])))
    changed = 0
    files = 0
    for path in src.rglob("*.java"):
        original = path.read_text(encoding="utf-8")
        result = remap_text(original, members)
        files += 1
        if result != original:
            path.write_text(result, encoding="utf-8", newline="\n")
            changed += 1
    print(f"scanned={files} changed={changed} mappings={len(members)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
