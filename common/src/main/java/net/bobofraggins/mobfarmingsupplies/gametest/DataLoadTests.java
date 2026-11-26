package net.bobofraggins.mobfarmingsupplies.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.platform.Platform;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Every data file the mod ships actually loaded. Minecraft only logs a broken recipe, loot table,
 * advancement or tag and carries on without it, which is easy to miss.
 *
 * <p>Reads the files straight out of the mod (its jar, or its resources folder in development) and
 * asks the server for each one. Files that only load when another mod is present (Forge
 * {@code conditions}, Fabric {@code fabric:load_conditions}) are skipped.
 */
final class DataLoadTests {

    private DataLoadTests() {}

    static List<TestFunction> all() {
        return List.of(
                GameTestKit.test("data", "recipes", 20, h -> check(h, "recipes",
                        (server, id) -> server.getRecipeManager().byKey(id).isPresent())),
                GameTestKit.test("data", "loot_tables", 20, h -> check(h, "loot_tables",
                        (server, id) -> server.getLootData().getLootTable(id) != LootTable.EMPTY)),
                GameTestKit.test("data", "advancements", 20, h -> check(h, "advancements",
                        (server, id) -> server.getAdvancements().getAdvancement(id) != null)),
                GameTestKit.test("data", "tags", 20, DataLoadTests::checkTags));
    }

    private interface Loaded {
        boolean test(MinecraftServer server, ResourceLocation id);
    }

    /** {@code data/<namespace>/<folder>/<path>.json} for every namespace the mod ships files in. */
    private static void check(GameTestHelper helper, String folder, Loaded loaded) {
        MinecraftServer server = helper.getLevel().getServer();
        GameTestKit.Problems problems = new GameTestKit.Problems();
        int checked = 0;
        for (DataFile file : files()) {
            if (!file.folder().equals(folder) || file.conditional()) continue;
            checked++;
            if (!loaded.test(server, file.id())) problems.add(folder + " " + file.id() + " did not load");
        }
        if (checked == 0) problems.add("found no " + folder + " files in the mod");
        problems.finish(helper);
    }

    private static void checkTags(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        GameTestKit.Problems problems = new GameTestKit.Problems();
        List<Registry<?>> registries = new ArrayList<>();
        server.registryAccess().registries().forEach(e -> registries.add(e.value()));
        int checked = 0;
        for (DataFile file : files()) {
            if (!file.folder().equals("tags")) continue;
            // file.id() path is "<registry dir>/<tag path>", e.g. "items/skulls" or "worldgen/biome/x".
            Registry<?> registry = null;
            String tagPath = null;
            for (Registry<?> candidate : registries) {
                String dir = TagManager.getTagDir(candidate.key()).substring("tags/".length()) + "/";
                if (file.id().getPath().startsWith(dir)) {
                    registry = candidate;
                    tagPath = file.id().getPath().substring(dir.length());
                    break;
                }
            }
            if (registry == null) {
                problems.add("tag file " + file.id() + " is in no registry's tag folder");
                continue;
            }
            checked++;
            if (!hasTag(registry, new ResourceLocation(file.id().getNamespace(), tagPath))) {
                problems.add("tag #" + file.id().getNamespace() + ":" + tagPath + " (" + registry.key().location()
                        + ") did not load");
            }
        }
        if (checked == 0) problems.add("found no tag files in the mod");
        problems.finish(helper);
    }

    private static <T> boolean hasTag(Registry<T> registry, ResourceLocation id) {
        return registry.getTag(TagKey.create(registry.key(), id)).isPresent();
    }

    /** One JSON file under {@code data/}: its folder ("recipes"), id, and whether it has load conditions. */
    record DataFile(String folder, ResourceLocation id, boolean conditional) {}

    private static List<DataFile> cache;

    static synchronized List<DataFile> files() {
        if (cache != null) return cache;
        List<DataFile> out = new ArrayList<>();
        for (Path data : dataRoots()) {
            try (Stream<Path> walk = Files.walk(data)) {
                for (Path path : (Iterable<Path>) walk.filter(p -> p.toString().endsWith(".json"))::iterator) {
                    Path rel = data.relativize(path);
                    if (rel.getNameCount() < 3) continue;
                    String namespace = rel.getName(0).toString();
                    String folder = rel.getName(1).toString();
                    String rest = rel.subpath(2, rel.getNameCount()).toString().replace('\\', '/');
                    rest = rest.substring(0, rest.length() - ".json".length());
                    ResourceLocation id = ResourceLocation.tryBuild(namespace, rest);
                    if (id != null) out.add(new DataFile(folder, id, hasConditions(path)));
                }
            } catch (IOException e) {
                throw new IllegalStateException("reading the mod's data files in " + data, e);
            }
        }
        cache = List.copyOf(out);
        return cache;
    }

    /**
     * Every {@code data} folder of the mod. In development the common and loader resources are
     * separate folders (Architectury's {@code findResource} only sees the loader's); in a jar there is one.
     */
    private static List<Path> dataRoots() {
        Set<Path> roots = new LinkedHashSet<>();
        try {
            Enumeration<URL> urls = DataLoadTests.class.getClassLoader().getResources("data/" + GameTestKit.MODID);
            while (urls.hasMoreElements()) {
                Path ours = Path.of(urls.nextElement().toURI());
                roots.add(ours.getParent());
            }
        } catch (IOException | URISyntaxException | RuntimeException e) {
            throw new IllegalStateException("finding the mod's data folders", e);
        }
        Platform.getMod(GameTestKit.MODID).findResource("data").ifPresent(roots::add);
        if (roots.isEmpty()) throw new IllegalStateException("the mod has no data folder");
        return List.copyOf(roots);
    }

    private static boolean hasConditions(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            var element = JsonParser.parseReader(reader);
            if (!element.isJsonObject()) return false;
            JsonObject json = element.getAsJsonObject();
            return json.has("conditions") || json.has("forge:conditions") || json.has("fabric:load_conditions");
        } catch (Exception e) {
            return false; // a file that doesn't parse is reported as not loaded
        }
    }

}
