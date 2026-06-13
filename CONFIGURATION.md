# Configuration

MobFarmingSupplies writes a server-side config file the first time it loads. Edit it while
the game/server is stopped, then restart to apply changes.

- **NeoForge**: `config/mobfarmingsupplies-server.toml`
- **Fabric**: `config/mobfarmingsupplies-server.json`

## Fan

| Key | Default | Description |
|---|---|---|
| `fan.strongerBlades` | `false` | If `true`, the fan's push range is only stopped by blocks with a collision shape. If `false`, any non-air block blocks the airflow. |

## Mob Harvester

| Key | Default | Description |
|---|---|---|
| `mobHarvester.maxUpgrade` | `10` | Maximum upgrade stack size accepted per slot (0–10). |

## Clone-O-Matic

| Key | Default | Description |
|---|---|---|
| `cloneOMatic.spawnInterval` | `5` | Ticks between spawn attempts while continuously powered (1–200). |

## DNA Sample Pack mob lists

Each pack has a configurable list of entity type IDs (e.g. `minecraft:zombie`) that it can
capture/spawn. All keys live under `dnaSamplePacks` and end in `.mobs`.

| Key | Default mobs |
|---|---|
| `dnaSamplePacks.commonHostile.mobs` | zombie, zombie_villager, skeleton, enderman, spider, slime, creeper, witch |
| `dnaSamplePacks.commonPassive.mobs` | cow, pig, sheep, chicken, mooshroom, rabbit |
| `dnaSamplePacks.aquatic.mobs` | drowned, guardian, squid, glow_squid, nautilus, dolphin, cod, salmon, tropical_fish, pufferfish |
| `dnaSamplePacks.nether.mobs` | blaze, ghast, magma_cube, piglin, wither_skeleton, zombified_piglin, zoglin, hoglin |
| `dnaSamplePacks.rareHostile.mobs` | breeze, evoker, phantom, pillager, vindicator, cave_spider, husk, bogged, ravager |
| `dnaSamplePacks.rarePassive.mobs` | goat, armadillo, donkey, horse, turtle |
| `dnaSamplePacks.baby.mobs` | every mob with a baby variant — spawns from this pack are forced into their baby form |
| `dnaSamplePacks.wrongMobs.mobs` | mobs that normally have baby variants — spawns from this pack are forced into their adult form |

## DNA Sample Pack chest loot

DNA sample packs (`dna_sample_rare`, `dna_sample_baby`, `dna_sample_passive_rare`,
`dna_sample_wrong`) and the EvilCraft/Aquaculture booster packs can appear in vanilla chest
loot tables. Chests are split into two tiers, each with its own configurable chance:

| Key | Default | Description |
|---|---|---|
| `dnaSamplePacks.chestLoot.commonChestChance` | `0.01` | Chance (0.0–1.0) for the four DNA sample packs (and, if EvilCraft is loaded, `dna_booster_pack_evilcraft`) to appear in "common" tier chests — overworld dungeons, mineshafts, temples, strongholds, ancient cities, trial chambers, ruined portals, igloos, and shipwrecks. |
| `dnaSamplePacks.chestLoot.rareChestChance` | `0.05` | Chance (0.0–1.0) for the same packs to appear in "rare" tier chests — nether fortresses, bastions, and end city ships. Also used for `dna_booster_pack_aquaculture` (if Aquaculture is loaded), which always appears at this rate in common-tier chests. |

The Aether II booster packs (`dna_booster_pack_aether_passive`/`_hostile`) use their own
fixed three-tier chances (0.01 / 0.03 / 0.05 across the Sentry Ruins dungeon's common, rare,
and boss chest tiers) and are not configurable.

## Toggle Buttons

| Key | Default | Description |
|---|---|---|
| `toggleButtons.chestDropChance` | `0.05` | Chance (0.0–1.0) for each of the four redstone toggle buttons to appear in vanilla chest loot tables. |
