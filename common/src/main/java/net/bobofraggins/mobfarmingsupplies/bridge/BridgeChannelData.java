package net.bobofraggins.mobfarmingsupplies.bridge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * The settings Einstein-Rosen Bridges share per channel — kept "in the aether" (the world's saved
 * data), not in any block: the nine Item Filters and the AND / OR mode. Side configuration is
 * per block and lives in each bridge's block entity.
 */
public final class BridgeChannelData extends SavedData {

    public static final int FILTER_SLOTS = 9;

    /** One channel's shared settings. The filter container is edited directly by bridge menus. */
    public final class Settings {
        public final SimpleContainer filters = new SimpleContainer(FILTER_SLOTS) {
            @Override
            public void setChanged() {
                super.setChanged();
                BridgeChannelData.this.setDirty();
            }
        };
        private boolean andMode = true;

        public boolean andMode() { return andMode; }

        public void setAndMode(boolean andMode) {
            if (this.andMode == andMode) return;
            this.andMode = andMode;
            setDirty();
        }
    }

    private record Entry(int channel, boolean andMode, List<ItemStack> filters) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("channel").forGetter(Entry::channel),
                Codec.BOOL.optionalFieldOf("and_mode", true).forGetter(Entry::andMode),
                ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("filters", List.of()).forGetter(Entry::filters)
        ).apply(i, Entry::new));
    }

    private static final Codec<BridgeChannelData> CODEC = Entry.CODEC.listOf().xmap(
            BridgeChannelData::fromEntries, BridgeChannelData::toEntries);

    // Data fixers only touch data saved by older game versions; ours is a plain compound either way.
    private static final SavedDataType<BridgeChannelData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "einstein_rosen_bridges"),
            BridgeChannelData::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Map<Integer, Settings> channels = new HashMap<>();

    public static BridgeChannelData get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    /** The settings for {@code channel}, created with defaults the first time it's used. */
    public Settings settings(int channel) {
        return channels.computeIfAbsent(channel, c -> new Settings());
    }

    private static BridgeChannelData fromEntries(List<Entry> entries) {
        BridgeChannelData data = new BridgeChannelData();
        for (Entry e : entries) {
            Settings s = data.settings(e.channel());
            s.andMode = e.andMode();
            for (int i = 0; i < FILTER_SLOTS && i < e.filters().size(); i++) {
                s.filters.getItems().set(i, e.filters().get(i));
            }
        }
        return data;
    }

    private List<Entry> toEntries() {
        List<Entry> entries = new ArrayList<>();
        channels.forEach((channel, s) -> {
            if (s.andMode && s.filters.isEmpty()) return; // all defaults - nothing worth saving
            entries.add(new Entry(channel, s.andMode, new ArrayList<>(s.filters.getItems())));
        });
        return entries;
    }
}
