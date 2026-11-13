#!/usr/bin/env python3
"""Builds each essence's art from vanilla's textures and our own. Needs Pillow.

Run from the repository root, after adding an essence to ESSENCES, with a Minecraft jar:

    python3 blockbench/essence_assets.py path/to/minecraft.jar

It writes, for each essence:
  textures/block/essence_of_<id>_{still,flow}.png  vanilla's lava, recoloured, with its animation
  textures/item/essence_of_<id>_bucket.png         the XP Juice bucket, recoloured
  textures/item/essence_filter_<id>.png            a Blank Filter card in lime, with the mob's face
"""
import colorsys
import io
import json
import sys
import zipfile

from PIL import Image

ASSETS = "common/src/main/resources/assets/mobfarmingsupplies/textures"

# id: dark, middle and light fluid colours (or "rainbow"), the mob's texture (and any glowing eyes layered on
# it), where its head's front face is, and optionally how much to enlarge a tiny face and where a
# separately modelled snout is on the texture and goes on the face.
ESSENCES = {
    "venom": {
        "colors": ((8, 40, 14), (24, 96, 40), (86, 172, 84)),
        "skin": "assets/minecraft/textures/entity/spider/spider.png",
        "face": (40, 12, 48, 20),
    },
    "spacetime": {
        "colors": ((3, 1, 8), (62, 16, 124), (170, 70, 235)),
        "skin": "assets/minecraft/textures/entity/enderman/enderman.png",
        "eyes": "assets/minecraft/textures/entity/enderman/enderman_eyes.png",
        "face": (8, 8, 16, 16),
    },
    "water": {
        "colors": ((1, 3, 12), (12, 42, 128), (66, 128, 235)),
        "skin": "assets/minecraft/textures/entity/squid/squid.png",
        "face": (14, 14, 22, 22),  # the middle of its tall front, with both eyes
    },
    "fire": {
        "colors": ((6, 1, 1), (128, 12, 12), (240, 58, 40)),
        "skin": "assets/minecraft/textures/entity/blaze/blaze.png",
        "face": (8, 8, 16, 16),
    },
    "sight": {
        "colors": ((4, 4, 4), (112, 112, 112), (245, 245, 245)),
        "skin": "assets/minecraft/textures/entity/bat/bat.png",
        "face": (1, 9, 7, 12),  # its eyes and nose, which wrap onto the sides of its tiny head
        "scale": 2,
    },
    "wither": {
        "colors": ((38, 38, 40), (124, 124, 128), (245, 245, 245)),
        "skin": "assets/minecraft/textures/entity/skeleton/wither_skeleton.png",
        "face": (8, 8, 16, 16),
    },
    "growth": {
        "colors": ((242, 133, 0), (250, 180, 30), (255, 225, 53)),
        "skin": "assets/minecraft/textures/entity/cow/cow_temperate.png",
        "face": (6, 6, 14, 14),
    },
    "color": {
        "rainbow": True,
        "skin": "assets/minecraft/textures/entity/sheep/sheep.png",
        "face": (8, 8, 14, 14),
    },
    "frost": {
        "colors": ((150, 205, 245), (205, 232, 250), (255, 255, 255)),
        "skin": "assets/minecraft/textures/entity/snow_golem/snow_golem.png",
        "face": (8, 8, 16, 16),
    },
    "air": {
        "colors": ((196, 196, 200), (164, 198, 222), (120, 200, 245)),
        "skin": "assets/minecraft/textures/entity/phantom/phantom.png",
        "eyes": "assets/minecraft/textures/entity/phantom/phantom_eyes.png",
        "face": (5, 5, 12, 8),  # its flat head's front, with both eyes
        "scale": 2,
    },
    "undeath": {
        "colors": ((45, 47, 48), (84, 140, 44), (128, 232, 42)),
        "skin": "assets/minecraft/textures/entity/zombie/zombie.png",
        "face": (8, 8, 16, 16),
    },
    "bacon": {
        "colors": ((250, 170, 190), (253, 213, 223), (255, 255, 255)),
        "skin": "assets/minecraft/textures/entity/pig/pig_temperate.png",
        "face": (8, 8, 16, 16),
        "snout": ((17, 17, 21, 20), (2, 4)),
    },
}

# The Blank Filter's purples, and the lime that replaces them.
CARD = {(58, 25, 156): (118, 214, 32), (125, 86, 242): (176, 240, 96)}
# A rainbow fluid's animation: as long as still lava's, which plays its 20 frames there and back.
RAINBOW_FRAMES, RAINBOW_FRAMETIME = 38, 2
WHITE = (255, 255, 255)

# The middle of a mob's face on the card (as on the Smite Upgrade's).
FACE_CENTRE_X, FACE_CENTRE_Y = 16, 21


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


def rgb(hue, saturation, value):
    return tuple(round(c * 255) for c in colorsys.hsv_to_rgb(hue % 1, saturation, value))


def rainbow_fluid(image):
    """Lava with each frame shaded from one colour of the rainbow to white, going round the
    rainbow over the animation. Source and flowing fluid get the same number of frames at the
    same speed, so they're the same colour at the same moment, and lava's own frames are written
    out in the order they play, so the colours join up where the animation loops. Still lava
    bounces back and forth through its frames; flowing lava loops through its own twice.
    Returns the new strip and its .mcmeta."""
    size = image.size[0]
    count = image.size[1] // size
    if count == RAINBOW_FRAMES // 2 + 1:
        order = list(range(count)) + list(range(count - 2, 0, -1))
    else:
        order = [i * 2 * count // RAINBOW_FRAMES % count for i in range(RAINBOW_FRAMES)]
    source = image.load()
    lumas = [luminance(source[x, y]) for x in range(size) for y in range(image.size[1])]
    low, high = min(lumas), max(lumas)
    strip = Image.new("RGBA", (size, size * len(order)))
    px = strip.load()
    for i, frame in enumerate(order):
        colour = rgb(i / len(order), 1.0, 0.95)
        colours = (colour, mix(colour, WHITE, 0.5), WHITE)
        for y in range(size):
            for x in range(size):
                luma = (luminance(source[x, frame * size + y]) - low) / (high - low or 1)
                px[x, i * size + y] = shade(luma, colours) + (255,)
    return strip, json.dumps({"animation": {"frametime": RAINBOW_FRAMETIME}}, indent=2).encode() + b"\n"


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
                mcmeta = jar.read(lava + ".mcmeta")
                if essence.get("rainbow"):
                    image, mcmeta = rainbow_fluid(image)
                else:
                    recolour(image, essence["colors"], lambda p: p[3] > 0)
                image.save(f"{ASSETS}/block/{fluid}_{kind}.png")
                with open(f"{ASSETS}/block/{fluid}_{kind}.png.mcmeta", "wb") as f:
                    f.write(mcmeta)

            bucket = Image.open(f"{ASSETS}/item/xp_juice_bucket.png").convert("RGBA")
            is_juice = lambda p: 0 < p[3] < 255 and p[1] > p[0] and p[1] > p[2]
            px = bucket.load()
            if essence.get("rainbow"):
                for y in range(bucket.size[1]):
                    for x in range(bucket.size[0]):
                        if is_juice(px[x, y]):
                            px[x, y] = rgb(x / bucket.size[0], 0.85, 0.95) + (px[x, y][3],)
            else:
                recolour(bucket, essence["colors"], is_juice)
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
            skin = load(essence["skin"])
            if "eyes" in essence:
                skin.alpha_composite(load(essence["eyes"]))
            face = skin.crop(essence["face"])
            if "snout" in essence:
                area, at = essence["snout"]
                snout = skin.crop(area)
                face.paste(snout, at, snout)
            scale = essence.get("scale", 1)
            face = face.resize((face.width * scale, face.height * scale), Image.NEAREST)
            card.paste(face, (FACE_CENTRE_X - face.width // 2, FACE_CENTRE_Y - face.height // 2), face)
            card.save(f"{ASSETS}/item/essence_filter_{name}.png")
            print(name)


if __name__ == "__main__":
    main()
