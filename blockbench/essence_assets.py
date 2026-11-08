#!/usr/bin/env python3
"""Builds each essence's art from vanilla's textures and our own. Needs Pillow.

Run from the repository root, after adding an essence to ESSENCES, with a Minecraft jar:

    python3 blockbench/essence_assets.py path/to/minecraft.jar

It writes, for each essence:
  textures/block/essence_of_<id>_{still,flow}.png  vanilla's lava, recoloured, with its animation
  textures/item/essence_of_<id>_bucket.png         the XP Juice bucket, recoloured
  textures/item/essence_filter_<id>.png            a Blank Filter card in lime, with the mob's face
"""
import io
import sys
import zipfile

from PIL import Image

ASSETS = "common/src/main/resources/assets/mobfarmingsupplies/textures"

# id: dark, middle and light fluid colours, the mob's texture, and where its head's front face is.
ESSENCES = {
    "venom": {
        "colors": ((8, 40, 14), (24, 96, 40), (86, 172, 84)),
        "skin": "assets/minecraft/textures/entity/spider/spider.png",
        "face": (40, 12, 48, 20),
    },
}

# The Blank Filter's purples, and the lime that replaces them.
CARD = {(58, 25, 156): (118, 214, 32), (125, 86, 242): (176, 240, 96)}
# Where a mob's face goes on the card (the Smite Upgrade's), and how big each of its pixels is.
FACE_LEFT, FACE_TOP, FACE_SCALE = 12, 17, 1


def mix(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def shade(luma, colors):
    dark, middle, light = colors
    return mix(dark, middle, luma * 2) if luma < 0.5 else mix(middle, light, luma * 2 - 1)


def luminance(rgb):
    return (0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]) / 255


def recolour(image, colors, wanted):
    """Shades the pixels picked by wanted with colors, by their brightness among themselves."""
    px = image.load()
    width, height = image.size
    lumas = [luminance(px[x, y]) for x in range(width) for y in range(height) if wanted(px[x, y])]
    low, high = min(lumas), max(lumas)
    for y in range(height):
        for x in range(width):
            if wanted(px[x, y]):
                luma = (luminance(px[x, y]) - low) / (high - low or 1)
                px[x, y] = shade(luma, colors) + (px[x, y][3],)


def main():
    if len(sys.argv) != 2:
        raise SystemExit(__doc__)
    with zipfile.ZipFile(sys.argv[1]) as jar:
        def load(path):
            return Image.open(io.BytesIO(jar.read(path))).convert("RGBA")

        for name, essence in ESSENCES.items():
            fluid = f"essence_of_{name}"
            for kind in ("still", "flow"):
                lava = f"assets/minecraft/textures/block/lava_{kind}.png"
                image = load(lava)
                recolour(image, essence["colors"], lambda p: p[3] > 0)
                image.save(f"{ASSETS}/block/{fluid}_{kind}.png")
                with open(f"{ASSETS}/block/{fluid}_{kind}.png.mcmeta", "wb") as f:
                    f.write(jar.read(lava + ".mcmeta"))

            bucket = Image.open(f"{ASSETS}/item/xp_juice_bucket.png").convert("RGBA")
            is_juice = lambda p: 0 < p[3] < 255 and p[1] > p[0] and p[1] > p[2]
            recolour(bucket, essence["colors"], is_juice)
            px = bucket.load()
            for y in range(bucket.size[1]):
                for x in range(bucket.size[0]):
                    if px[x, y][3] not in (0, 255):
                        px[x, y] = px[x, y][:3] + (255,)  # dark colours need to be opaque to read
            bucket.save(f"{ASSETS}/item/{fluid}_bucket.png")

            card = Image.open(f"{ASSETS}/item/blank_filter.png").convert("RGBA")
            px = card.load()
            for y in range(card.size[1]):
                for x in range(card.size[0]):
                    if px[x, y][:3] in CARD and px[x, y][3] > 0:
                        px[x, y] = CARD[px[x, y][:3]] + (px[x, y][3],)
            face = load(essence["skin"]).crop(essence["face"])
            face = face.resize((face.width * FACE_SCALE, face.height * FACE_SCALE), Image.NEAREST)
            card.paste(face, (FACE_LEFT, FACE_TOP), face)
            card.save(f"{ASSETS}/item/essence_filter_{name}.png")
            print(name)


if __name__ == "__main__":
    main()
