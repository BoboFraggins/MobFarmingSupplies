#!/usr/bin/env python3
"""Tiles screenshots into contact sheets for a quick look.

Usage: python3 tools/contact_sheet.py <out prefix> <columns> <tile width> <png>...
Writes <out prefix>_<n>.png, at most 12 tiles each, each tile labelled with its file name.
"""
import os, sys
from PIL import Image, ImageDraw

prefix, cols, tile_w = sys.argv[1], int(sys.argv[2]), int(sys.argv[3])
files = sys.argv[4:]
per_sheet = cols * 3
for sheet in range(0, len(files), per_sheet):
    batch = files[sheet:sheet + per_sheet]
    images = [Image.open(f).convert('RGB') for f in batch]
    tile_h = int(tile_w * images[0].height / images[0].width)
    rows = (len(images) + cols - 1) // cols
    out = Image.new('RGB', (cols * tile_w, rows * (tile_h + 16)), 'black')
    draw = ImageDraw.Draw(out)
    for i, (f, im) in enumerate(zip(batch, images)):
        x, y = (i % cols) * tile_w, (i // cols) * (tile_h + 16)
        out.paste(im.resize((tile_w, tile_h)), (x, y + 16))
        draw.text((x + 4, y + 2), os.path.basename(f)[:-4], fill='white')
    out.save(f'{prefix}_{sheet // per_sheet}.png')
    print(f'{prefix}_{sheet // per_sheet}.png')
