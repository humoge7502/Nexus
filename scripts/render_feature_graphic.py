#!/usr/bin/env python3
"""
Render the Play Store feature graphic from the app's own design tokens.

Colours are read straight out of core/designsystem/.../theme/Color.kt and the
lettering uses the faces the app bundles, so the store graphic and the shipping
app cannot drift apart. Deterministic: same tree, same PNG.
"""
import re
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
COLOR_SRC = ROOT / "core/designsystem/src/main/kotlin/com/nexus/designsystem/theme/Color.kt"
FONT_DIR = ROOT / "core/designsystem/src/main/res/font"
OUT = ROOT / "store/listing/en-US/feature_graphic.png"

W, H = 1024, 250


def tokens():
    """Pull the hex values out of the Kotlin source rather than restating them."""
    src = COLOR_SRC.read_text(encoding="utf-8")
    found = dict(re.findall(r"val\s+(\w+)\s*=\s*Color\(0xFF([0-9A-Fa-f]{6})\)", src))
    return {name: (int(v[0:2], 16), int(v[2:4], 16), int(v[4:6], 16)) for name, v in found.items()}


def font(face, size):
    return ImageFont.truetype(str(FONT_DIR / face), size)


def main():
    c = tokens()
    missing = [k for k in ("VoidBlack", "Carbon", "Signal", "SignalDeep", "Attention", "PearlWhite", "Structure") if k not in c]
    if missing:
        sys.exit(f"Color.kt does not define: {missing}")

    img = Image.new("RGB", (W, H), c["VoidBlack"])
    px = img.load()
    top, bottom = c["VoidBlack"], c["Carbon"]
    for y in range(H):
        t = y / (H - 1)
        row = tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3))
        for x in range(W):
            px[x, y] = row

    d = ImageDraw.Draw(img, "RGBA")

    # The instrument grid: hairlines the app draws behind every screen.
    for x in range(0, W, 34):
        d.line([(x, 0), (x, H)], fill=(255, 255, 255, 9), width=1)
    for y in range(0, H, 34):
        d.line([(0, y), (W, y)], fill=(255, 255, 255, 9), width=1)

    # Calibrated rule along the bottom edge, with a signal-coloured lead segment.
    base = H - 16
    d.line([(56, base), (W - 56, base)], fill=(255, 255, 255, 26), width=1)
    for i, x in enumerate(range(56, W - 56, 28)):
        tall = i % 5 == 0
        d.line([(x, base), (x, base - (9 if tall else 4))],
               fill=c["Signal"] + (120,) if tall else c["Signal"] + (40,), width=1 if not tall else 2)

    # Radar sweep — the thing the app actually does, drawn as an instrument.
    cx, cy, r = 838, 124, 92
    d.ellipse([cx - r, cy - r, cx + r, cy + r], outline=(255, 255, 255, 34), width=1)
    d.ellipse([cx - r * 0.66, cy - r * 0.66, cx + r * 0.66, cy + r * 0.66],
              outline=(255, 255, 255, 24), width=1)
    d.ellipse([cx - r * 0.33, cy - r * 0.33, cx + r * 0.33, cy + r * 0.33],
              outline=(255, 255, 255, 20), width=1)
    d.line([(cx - r, cy), (cx + r, cy)], fill=(255, 255, 255, 22), width=1)
    d.line([(cx, cy - r), (cx, cy + r)], fill=(255, 255, 255, 22), width=1)
    d.pieslice([cx - r, cy - r, cx + r, cy + r], start=-58, end=6,
               fill=c["Signal"] + (26,), outline=None)
    d.line([(cx, cy), (cx + r * 0.94, cy - r * 0.34)], fill=c["Signal"] + (150,), width=2)

    for (ox, oy, col, rad) in ((-46, -34, c["Signal"], 6), (28, 44, c["Attention"], 5),
                               (52, -22, c["Structure"], 4), (-18, 52, c["Signal"], 3)):
        d.ellipse([cx + ox - rad, cy + oy - rad, cx + ox + rad, cy + oy + rad], fill=col + (255,))
        d.ellipse([cx + ox - rad - 5, cy + oy - rad - 5, cx + ox + rad + 5, cy + oy + rad + 5],
                  outline=col + (70,), width=1)

    word = font("jetbrains_mono_variable.ttf", 76)
    tag = font("jetbrains_mono_variable.ttf", 22)
    label = font("inter_variable.ttf", 21)

    x0 = 56
    d.text((x0, 44), "SAGITTA", font=word, fill=c["PearlWhite"])
    # Tracking, because the mono face reads tight at display size.
    kern = 6
    ty = 148
    tx = x0
    for ch in "OPPORTUNITY RADAR":
        d.text((tx, ty), ch, font=tag, fill=c["Signal"])
        tx += d.textlength(ch, font=tag) + kern

    d.text((x0, 186), "Every internship and hackathon worth your time, with the deadline attached.",
           font=label, fill=(255, 255, 255, 150))

    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT, "PNG", optimize=True)
    print(f"wrote {OUT} ({img.size[0]}x{img.size[1]})")


if __name__ == "__main__":
    main()
