# Mob Farming Supplies

Mob processing utilities for Minecraft — a toolkit of machines and items for building
automated mob farms: harvest mobs for drops and XP, vacuum up the loot, clone mobs from
DNA samples, and keep your farm contained and wither-proof.

Built with [Architectury](https://docs.architectury.dev/) for **NeoForge** and **Fabric**.

## Features

### Mob Harvester

Place inside a mob grinder. When powered by redstone, it attacks everything in a 3×3×3 area.
Killed mobs drop items and XP orbs as normal — pair with an Absorption Hopper to collect
everything automatically.

Nine upgrade slots accept:

- **Sharpness Upgrade** — increases attack damage
- **Looting Upgrade** — increases drop rates
- **Beheading Upgrade** — chance to drop mob heads

### Clone-O-Matic

A redstone-powered mob duplicator. Place DNA Samples or DNA Sample Packs into its nine
DNA slots; while continuously powered, it periodically attempts to spawn a clone of one
of the stored mobs in the space above the block (spawn interval is configurable, default
every 5 ticks). Spawn attempts respect block collisions, so the Clone-O-Matic won't jam
itself by trying to spawn a mob inside a wall — but it ignores nearby entities, so it
keeps working even in a crowded farm.

### DNA Collector & DNA Samples

- **DNA Collector** — a single-use tool. Right-click any non-player mob to
  capture its full save data (type, equipment, name, variant, etc.) into a **DNA
  Sample** item.
- **DNA Sample** — a unique item representing one specific captured mob. Feed it to a
  Clone-O-Matic to spawn fresh copies of that exact mob (each clone gets its own
  identity, so the mob cap isn't accidentally bypassed or blocked).
- **DNA Sample Packs / DNA Booster Packs** — pre-rolled collections of common mobs
  (Common Hostile, Common Passive, Aquatic, Nether, Baby, Rare Hostile/Passive, "Wrong
  Mobs Only", and packs for supported mod-compat mobs) that can be found in dungeon
  loot. Mob lists are configurable.

### Absorption Hopper

A block that vacuums up nearby item entities and XP orbs into internal storage, then
automatically pushes collected items and XP fluid out to adjacent inventories and tanks.
Each of its six sides can be individually toggled on/off for output via the in-game UI —
handy for routing items to specific pipes/chests while keeping XP flowing elsewhere.

### Tank

A fluid storage block (also usable as a large bucket) that holds **XP Juice** and other
fluids. Right-click with a bucket to fill/drain.

### XP Juice & Experience Syringe

- **XP Juice** — a fluid representation of player experience. Produced by Absorption
  Hoppers collecting XP orbs and stored in Tanks.
- **Experience Syringe** — a portable XP container to store your player levels for future
  use. Right-click to withdraw enough XP to complete your current level; shift+right-click to
  deposit your current level's progress back into the syringe.

### Ender Inhibitor

A torch-like block that mounts on any surface (floor, ceiling, or wall) and suppresses
all Enderman teleportation within an 8-block radius of itself.
Useful for keeping Endermen penned in a farm instead of teleporting away.

### Fan

A directional block that, when powered by redstone, pushes entities away from its face
in a configurable column. Three upgrade slots (up to 5 each) extend the push **width**,
**height**, and **distance** of the airflow — useful for funneling mobs or items toward
a collection point.

### Vector Plate

A thin, directional plate block. Any entity standing on it is continuously pushed
in the plate's facing direction and gently centered on the lane perpendicular to that
direction — useful for building mob-transport conveyors.

### Wither-Proof Glass

A transparent glass block that is immune to all explosions and cannot be destroyed
directly by the Wither.

### Mob Exclusion Glass

Behaves exactly like Wither-Proof Glass (explosion-proof, Wither-proof) **except
players can walk straight through it**, while it remains completely solid to every
other entity.

## Credits

Inspired by "Mob Grinding Utils": https://github.com/vadis365/Mob-Grinding-Utils
Dramatic Chipmunk Sound: https://notification-sounds.com/2298-dramatic-chipmunk.html -- License: Creative Commons
Red Alert Sound: Red Alert_Nuclear_Buzzer.mp3 by imagery2 -- https://freesound.org/s/458570/ -- License: Creative Commons 0
Rimshot Sound: Rimshot Joke Funny by deleted_user_7146007 -- https://freesound.org/s/383898/ -- License: Creative Commons 0
Wilhelm Scream: https://notification-sounds.com/784-wilhelm-scream.html -- License: Creative Commons

## License

[MIT](LICENSE) © 2026 BoboFraggins
