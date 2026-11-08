#!/usr/bin/env python3
"""Builds the Upgrade Station's game models from the Blockbench export.

Run from the repository root after re-exporting upgrade_station.json:

    python3 blockbench/upgrade_station_build.py

It writes, both using textures/block/upgrade_station_texture.png:
  models/block/upgrade_station_base.json  the "base" group: the block's own model
  models/block/upgrade_station_lid.json   the "lid" group, drawn by the renderer so it can open

The lid's cubes keep their rotation origin (the hinge, y 13, z 16).
"""
import json
import os

ASSETS = "common/src/main/resources/assets/mobfarmingsupplies"
SOURCE_MODEL = f"{ASSETS}/models/block/upgrade_station.json"

# Block display transforms, so the parts also serve as the item's model.
DISPLAY = {
    "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
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


def write(model, name, elements):
    part = {
        "parent": "minecraft:block/block",
        "texture_size": model.get("texture_size", [16, 16]),
        "textures": model["textures"],
        "elements": elements,
        "display": DISPLAY,
    }
    path = f"{ASSETS}/models/block/upgrade_station_{name}.json"
    with open(path, "w") as f:
        json.dump(part, f, indent="\t")
        f.write("\n")
    print(f"{path}: {len(elements)} elements")


def main():
    if not os.path.exists(SOURCE_MODEL):
        raise SystemExit("Run this from the repository root.")
    with open(SOURCE_MODEL) as f:
        model = json.load(f)
    elements = model["elements"]
    for name in ("base", "lid"):
        group = find_group(model["groups"], name)
        if group is None:
            raise SystemExit(f'No "{name}" group in {SOURCE_MODEL}')
        write(model, name, [elements[i] for i in sorted(element_indices(group))])


if __name__ == "__main__":
    main()
