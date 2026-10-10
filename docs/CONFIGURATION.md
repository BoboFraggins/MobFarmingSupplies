# Configuration

Mob Farming Supplies writes a server config file the first time it loads. On NeoForge it is
`config/mobfarmingsupplies-server.toml`, and on Fabric it is
`config/mobfarmingsupplies-server.json`. Edit it while
the game or server is stopped, then restart.

The keys are the same on both loaders, with a few differences noted below: the layout of the DNA
pack mob lists, and the hoppers' energy and chemical rates, which only exist on
NeoForge. Values outside a setting's range are rejected on NeoForge and clamped to the range on
Fabric.

## Fan

| Key | Default | Description |
|---|---|---|
| `fan.strongerBlades` | `false` | When `true`, the airflow is stopped only by blocks with a collision shape. When `false`, any non-air block stops it. |

## Mob Harvester

| Key | Default | Range | Description |
|---|---|---|---|
| `mobHarvester.maxUpgrade` | `10` | 0 to 10 | The most upgrades of one kind a single slot accepts. |

## Clone-O-Matic

| Key | Default | Range | Description |
|---|---|---|---|
| `cloneOMatic.spawnInterval` | `5` | 1 to 200 | Ticks between spawn attempts while the Clone-O-Matic is powered. |

## Tank

| Key | Default | Range | Description |
|---|---|---|---|
| `tank.baseCapacity` | `64` | 1 to 2,000,000 | Capacity of the basic Tank, in buckets. |
| `tank.upgradeMultiplier` | `4` | 1 to 16 | How many times more each upgrade tier holds than the tier below. |

With the defaults, the Gold, Diamond and Emerald Tanks hold 256, 1,024 and 4,096 buckets. No tank
holds more than about 2.1 million buckets, whatever the settings. The server sends these values to
each player when they join, so fill levels and tooltips match the server. Lowering them never
deletes fluid: a tank holding more than its new capacity keeps its contents but accepts no more
until it drops below the limit.

## DNA Sample Pack mob lists

Each pack spawns mobs from its own list of entity type IDs, such as `minecraft:zombie`. On NeoForge
each list is the `mobs` key inside the pack's section, for example
`dnaSamplePacks.commonHostile.mobs`. On Fabric the list is the pack's key itself, for example
`dnaSamplePacks.commonHostile`.

| Pack | Key | Default mobs |
|---|---|---|
| Common/Hostile | `dnaSamplePacks.commonHostile` | zombie, zombie_villager, skeleton, enderman, spider, slime, creeper, witch |
| Common/Passive | `dnaSamplePacks.commonPassive` | cow, pig, sheep, chicken, mooshroom, rabbit |
| Aquatic | `dnaSamplePacks.aquatic` | drowned, guardian, squid, glow_squid, nautilus, dolphin, cod, salmon, tropical_fish, pufferfish |
| Nether | `dnaSamplePacks.nether` | blaze, ghast, magma_cube, piglin, wither_skeleton, zombified_piglin, zoglin, hoglin |
| Rare/Hostile | `dnaSamplePacks.rareHostile` | breeze, evoker, phantom, pillager, vindicator, cave_spider, husk, bogged, ravager |
| Rare/Passive | `dnaSamplePacks.rarePassive` | goat, armadillo, donkey, horse, turtle |
| Babies | `dnaSamplePacks.baby` | armadillo, axolotl, bee, camel, camel_husk, cat, chicken, cow, dolphin, donkey, fox, goat, happy_ghast, horse, llama, mooshroom, mule, nautilus, ocelot, panda, pig, polar_bear, rabbit, sheep, sniffer, squid, glow_squid, strider, trader_llama, turtle, villager, wolf, hoglin, husk, piglin, skeleton_horse, zombie, zombie_horse, zombie_nautilus, zombie_villager, zombified_piglin, zoglin |
| Wrong Mobs Only | `dnaSamplePacks.wrongMobs` | allay, axolotl, bat, bee, cat, copper_golem, dolphin, fox, frog, ocelot, panda, parrot, polar_bear, sniffer, snow_golem, strider, villager, wolf |

All default mobs are in the `minecraft` namespace. Mobs from the Babies pack always spawn as
babies. Mobs from the Wrong Mobs Only pack that have a baby form always spawn as adults.

The Common/Hostile and Common/Passive packs also spawn the monsters or land animals that naturally
spawn in the Clone-O-Matic's biome, including those added by other mods. The biome deny list
removes mobs from that pool; it does not affect a pack's own list. It is
`dnaSamplePacks.biomeSpawns.denyList` on NeoForge and `dnaSamplePacks.biomeSpawnDenyList` on
Fabric, is empty by default, and takes entity type IDs or `modid:*` to exclude a whole mod.

## Chest loot

| Key | Default | Range | Description |
|---|---|---|---|
| `dnaSamplePacks.chestLoot.commonChestChance` | `0.01` | 0.0 to 1.0 | Chance for each DNA Sample Pack to appear in an overworld structure chest. |
| `dnaSamplePacks.chestLoot.rareChestChance` | `0.05` | 0.0 to 1.0 | Chance for each DNA Sample Pack to appear in a Nether or End structure chest. |
| `toggleButtons.chestDropChance` | `0.05` | 0.0 to 1.0 | Chance for each of the four novelty buttons to appear in any structure chest. |
| `magicHat.chestDropChance` | `0.02` | 0.0 to 1.0 | Chance for a Magic Hat to appear in any structure chest. |
| `smores.chestDropChance` | `0.25` | 0.0 to 1.0 | Chance for one to three S'mores to appear in a village house chest. |

Overworld structure chests are those of dungeons, mineshafts, pillager outposts, woodland
mansions, jungle and desert temples, strongholds, ancient cities, trial chamber vaults, ruined
portals, igloos and shipwreck treasure. Nether and End structure chests are those of fortresses,
bastions and end cities. Village house chests are those of plains, desert, savanna, snowy and taiga
houses.

The packs that appear in chests are Rare/Hostile, Rare/Passive, Babies and Wrong Mobs Only. When
EvilCraft is installed its booster pack uses the same two chances. When Aquaculture is installed
its booster pack appears in overworld chests at the rare chance. The Aether II booster packs have
fixed chances of 0.01, 0.03 and 0.05 in the Sentry Ruins common, rare and boss chests.

The Experience Syringe, Omnidirectional Hopper and Einstein-Rosen Bridge added to dungeon,
fortress and bastion chests are set by loot tables rather than this file. See
[For modpack authors](MODPACK_AUTHORS.md#chest-loot).

## Mob spawns

| Key | Default | Description |
|---|---|---|
| `magicHat.zombiesWearHats` | `true` | When `true`, about 1 in 20 naturally spawned zombies wears a Magic Hat with a mob inside, and about 1 in 100 from a Clone-O-Matic. |
| `present.endermenCarryPresents` | `true` | When `true`, about 1 in 20 naturally spawned Endermen carries a surprise Present, and about 1 in 100 from a Clone-O-Matic. |

Mobs created with commands, spawn eggs, buckets or dispensers never get either.

## Hoppers

These settings are shared by all four hoppers: the Absorption Hopper, Logistic Sorter,
Omnidirectional Hopper and Einstein-Rosen Bridge. Each rate is the amount moved through one side.
For the Logistic Sorter, Omnidirectional Hopper and Bridge that is each input side, so a block with
two input sides moves twice as much. For the Absorption Hopper it is each side it pushes to. The
Logistic Sorter moves only items, and the Absorption Hopper pushes items and XP Juice.

| Key | Default | Range | Description |
|---|---|---|---|
| `hoppers.itemsPerTransfer` | `64` | 1 to 4096 | Items moved per side in each item transfer. |
| `hoppers.itemTransferIntervalTicks` | `8` | 1 to 200 | Ticks between item transfers. |
| `hoppers.fluidPerTick` | `1000` | 1 or more | Fluid in mB moved per side each tick. |
| `hoppers.energyPerTick` | `10000` | 1 or more | Energy in FE moved per input side each tick by the Omnidirectional Hopper and Bridge. NeoForge only. |
| `hoppers.chemicalPerTick` | `1000` | 1 or more | Mekanism chemicals in mB moved per input side each tick by the Omnidirectional Hopper and Bridge. NeoForge only, and only with Mekanism installed. |

Earlier versions called this section `omnidirectionalHopper`. Fabric still reads a section by that
name if there is no `hoppers` section. NeoForge replaces it with `hoppers` at default values and
keeps a backup of the old file.
