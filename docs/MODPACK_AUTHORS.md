# For modpack authors

This page covers what you can adjust in Mob Farming Supplies without touching code, and how the
mod fits alongside others.

## Configuration

The server config holds the tunable numbers: Fan behaviour, the Mob Harvester's upgrade limit, the
Clone-O-Matic's spawn rate, Tank sizes, the mob lists for each DNA Sample Pack, the chance of each
item appearing in chest loot, and the transfer rates shared by the four hoppers. Zombies spawning
with Magic Hats, Endermen spawning with Presents, Tritanium Ore and mobs carrying Tritanium Alloy
tools can each be switched off. The file is `config/mobfarmingsupplies-server.toml` on NeoForge and
`config/mobfarmingsupplies-server.json` on Fabric. [Configuration](CONFIGURATION.md) documents the
keys.

All four hoppers, the Absorption Hopper, Logistic Sorter, Omnidirectional Hopper and Einstein-Rosen
Bridge, share one set of transfer rates, measured per side. By default each side moves 64 items
every 8 ticks and 1,000 mB of fluid per tick, and on NeoForge the Omnidirectional Hopper and Bridge
also move 10,000 energy and 1,000 mB of Mekanism chemicals per tick.

## Chest loot

Several items are added to vanilla chest loot, each at a configurable chance. DNA Sample Packs and
the four novelty buttons appear in dungeon, temple, stronghold and similar chests. Magic Hats
appear in the same kinds of chests at 2% by default. S'mores appear in village house chests at 25%.

Several items are added through loot tables you can override with a datapack:
`mobfarmingsupplies:chests/inject/dungeon` adds an Experience Syringe holding three to seven levels
to dungeon chests, `chests/inject/nether_fortress` adds an Omnidirectional Hopper to fortress
chests, and `chests/inject/bastion` adds a linked pair of Einstein-Rosen Bridges to bastion chests.
Each rolls at 25%. `chests/inject/end_city_treasure` adds an enchanted Tritanium Alloy Paxel to End
City chests at 5%. `chests/inject/brains` adds a Brain or a Zombie Brain to the same structure
chests as the Magic Hat at 1%, and `entities/inject/zombie_brain` gives zombies, husks, drowned,
zombie villagers and zombified piglins a one in eight chance to drop a Zombie Brain. Replacing a
table with an empty one removes that item from loot.

## Tritanium

Tritanium is a metal for later machines and the base of Tritanium Alloy. Its ore needs an iron
pickaxe. The ore is placed by the `mobfarmingsupplies:tritanium_ore` placed feature, which a
datapack can override, and can be switched off in the config. Its items carry the usual common tags,
such as `c:ores/tritanium`, `c:raw_materials/tritanium`, `c:ingots/tritanium`, `c:nuggets/tritanium`
and `c:storage_blocks/tritanium`, so other mods' processing recipes can use them.

Tritanium Alloy comes in ingots, nuggets, blocks, tools and armor, with no ore. Its tools and armor
match Mekanism Tools' Osmium. One Tritanium and three Iron of the same size, as nuggets, ingots or
blocks, craft into four of the alloy. Its tags follow the same pattern, such as
`c:ingots/tritanium_alloy`.

Dilithium Ore is mined like diamond and drops Dilithium Crystals, which carry `c:gems/dilithium`;
the ore is in `c:ores/dilithium` and placed by the `mobfarmingsupplies:dilithium_ore` placed
feature.

With Allthemodium installed, both ores also generate in its mining dimension, between Y 65 and 247
where its stone and deepslate are. The placed features are `mobfarmingsupplies:mining/tritanium_ore`
and `mobfarmingsupplies:mining/dilithium_ore`, and the same config settings switch them off.

On NeoForge, both metals join four other mods when they are installed. Productive Metalworks melts
and casts them, and its foundry alloys one part Molten Tritanium with three of Molten Iron. Mystical
Agriculture grows Tritanium as a tier three crop and Tritanium Alloy as a tier four crop. Productive
Bees adds a Tritanium Bee, bred from an Iron Bee and an Emerald Bee, and a Tritanium Alloy Bee, bred
from a Tritanium Bee and an Iron Bee. Ex Deorum sieves Tritanium Ore Chunks from gravel.

## Recipes

Every recipe is a datapack JSON and can be replaced. Six use the mod's own recipe types:
`tank_upgrade` (keeps a Tank's contents when upgrading its tier), `bridge_pair` and `bridge_link`
(give the crafted bridges a fresh shared channel), `anvil_crushing` (items crushed by a falling
anvil), `shearing` (an item cut with a tool in the crafting grid, such as Tritanium Alloy Plates and
Wire, which hands the tool back one point more worn) and `circuit_inscribing` (what the Circuit
Inscriber makes). They follow the vanilla shaped and shapeless formats. The crushing recipe takes an
`ingredient` and a `result`, and the shearing recipe takes an `ingredient`, a `result` and an
optional `tool`, which defaults to Shears. The inscribing recipe takes a `first` ingredient, an
optional `second`, a `result`, the total `energy` the craft uses and its `time` in ticks; the energy
is spread evenly over that time. The Isolinear Optical Chip upgrades use 64,000 energy over 300
ticks (Mark 2, with a gold ingot), 88,000 over 600 (Mark 3, with an emerald) and 114,000 over 1,200
(Mark 4, with a netherite ingot).

## Tags

Blocks in `c:relocation_not_supported` cannot be wrapped in a Present. The Present itself is in
that tag, which keeps mods like Carry On from picking it up. Add any block that should never be
moved.

Entity types in `mobfarmingsupplies:no_dna_sampling` cannot be sampled with a DNA Collector, and
those in `mobfarmingsupplies:cannot_use_toilet` won't sit on a Toilet. Both include the Wither and
the Ender Dragon by default.

Items in `mobfarmingsupplies:repairs_tritanium_alloy` repair Tritanium Alloy tools and armor; by
default that is `c:ingots/tritanium_alloy`. Blocks in `mobfarmingsupplies:mineable/paxel` are mined
quickly by the Paxel.

Every mob head, including the mod's own, is in `minecraft:skulls`, and the head blocks are in
`c:skulls`. XP Juice is in `c:experience`, the common tag mods use to recognise experience fluids.

## Advancements and quests

The mod has its own advancement tab of 17 advancements across three lines: Logistics, Mob Farming
and Fun Stuff. A sample [FTB Quests](https://www.curseforge.com/minecraft/mc-mods/ftb-quests-forge)
chapter that mirrors them is in [extras/ftbquests](../extras/ftbquests). Each quest completes when
its advancement is earned. Copy the two files into your pack's quests folder as its README
describes, then edit the rewards and placement to suit your pack.

## Compatibility

The Stirling Engine makes energy from lava, magma or a lit campfire below it and pushes it into
every neighbour that takes energy. On NeoForge that is Forge Energy. On Fabric it is Team Reborn
Energy, the API most Fabric tech mods use, which is bundled with the mod. Each upgrade tier doubles
its output, from 50 a tick over lava for the basic engine to 800 for the Emerald one.

JEI shows every mob head and guide pages for the processes that don't use a crafting table, such as
making XP Juice, anvil crushing and beheading. Jade shows block details such as a bridge's channel
and a Present's contents. On NeoForge, the Magic Hat and Picnic Basket fit Curios slots, the
Omnidirectional Hopper and Bridge move Mekanism chemicals, and the Picnic Basket's auto-feed favours
foods the player hasn't eaten yet when Spice of Life: Carrot Edition is installed. On Fabric, the
Magic Hat and Picnic Basket fit Trinkets Updated slots. DNA Booster Packs for Aquaculture,
EvilCraft and the Aether II mobs are added when those mods are present.
