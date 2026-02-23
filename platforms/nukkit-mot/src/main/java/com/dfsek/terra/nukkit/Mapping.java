package com.dfsek.terra.nukkit;

import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.leveldb.BlockStateMapping;
import cn.nukkit.level.format.leveldb.NukkitLegacyMapper;
import cn.nukkit.level.format.leveldb.structure.BlockStateSnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;


public final class Mapping {
    private static final Logger LOGGER = LoggerFactory.getLogger(Mapping.class);

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapterFactory(new IgnoreFailureTypeAdapterFactory())
        .create();

    private static final Map<String, Map<String, String>> JE_BLOCK_DEFAULT_PROPERTIES = new HashMap<>();
    private static final Map<Integer, JeBlockState> BE_FULL_ID_TO_JE = new HashMap<>();
    private static final Map<Integer, NukkitBlockData> JE_BLOCK_STATE_HASH_TO_BE = new HashMap<>();
    private static final Map<String, NukkitItemData> JE_ITEM_ID_TO_BE = new HashMap<>();
    private static final Map<String, Integer> JE_BIOME_ID_TO_BE = new HashMap<>();

    private static int blockStateVersion;

    public static void init() {
        if(!initBlockStateMapping()) error();
        if(!initJeBlockDefaultProperties()) error();
        if(!initItemMapping()) error();
        if(!initBiomeMapping()) error();
    }

    public static JeBlockState blockFullIdToJe(int fullId) {
        return BE_FULL_ID_TO_JE.get(fullId);
    }

    public static NukkitBlockData blockStateJeToBe(JeBlockState jeBlockState) {
        NukkitBlockData result = JE_BLOCK_STATE_HASH_TO_BE.get(jeBlockState.getHash());
        if(result == null) {
            LOGGER.warn("Failed to find BE block state for {}", jeBlockState);
            return NukkitBlockData.AIR;
        }
        return result;
    }

    public static NukkitItemData itemIdJeToBe(String jeItemId) {
        NukkitItemData result = JE_ITEM_ID_TO_BE.get(jeItemId);
        if(result == null) {
            LOGGER.warn("Failed to find BE item for {}", jeItemId);
            return new NukkitItemData(0, 0);
        }
        return result;
    }

    public static int biomeIdJeToBe(String jeBiomeId) {
        Integer result = JE_BIOME_ID_TO_BE.get(jeBiomeId);
        if(result == null) {
            LOGGER.warn("Failed to find BE biome for {}", jeBiomeId);
            return 1; // plains
        }
        return result;
    }

    public static Map<String, String> getJeBlockDefaultProperties(String jeBlockIdentifier) {
        var defaultProperties = JE_BLOCK_DEFAULT_PROPERTIES.get(jeBlockIdentifier);
        if(defaultProperties == null) {
            return Map.of();
        }
        return defaultProperties;
    }

    private static void error() {
        throw new RuntimeException("Mapping not initialized");
    }

    private static boolean initBiomeMapping() {
        try(InputStream stream = Mapping.class.getClassLoader().getResourceAsStream("mapping/biomes.json")) {
            if(stream == null) {
                LOGGER.error("biomes mapping not found");
                return false;
            }

            Map<String, BiomeMapping> mappings = from(stream, new TypeToken<>() {
            });
            mappings.forEach((javaId, mapping) -> JE_BIOME_ID_TO_BE.put(javaId, mapping.bedrockId()));
        } catch(IOException e) {
            LOGGER.error("Failed to load biomes mapping", e);
            return false;
        }
        return true;
    }

    private static boolean initItemMapping() {
        try(InputStream stream = Mapping.class.getClassLoader().getResourceAsStream("mapping/items.json")) {
            if(stream == null) {
                LOGGER.error("items mapping not found");
                return false;
            }

            Map<String, ItemMapping> mappings = from(stream, new TypeToken<>() {
            });
            mappings.forEach((javaId, mapping) -> {
                Item nukkitItem = Item.fromString(mapping.bedrockId());
                if(nukkitItem != null) {
                    JE_ITEM_ID_TO_BE.put(javaId, new NukkitItemData(nukkitItem.getId(), mapping.bedrockData()));
                }
            });
        } catch(IOException e) {
            LOGGER.error("Failed to load items mapping", e);
            return false;
        }
        return true;
    }

    private static boolean initBlockStateMapping() {
        // Extract block state version from palette to match paletteMap key structure
        List<NbtMap> palette = NukkitLegacyMapper.loadBlockPalette();
        if(!palette.isEmpty()) {
            blockStateVersion = palette.getFirst().getInt("version");
            LOGGER.info("Block state version: {}", blockStateVersion);
        }

        try(InputStream stream = Mapping.class.getClassLoader().getResourceAsStream("mapping/blocks.json")) {
            if(stream == null) {
                LOGGER.error("blocks mapping not found");
                return false;
            }

            Map<String, List<BlockMapping>> root = from(stream, new TypeToken<>() {
            });
            List<BlockMapping> mappings = root.get("mappings");
            int mapped = 0;
            int failed = 0;
            for(BlockMapping mapping : mappings) {
                JeBlockState jeState = createJeBlockState(mapping.javaState());
                NukkitBlockData beData = createNukkitBlockData(mapping.bedrockState());
                if(beData != null) {
                    JE_BLOCK_STATE_HASH_TO_BE.put(jeState.getHash(), beData);
                    int fullId = (beData.blockId() << Block.DATA_BITS) | beData.metadata();
                    BE_FULL_ID_TO_JE.put(fullId, jeState);
                    mapped++;
                } else {
                    failed++;
                }
            }
            LOGGER.info("Block state mapping loaded: {} mapped, {} failed", mapped, failed);
        } catch(IOException e) {
            LOGGER.error("Failed to load blocks mapping", e);
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static boolean initJeBlockDefaultProperties() {
        try(InputStream stream = Mapping.class.getClassLoader().getResourceAsStream("je_blocks.json")) {
            if(stream == null) {
                LOGGER.error("je_blocks.json not found");
                return false;
            }

            Map<String, List<Map<String, ?>>> data = from(stream, new TypeToken<>() {
            });
            for(var entry : data.entrySet()) {
                JE_BLOCK_DEFAULT_PROPERTIES.put(
                    "minecraft:" + entry.getKey(),
                    (Map<String, String>) entry.getValue().get(1)
                );
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private static JeBlockState createJeBlockState(BlockMapping.JavaState state) {
        Map<String, String> properties = state.properties() == null ? Map.of() : state.properties();
        return JeBlockState.create(state.name(), new TreeMap<>(properties));
    }

    @Nullable
    private static NukkitBlockData createNukkitBlockData(BlockMapping.BedrockState state) {
        try {
            String beName = "minecraft:" + state.bedrockId();
            NbtMapBuilder statesBuilder = NbtMap.builder();
            if(state.state() != null) {
                for(Entry<String, Object> entry : state.state().entrySet()) {
                    Object value = entry.getValue();
                    if(value instanceof Number number) {
                        statesBuilder.putInt(entry.getKey(), number.intValue());
                    } else if(value instanceof Boolean bool) {
                        statesBuilder.putByte(entry.getKey(), (byte) (bool ? 1 : 0));
                    } else {
                        statesBuilder.putString(entry.getKey(), value.toString());
                    }
                }
            }

            NbtMap nbtState = NbtMap.builder()
                .putString("name", beName)
                .putCompound("states", statesBuilder.build())
                .putInt("version", blockStateVersion)
                .build();

            BlockStateSnapshot snapshot = BlockStateMapping.get().getStateUnsafe(nbtState);
            if(snapshot == null) {
                return null;
            }

            int legacyId = snapshot.getLegacyId();
            int legacyData = snapshot.getLegacyData();
            if(legacyId == -1) {
                return null;
            }
            return new NukkitBlockData(legacyId, legacyData);
        } catch(Exception e) {
            return null;
        }
    }

    public static <V> V from(InputStream inputStream, TypeToken<V> typeToken) {
        JsonReader reader = new JsonReader(new InputStreamReader(Objects.requireNonNull(inputStream)));
        return GSON.fromJson(reader, typeToken.getType());
    }

    public record NukkitBlockData(int blockId, int metadata) {
        public static final NukkitBlockData AIR = new NukkitBlockData(0, 0);
    }

    public record NukkitItemData(int itemId, int metadata) {
    }

    public record BiomeMapping(
        @SerializedName("bedrock_id")
        int bedrockId
    ) {
    }

    public record ItemMapping(
        @SerializedName("bedrock_identifier")
        String bedrockId,
        @SerializedName("bedrock_data")
        int bedrockData
    ) {
    }

    public record BlockMapping(
        @SerializedName("java_state")
        BlockMapping.JavaState javaState,
        @SerializedName("bedrock_state")
        BlockMapping.BedrockState bedrockState
    ) {
        public record JavaState(
            @SerializedName("Name")
            String name,
            @Nullable
            @SerializedName("Properties")
            Map<String, String> properties
        ) {
        }

        public record BedrockState(
            @SerializedName("bedrock_identifier")
            String bedrockId,
            @Nullable
            Map<String, Object> state
        ) {
        }
    }

    public static class IgnoreFailureTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            TypeAdapter<T> delegate = gson.getDelegateAdapter(this, typeToken);
            return new TypeAdapter<>() {
                @Override
                public void write(JsonWriter writer, T value) throws IOException {
                    delegate.write(writer, value);
                }

                @Override
                public T read(JsonReader reader) throws IOException {
                    try {
                        return delegate.read(reader);
                    } catch(Exception e) {
                        reader.skipValue();
                        return null;
                    }
                }
            };
        }
    }
}
