package net.bobofraggins.mobfarmingsupplies.register;

import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.RegistrySupplier;
import net.bobofraggins.mobfarmingsupplies.dna.DnaSamplePackItem;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Registers optional DNA Booster Pack items that are only present when the
 * matching third-party mod is loaded.
 *
 * <p>Each pack field is {@code null} when its mod is absent, so callers must
 * null-check before using it (e.g. in the creative tab).
 *
 * <p>{@link #register()} must be called before {@link Registration#register()}
 * fires so entries are included in the DeferredRegister submission.
 */
public final class ModCompatRegistration {

    private ModCompatRegistration() {}

    // ── Registered item handles (null when the mod is absent) ────────────────────

    @Nullable public static RegistrySupplier<DnaSamplePackItem> DNA_BOOSTER_AQUACULTURE;
    @Nullable public static RegistrySupplier<DnaSamplePackItem> DNA_BOOSTER_AETHER_PASSIVE;
    @Nullable public static RegistrySupplier<DnaSamplePackItem> DNA_BOOSTER_AETHER_HOSTILE;
    @Nullable public static RegistrySupplier<DnaSamplePackItem> DNA_BOOSTER_EVILCRAFT;

    // ── Mob pools ─────────────────────────────────────────────────────────────────

    private static final List<String> AQUACULTURE_MOBS = List.of(
            "aquaculture:arapaima", "aquaculture:arrau_turtle", "aquaculture:atlantic_cod",
            "aquaculture:atlantic_halibut", "aquaculture:atlantic_herring", "aquaculture:bayad",
            "aquaculture:blackfish", "aquaculture:bluegill", "aquaculture:boulti",
            "aquaculture:box_turtle", "aquaculture:brown_shrooma", "aquaculture:brown_trout",
            "aquaculture:capitaine", "aquaculture:carp", "aquaculture:catfish",
            "aquaculture:gar", "aquaculture:jellyfish", "aquaculture:largemouth_bass",
            "aquaculture:minnow", "aquaculture:muskellunge", "aquaculture:pacific_halibut",
            "aquaculture:perch", "aquaculture:pink_salmon", "aquaculture:piranha",
            "aquaculture:pollock", "aquaculture:rainbow_trout", "aquaculture:red_grouper",
            "aquaculture:red_shrooma", "aquaculture:smallmouth_bass",
            "aquaculture:starshell_turtle", "aquaculture:synodontis",
            "aquaculture:tambaqui", "aquaculture:tuna"
    );

    private static final List<String> AETHER_PASSIVE_MOBS = List.of(
            "aether_ii:flying_cow", "aether_ii:phyg", "aether_ii:sheepuff",
            "aether_ii:arctic_kirrid", "aether_ii:highfields_kirrid", "aether_ii:magnetic_kirrid",
            "aether_ii:arctic_burrukai", "aether_ii:highfields_burrukai", "aether_ii:magnetic_burrukai",
            "aether_ii:arctic_taegore", "aether_ii:highfields_taegore", "aether_ii:magnetic_taegore"
    );

    private static final List<String> AETHER_HOSTILE_MOBS = List.of(
            "aether_ii:aechor_plant", "aether_ii:carrion_sprout", "aether_ii:cockatrice",
            "aether_ii:blue_swet", "aether_ii:golden_swet", "aether_ii:skephid",
            "aether_ii:tempest", "aether_ii:zephyr"
    );

    private static final List<String> EVILCRAFT_MOBS = List.of(
            "evilcraft:poisonous_libelle", "evilcraft:werewolf"
    );

    // ── Registration ──────────────────────────────────────────────────────────────

    public static void register() {
        if (Platform.isModLoaded("aquaculture")) {
            DNA_BOOSTER_AQUACULTURE = Registration.ITEMS.register(
                    "dna_booster_pack_aquaculture",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(Registration.itemKey("dna_booster_pack_aquaculture"))
                            .stacksTo(1), () -> AQUACULTURE_MOBS));
        }

        if (Platform.isModLoaded("aether_ii")) {
            DNA_BOOSTER_AETHER_PASSIVE = Registration.ITEMS.register(
                    "dna_booster_pack_aether_passive",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(Registration.itemKey("dna_booster_pack_aether_passive"))
                            .stacksTo(1), () -> AETHER_PASSIVE_MOBS));
            DNA_BOOSTER_AETHER_HOSTILE = Registration.ITEMS.register(
                    "dna_booster_pack_aether_hostile",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(Registration.itemKey("dna_booster_pack_aether_hostile"))
                            .stacksTo(1), () -> AETHER_HOSTILE_MOBS));
        }

        if (Platform.isModLoaded("evilcraft")) {
            DNA_BOOSTER_EVILCRAFT = Registration.ITEMS.register(
                    "dna_booster_pack_evilcraft",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(Registration.itemKey("dna_booster_pack_evilcraft"))
                            .stacksTo(1), () -> EVILCRAFT_MOBS));
        }
    }
}
