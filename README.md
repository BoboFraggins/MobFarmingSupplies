# Mob Farming Supplies

Mob Farming Supplies is a toolkit for building automated mob farms in Minecraft: machines that
move, hold and kill mobs, a cloning system, item and fluid logistics with real filtering, and
a handful of things that are simply fun to have.

It runs on NeoForge and Fabric, built with [Architectury](https://docs.architectury.dev/).

## Logistics

Four specialized hoppers move resources, and all four are configured with the same placement-aware
side grid.

The Absorption Hopper collects from the world and pushes to whichever sides you choose. The
Logistic Sorter reads Item Filters and routes each item to a matching side or a non-matching side,
holding nothing itself, so it can't jam or lose items. The Omnidirectional Hopper pulls and pushes
on any side and moves fluids as well as items, along with energy and Mekanism chemicals on
NeoForge, splitting evenly across its outputs. The Einstein-Rosen Bridge is an Omnidirectional
Hopper that works at a distance: bridges crafted as a pair share a channel and move resources
between each other across any distance, including between dimensions, as long as both ends
are chunk-loaded.

Filtering is done with Item Filters, scribed at the Filter Scribing Terminal from Blank Filters.
A filter can match an exact item, a similar item, everything from a given mod, a common tag such
as ores or ingots, or a property such as being enchanted or damaged. The Sorter, Omnidirectional
Hopper and Bridge each combine their filters with AND or OR.

## Mob farming

The Mob Harvester is the center of most farms. Powered by redstone, it attacks every mob in a
3x3x3 area, and the mobs drop their loot and experience as if a player had killed
them. Its three upgrade slots take Sharpness, Looting and Beheading Upgrades. Beheading gives a
chance for the mob's head to drop, and almost every mob in the game has one, not just the handful
vanilla provides. Players are not exempt.

Getting mobs to the harvester is the job of the movement blocks. A Fan pushes entities away from
its face when powered, with upgrades that extend its reach in width, height and distance. A Vector
Plate carries anything standing on it in the direction it faces while keeping it centred in its
lane, which makes conveyors simple to build. A Halting Plate does the opposite: it draws a mob to
its centre and holds it there, which is useful for keeping a target exactly where the harvester
can reach it.

The farm itself can be built to stay intact. Wither-Proof Glass survives explosions and the
Wither. Mob Exclusion Glass is just as tough but lets players walk through while staying solid to
everything else. The Ender Inhibitor stops Endermen teleporting anywhere near it.

## Cloning

A DNA Collector captures a mob's complete data, including its variant and name, into a
DNA Sample. Feed samples to a powered Clone-O-Matic and it spawns fresh copies of those mobs around
itself, choosing open space so it never jams. DNA Sample Packs, found in chest loot, hold
collections of common, aquatic, Nether, rare and baby mobs, with mob lists that can be configured.

## Experience

Experience is a fluid here, called XP Juice. The Absorption Hopper vacuums up nearby items and
experience orbs and pushes them out to neighbouring inventories and tanks. The Experience Syringe
stores your own levels for later: shift-click to bank your progress, click to take back enough for
the next level. Empty either into a Tank and the experience can be bottled, piped or carried in a
bucket.

The Tank holds any fluid and keeps its contents when broken. It upgrades in place from the basic
64 buckets to Gold, Diamond and Emerald tiers, the last holding 4,096 buckets by default, and the
upgrade recipe keeps whatever is inside. Both the starting size and the growth per tier are
configurable.

## Everything else

The Present relocates blocks. Use it on almost any block, including a full chest or a machine, to
wrap it up with its contents intact, carry it anywhere, and unwrap it in its new home. The Magic
Hat yoinks mobs: right-click one to catch it, right-click again to let it out wherever you are,
and it can be worn on the head or in an accessory slot. The Picnic Basket stores food and feeds you
automatically when you get hungry, and a campfire, some marshmallows and a few ingredients make
S'mores. The Toilet destroys anything piped into it, provides endless water, and can be sat on.
The four novelty buttons play a red alert, a dramatic sting, a rimshot or a Wilhelm scream.

The mod has its own advancement tab, and JEI pages explain the processes that don't use a crafting
table.

## Documentation

[Configuration](docs/CONFIGURATION.md) lists the server config options.
[For modpack authors](docs/MODPACK_AUTHORS.md) covers loot, tags, compatibility and the sample
quest chapter.
[Credits](docs/CREDITS.md) acknowledges the work this mod builds on.

## License

[MIT](LICENSE) © 2026 BoboFraggins
