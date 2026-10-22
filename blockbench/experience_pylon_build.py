#!/usr/bin/env python3
"""Builds the Experience Pylon's game assets from the Blockbench export.

Run from the repository root after re-exporting experience_pylon.json or editing
experience_pylon_texture.png:

    python3 blockbench/experience_pylon_build.py

It writes:
  models/block/experience_pylon_base.json   the "base" group, the block's own model
  models/block/experience_pylon_pylon.json  the "pylon" group, drawn bobbing by the renderer
  textures/block/experience_pylon.png       the texture with every white pixel replaced by
                                            flowing XP Juice, one frame per juice frame
  textures/block/experience_pylon.png.mcmeta  the juice's animation timing

Needs Pillow (pip3 install pillow).
"""
import json
import os

from PIL import Image

ASSETS = "common/src/main/resources/assets/mobfarmingsupplies"
SOURCE_MODEL = f"{ASSETS}/models/block/experience_pylon.json"
SOURCE_TEXTURE = f"{ASSETS}/textures/block/experience_pylon_texture.png"
JUICE = f"{ASSETS}/textures/block/xp_juice_flow.png"
OUT_TEXTURE = f"{ASSETS}/textures/block/experience_pylon.png"
TEXTURE_ID = "mobfarmingsupplies:block/experience_pylon"

# The juice texture is blue; the game tints XP Juice neon green when it draws the fluid
# (XpJuiceClientEvents / FabricXpJuiceFluid), so the same tint is applied here.
JUICE_TINT = (0x39, 0xFF, 0x14)
# Juice pixels are 75% opaque, so the crystal looks glassy.
JUICE_ALPHA = 191

# Both parts are also the item's model, so they carry block display transforms. The pylon
# reaches 27 pixels up, so the GUI view is shrunk and lowered to fit a slot.
DISPLAY = {
    "gui": {"rotation": [30, 225, 0], "translation": [0, -3, 0], "scale": [0.45, 0.45, 0.45]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.2, 0.2, 0.2]},
    "fixed": {"rotation": [0, 0, 0], "translation": [0, -2.5, 0], "scale": [0.4, 0.4, 0.4]},
    "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 1, 0], "scale": [0.3, 0.3, 0.3]},
    "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, -1, 0], "scale": [0.3, 0.3, 0.3]},
    "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, -1, 0], "scale": [0.3, 0.3, 0.3]},
}


def element_indices(group):
    """Every element index under a Blockbench outliner group, however deeply nested."""
    out = []
    for child in group["children"]:
        if isinstance(child, int):
            out.append(child)
        else:
            out.extend(element_indices(child))
    return out


def find_group(groups, name):
    for group in groups:
        if isinstance(group, int):
            continue
        if group["name"] == name:
            return group
        found = find_group(group["children"], name)
        if found:
            return found
    return None


def write_part(model, name):
    group = find_group(model["groups"], name)
    if group is None:
        raise SystemExit(f'No "{name}" group in {SOURCE_MODEL}')
    elements = [model["elements"][i] for i in sorted(element_indices(group))]
    part = {
        "parent": "minecraft:block/block",
        "render_type": "minecraft:translucent",
        "texture_size": model.get("texture_size", [16, 16]),
        "textures": {"0": TEXTURE_ID, "particle": TEXTURE_ID},
        "elements": elements,
        "display": DISPLAY,
    }
    path = f"{ASSETS}/models/block/experience_pylon_{name}.json"
    with open(path, "w") as f:
        json.dump(part, f, indent="\t")
        f.write("\n")
    print(f"{path}: {len(elements)} elements")


def write_texture():
    source = Image.open(SOURCE_TEXTURE).convert("RGBA")
    juice = Image.open(JUICE).convert("RGBA")
    tile = juice.width
    frames = juice.height // tile
    width, height = source.size
    out = Image.new("RGBA", (width, height * frames))
    mask = [(x, y) for y in range(height) for x in range(width)
            if source.getpixel((x, y)) == (255, 255, 255, 255)]
    for frame in range(frames):
        out.paste(source, (0, frame * height))
        for x, y in mask:
            r, g, b, _ = juice.getpixel((x % tile, frame * tile + y % tile))
            tinted = tuple(c * t // 255 for c, t in zip((r, g, b), JUICE_TINT))
            out.putpixel((x, frame * height + y), tinted + (JUICE_ALPHA,))
    out.save(OUT_TEXTURE)
    with open(JUICE + ".mcmeta") as f:
        animation = json.load(f)
    with open(OUT_TEXTURE + ".mcmeta", "w") as f:
        json.dump(animation, f, indent=2)
        f.write("\n")
    print(f"{OUT_TEXTURE}: {len(mask)} juice pixels, {frames} frames")


def main():
    if not os.path.exists(SOURCE_MODEL):
        raise SystemExit("Run this from the repository root.")
    with open(SOURCE_MODEL) as f:
        model = json.load(f)
    write_part(model, "base")
    write_part(model, "pylon")
    write_texture()


if __name__ == "__main__":
    main()
