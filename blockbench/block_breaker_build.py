#!/usr/bin/env python3
"""Builds the Block Breaker's game assets from the Blockbench export.

Run from the repository root after re-exporting block_breaker.json or editing
block_breaker.png:

    python3 blockbench/block_breaker_build.py

It writes:
  models/block/block_breaker_core.json            the "core" group (the translucent cube and its
                                                   frame), drawn always
  models/block/block_breaker_<side>_input.json    a side's connection (from the "connections"
  models/block/block_breaker_<side>_output.json   group), drawn when that side is an input or an
                                                   output, as the Omnidirectional Hopper's are
  textures/block/block_breaker_input.png          the texture with every white pixel blue
  textures/block/block_breaker_output.png         the texture with every white pixel green

Needs Pillow (pip3 install pillow).
"""
import json
import os

from PIL import Image

ASSETS = "common/src/main/resources/assets/mobfarmingsupplies"
SOURCE_MODEL = f"{ASSETS}/models/block/block_breaker.json"
SOURCE_TEXTURE = f"{ASSETS}/textures/block/block_breaker.png"
TEXTURE_ID = "mobfarmingsupplies:block/block_breaker"

SIDES = ("north", "south", "east", "west", "up", "down")

# The connection colours, the Omnidirectional Hopper's (HopperSide's colours).
MODES = {"input": (60, 110, 230, 255), "output": (60, 180, 60, 255)}

# Block display transforms, so the core also serves as the item's model. The GUI view is 1.5
# times a block's, as the other hoppers' are: the core is only half a block across.
DISPLAY = {
    "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.9375, 0.9375, 0.9375]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
    "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
    "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
    "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]},
    "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]},
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


def write(model, name, elements, texture_id):
    part = {
        "parent": "minecraft:block/block",
        "texture_size": model.get("texture_size", [16, 16]),
        "textures": {"0": texture_id, "particle": TEXTURE_ID},
        "elements": elements,
        "display": DISPLAY,
    }
    path = f"{ASSETS}/models/block/block_breaker_{name}.json"
    with open(path, "w") as f:
        json.dump(part, f, indent="\t")
        f.write("\n")
    print(f"{path}: {len(elements)} elements")


def group_elements(model, name):
    group = find_group(model["groups"], name)
    if group is None:
        raise SystemExit(f'No "{name}" group in {SOURCE_MODEL}')
    return [model["elements"][i] for i in sorted(element_indices(group))]


def write_textures():
    source = Image.open(SOURCE_TEXTURE).convert("RGBA")
    for mode, colour in MODES.items():
        out = source.copy()
        whites = 0
        for y in range(out.height):
            for x in range(out.width):
                if out.getpixel((x, y)) == (255, 255, 255, 255):
                    out.putpixel((x, y), colour)
                    whites += 1
        path = f"{ASSETS}/textures/block/block_breaker_{mode}.png"
        out.save(path)
        print(f"{path}: {whites} white pixels recoloured")


def main():
    if not os.path.exists(SOURCE_MODEL):
        raise SystemExit("Run this from the repository root.")
    with open(SOURCE_MODEL) as f:
        model = json.load(f)
    write(model, "core", group_elements(model, "core"), TEXTURE_ID)
    for side in SIDES:
        elements = group_elements(model, side)
        for mode in MODES:
            write(model, f"{side}_{mode}", elements, f"{TEXTURE_ID}_{mode}")
    write_textures()


if __name__ == "__main__":
    main()
