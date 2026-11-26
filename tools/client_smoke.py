#!/usr/bin/env python3
"""Writes the DevCommands script for the client smoke test (see tools/test-forge.sh).

The script, run by the dev client in a copy of the flat test world, builds a showcase and
photographs it:
  * every block of the mod, placed from its item the way a player places it (rows at z=100);
  * every item of the mod in an item frame (walls at z=121 and z=141), which renders each item's
    model or item renderer (tank fluids, hopper side grids, mob heads);
  * every screen, opened by clicking its block.
Screenshots land in <run>/screenshots/smoke_*.png. The game log is checked separately
(tools/check_client_log.py).

Usage: python3 tools/client_smoke.py <output commands file>
"""
import json, os, sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, 'common/src/main/resources/assets/mobfarmingsupplies')
MOD = 'mobfarmingsupplies'

# Blocks whose screens the game tests found (MenuTests); clicked here to photograph each screen.
SCREENS = ['absorption_hopper', 'anvil', 'block_breaker', 'clone_o_matic', 'color_workstation', 'diamond_tank',
           'disenchanter', 'einstein_rosen_bridge', 'emerald_tank', 'ender_inhibitor', 'essence_hopper',
           'experience_pylon', 'fan', 'filter_scribing_terminal', 'gold_tank', 'head_pike', 'incubator',
           'logistic_sorter', 'mob_harvester', 'muffler', 'omnidirectional_hopper', 'picnic_basket', 'tank',
           'upgrade_station', 'warden_inhibitor', 'wither_inhibitor']

# Blocks placed with /setblock: their items only place while sneaking, which the script can't do.
SETBLOCK = {'picnic_basket'}

Y = -60          # the flat world's surface: blocks stand at y=-60 on the floor at -61
BLOCK_Z = 100    # first row of blocks
PER_ROW = 10
SPACING = 3
X0 = 100


def ids():
    lang = json.load(open(os.path.join(ASSETS, 'lang/en_us.json')))
    items = sorted(f[:-5] for f in os.listdir(os.path.join(ASSETS, 'models/item')) if f.endswith('.json'))
    items = [i for i in items if f'item.{MOD}.{i}' in lang or f'block.{MOD}.{i}' in lang]
    states = {f[:-5] for f in os.listdir(os.path.join(ASSETS, 'blockstates'))}
    blocks = [i for i in items if i in states and f'block.{MOD}.{i}' in lang]
    return blocks, items


def main(out_path):
    blocks, items = ids()
    out = []
    add = out.append
    for line in ['gamemode creative @s', 'gamerule sendCommandFeedback false', 'time set noon', 'gamerule doDaylightCycle false',
                 'gamerule doMobSpawning false', 'gamerule doWeatherCycle false', 'weather clear',
                 'kill @e[type=!player]',
                 f'fill {X0 - 5} {Y} {BLOCK_Z - 15} {X0 + PER_ROW * SPACING + 5} {Y + 10} {BLOCK_Z + 15} air',
                 f'fill {X0 - 5} {Y} {BLOCK_Z + 16} {X0 + PER_ROW * SPACING + 5} {Y + 10} {BLOCK_Z + 45} air']:
        add(line)

    # Blocks, placed from their items by the player standing 3 blocks north, facing south.
    where = {}
    for n, block in enumerate(blocks):
        x = X0 + (n % PER_ROW) * SPACING
        z = BLOCK_Z + (n // PER_ROW) * 4
        where[block] = (x, z)
        add(f'tp @s {x} {Y + 1} {z - 3} 0 30')
        if block in SETBLOCK:
            add(f'setblock {x} {Y} {z} {MOD}:{block}')
            continue
        add(f'item replace entity @s weapon.mainhand with {MOD}:{block}')
        add(f'USEON {x} {Y - 1} {z}')
        add('CLOSE')  # in case using the item opened something instead of placing
    add('item replace entity @s weapon.mainhand with minecraft:air')
    add('HIDEGUI on')
    add('WAIT 40')
    rows = (len(blocks) + PER_ROW - 1) // PER_ROW
    for row in range(rows):
        z = BLOCK_Z + row * 4
        for half, x in enumerate([X0 + 6, X0 + 21]):
            add(f'tp @s {x} {Y + 3} {z - 7} 0 20')
            add('WAIT 20')
            add(f'SCREENSHOT smoke_blocks_row{row}_{half}')

    # Items in item frames on stone walls, 16 across and up to 6 high per wall.
    walls = [BLOCK_Z + 21, BLOCK_Z + 41]
    per_wall = 16 * 6
    for w, wall_z in enumerate(walls):
        add(f'fill {X0} {Y} {wall_z} {X0 + 15} {Y + 5} {wall_z} stone')
    for n, item in enumerate(items[:per_wall * len(walls)]):
        wall_z = walls[n // per_wall]
        k = n % per_wall
        x, y = X0 + k % 16, Y + 5 - k // 16
        add(f'summon item_frame {x} {y} {wall_z - 1} {{Facing:2b,Fixed:1b,Invulnerable:1b,'
            f'Item:{{id:"{MOD}:{item}",Count:1b}}}}')
    add('WAIT 40')
    for w, wall_z in enumerate(walls):
        if w * per_wall >= len(items):
            break
        for half, x in enumerate([X0 + 4, X0 + 11]):
            add(f'tp @s {x}.5 {Y + 1.4} {wall_z - 8} 0 -2')
            add('WAIT 20')
            add(f'SCREENSHOT smoke_items_wall{w}_{half}')

    # Screens.
    add('HIDEGUI off')
    for block in SCREENS:
        if block not in where:
            continue
        x, z = where[block]
        add(f'tp @s {x} {Y + 1} {z - 2} 0 40')
        add('WAIT 5')
        add(f'USEBLOCK {x} {Y} {z}')
        add('WAIT 15')
        add(f'SCREENSHOT smoke_screen_{block}')
        add('CLOSE')
        add('WAIT 5')

    with open(out_path, 'w') as f:
        f.write('\n'.join(out) + '\n')
    print(f'{len(blocks)} blocks, {len(items)} items, {len([s for s in SCREENS if s in where])} screens')


if __name__ == '__main__':
    main(sys.argv[1])
