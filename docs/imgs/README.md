# Block images

These are 256x256 transparent PNGs of each block in the mod, drawn from the same angle and with
the same lighting that JEI and the creative tab use. Each block fills 80% of the image along its
larger side and is centred. Files are named after the item, for example `tank.png`.

## Regenerating them

The images are rendered by the game itself, using a development tool built into the mod
(`BlockIconRenderer` in `common/.../client/devtools`). It only runs in the development client, and
only when the `MFS_RENDER_ICONS` environment variable names an output folder.

Items can only be rendered once a world is loaded, so the tool waits for one. The first time, start
the development client normally with `./gradlew :neoforge:runClient` and create a world named
`icons`. After that, run this from the repository root:

```sh
MFS_RENDER_ICONS="$PWD/docs/imgs" ./gradlew :neoforge:runClient --args="--quickPlaySingleplayer icons"
```

On Windows PowerShell, set the variable first:

```powershell
$env:MFS_RENDER_ICONS = "$PWD\docs\imgs"; ./gradlew :neoforge:runClient --args="--quickPlaySingleplayer icons"
```

The game opens the world, renders every block item in the mod, writes the images, and closes. The
log lists each file it wrote. Existing images with the same name are replaced.

## Rendering particular items

To render only some items, list their ids in `MFS_RENDER_ICONS_ITEMS`, separated by commas. Any
item works, including other mods' items and plain items such as `mobfarmingsupplies:smore`:

```sh
MFS_RENDER_ICONS="$PWD/docs/imgs" MFS_RENDER_ICONS_ITEMS="mobfarmingsupplies:tank,mobfarmingsupplies:smore" \
  ./gradlew :neoforge:runClient --args="--quickPlaySingleplayer icons"
```

Mob Heads are left out of the default set and can't be listed this way, because a Mob Head needs a
mob type that an item id alone doesn't carry.

## Changing the size

The image size and how much of it the block fills are the `SIZE` and `FILL` constants in
`BlockIconRenderer`. Blocks are rendered at 1024x1024 and scaled down, so sizes up to that render
cleanly.
