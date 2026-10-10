# For modpack authors

This page covers what you can adjust in Mob Farming Supplies without touching code, and how the
mod fits alongside others.

## Configuration

The server config holds the tunable numbers: Fan behaviour, the Mob Harvester's upgrade limit, the
Clone-O-Matic's spawn rate, Tank sizes, the mob lists for each DNA Sample Pack, the chance of each
item appearing in chest loot, and the transfer rates shared by the four hoppers. Zombies spawning
with Magic Hats and Endermen spawning with Presents can each be switched off. The file is
`config/mobfarmingsupplies-server.toml` on NeoForge and `config/mobfarmingsupplies-server.json` on
Fabric. [Configuration](CONFIGURATION.md) documents the keys.

All four hoppers, the Absorption Hopper, Logistic Sorter, Omnidirectional Hopper and Einstein-Rosen
Bridge, share one set of transfer rates, measured per side. By default each side moves 64 items
every 8 ticks and 1,000 mB of fluid per tick, and on NeoForge the Omnidirectional Hopper and Bridge
also move 10,000 energy and 1,000 mB of Mekanism chemicals per tick.

## Chest loot

Several items are added to vanilla chest loot, each at a configurable chance. DNA Sample Packs and
the four novelty buttons appear in dungeon, temple, stronghold and similar chests. Magic Hats
appear in the same kinds of chests at 2% by default. S'mores appear in village house chests at 25%.

Three items are added through loot tables you can override with a datapack:
`mobfarmingsupplies:chests/inject/dungeon` adds an Experience Syringe holding three to seven levels
to dungeon chests, `chests/inject/nether_fortress` adds an Omnidirectional Hopper to fortress
chests, and `chests/inject/bastion` adds a linked pair of Einstein-Rosen Bridges to bastion chests.
Each rolls at 25%. Replacing a table with an empty one removes that item from loot.

## Recipes

Every recipe is a datapack JSON and can be replaced. Four use the mod's own recipe types:
`tank_upgrade` (keeps a Tank's contents when upgrading its tier), `bridge_pair` and `bridge_link`
(give the crafted bridges a fresh shared channel) and `anvil_crushing` (items crushed by a falling
anvil). They follow the vanilla shaped and shapeless formats, and the crushing recipe takes an
`ingredient` and a `result`.

## Tags

Blocks in `c:relocation_not_supported` cannot be wrapped in a Present. The Present itself is in
that tag, which keeps mods like Carry On from picking it up. Add any block that should never be
moved.

Entity types in `mobfarmingsupplies:no_dna_sampling` cannot be sampled with a DNA Collector, and
those in `mobfarmingsupplies:cannot_use_toilet` won't sit on a Toilet. Both include the Wither and
the Ender Dragon by default.

Every mob head, including the mod's own, is in `minecraft:skulls`, and the head blocks are in
`c:skulls`. XP Juice is in `c:experience`, the common tag mods use to recognise experience fluids.

## Advancements and quests

The mod has its own advancement tab of 17 advancements across three lines: Logistics, Mob Farming
and Fun Stuff. A sample [FTB Quests](https://www.curseforge.com/minecraft/mc-mods/ftb-quests-forge)
chapter that mirrors them is in [extras/ftbquests](../extras/ftbquests). Each quest completes when
its advancement is earned. Copy the two files into your pack's quests folder as its README
describes, then edit the rewards and placement to suit your pack.

## Compatibility

JEI shows every mob head and guide pages for the processes that don't use a crafting table, such as
making XP Juice, anvil crushing and beheading. Jade shows block details such as a bridge's channel
and a Present's contents. On NeoForge, the Magic Hat and Picnic Basket fit Curios slots, the
Omnidirectional Hopper and Bridge move Mekanism chemicals, and the Picnic Basket's auto-feed favours
foods the player hasn't eaten yet when Spice of Life: Carrot Edition is installed. On Fabric, the
Magic Hat and Picnic Basket fit Trinkets Updated slots. DNA Booster Packs for Aquaculture,
EvilCraft and the Aether II mobs are added when those mods are present.
