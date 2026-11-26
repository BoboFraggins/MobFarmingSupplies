#!/usr/bin/env python3
"""Writes the game-test platform structure: a 15x8x15 space with a smooth stone floor.

Usage: python3 tools/make_gametest_platform.py <output.nbt>
"""
import gzip, struct, sys

def s(v): b = v.encode(); return struct.pack('>H', len(b)) + b
def named(tag, name, payload): return bytes([tag]) + s(name) + payload
def int_(v): return struct.pack('>i', v)
def list_(tag, items): return bytes([tag]) + int_(len(items)) + b''.join(items)
def compound(entries): return b''.join(entries) + b'\x00'

SIZE = (15, 8, 15)
blocks = [compound([named(9, 'pos', list_(3, [int_(x), int_(0), int_(z)])), named(3, 'state', int_(0))])
          for x in range(SIZE[0]) for z in range(SIZE[2])]
root = compound([
    named(3, 'DataVersion', int_(3465)),
    named(9, 'size', list_(3, [int_(v) for v in SIZE])),
    named(9, 'palette', list_(10, [compound([named(8, 'Name', s('minecraft:smooth_stone'))])])),
    named(9, 'blocks', list_(10, blocks)),
    named(9, 'entities', list_(10, [])),
])
with gzip.open(sys.argv[1], 'wb') as f:
    f.write(named(10, '', root))
