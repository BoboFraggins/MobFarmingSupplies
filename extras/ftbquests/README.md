# Mob Farming Supplies: sample FTB Quests chapter

A ready-made FTB Quests chapter for modpack authors. It has 17 quests that mirror the mod's
advancements: a starting quest, then three lines (Logistics, Mob Farming, Fun Stuff). Each quest
is completed by its matching advancement, so it needs no extra setup, and each has a short how-to
description.

Built for the FTB Quests `.json5` format (FTB Quests 26.x).

## Installing

Copy the `quests` folder into your pack's `config/ftbquests/`, merging with what's there:

```
config/ftbquests/quests/chapters/mob_farming_supplies.json5
config/ftbquests/quests/lang/en_us/chapters/mob_farming_supplies.json5
```

That's it. FTB Quests merges every language file under `lang/en_us/`, so the chapter title needs
no edits to your other files. Reload with `/ftbquests reload` or restart.

## Customising

- **Rewards** are XP only (10 for the first quest, 25 per task, 50 per goal, 100 per challenge).
  Swap in your own in the quest editor.
- **Chapter group and order**: the chapter isn't in a group (`group: ""`) and has `order_index: 0`.
  Move it in the editor.
- **Text** is all in the language file. To translate it, copy that file to another locale folder
  (e.g. `lang/fr_fr/chapters/`).

## Notes for players

Some quests are credited to every player within 16 blocks of where something happens, because the
blocks don't know who placed them:
- Spooky Action at a Distance (a bridge delivery into another dimension)
- Send in the Clones (a Clone-O-Matic spawn)
- Heads Will Roll (a head dropping)
- Royal Flush (a diamond going down a Toilet)

Hat Trick needs the rabbit released within 3 blocks of a villager.
