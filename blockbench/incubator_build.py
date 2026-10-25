#!/usr/bin/env python3
"""Builds the Incubator's game models from the Blockbench export.

Run from the repository root after re-exporting incubator.json:

    python3 blockbench/incubator_build.py

It writes, all using textures/block/incubator_texture.png:
  models/block/incubator_base.json       the "base" group without the eggs and test tube: the
                                         block's own model
  models/block/incubator_lid.json        the "lid" group, drawn by the renderer so it can open
  models/block/incubator_egg_<nest>.json the "egg" cube in each nest group (northwest, ...), drawn
                                         while that nest's row has an egg
  models/block/incubator_test_tube.json  the "test_tube" cube (in the "vial" group), drawn while
                                         there's a DNA sample

The lid's cubes keep their rotation origin (the hinge, y 8, z 16).
"""
import json
import os

ASSETS = "common/src/main/resources/assets/mobfarmingsupplies"
SOURCE_MODEL = f"{ASSETS}/models/block/incubator.json"

# Cubes left out of the base and drawn by the renderer instead, by Blockbench element name.
SEPARATE = ("egg", "test_tube")
NESTS = ("northwest", "southwest", "southeast", "northeast")

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
    path = f"{ASSETS}/models/block/incubator_{name}.json"
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
    for group_name in ("base", "lid"):
        if find_group(model["groups"], group_name) is None:
            raise SystemExit(f'No "{group_name}" group in {SOURCE_MODEL}')
    base = sorted(element_indices(find_group(model["groups"], "base")))
    lid = sorted(element_indices(find_group(model["groups"], "lid")))
    write(model, "base", [elements[i] for i in base if elements[i].get("name") not in SEPARATE])
    write(model, "lid", [elements[i] for i in lid])
    for nest in NESTS:
        group = find_group(model["groups"], nest)
        if group is None:
            raise SystemExit(f'No "{nest}" group in {SOURCE_MODEL}')
        write(model, f"egg_{nest}", [elements[i] for i in element_indices(group) if elements[i].get("name") == "egg"])
    write(model, "test_tube", [e for e in elements if e.get("name") == "test_tube"])


if __name__ == "__main__":
    main()
