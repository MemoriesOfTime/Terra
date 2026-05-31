package com.dfsek.terra.nukkit.generator;

import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.DimensionData;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.world.biome.generation.BiomeProvider;
import com.dfsek.terra.api.world.chunk.generation.ChunkGenerator;
import com.dfsek.terra.api.world.chunk.generation.stage.GenerationStage;
import com.dfsek.terra.nukkit.NukkitPlatform;
import com.dfsek.terra.nukkit.TerraNukkitPlugin;
import com.dfsek.terra.nukkit.delegate.NukkitBiome;
import com.dfsek.terra.nukkit.delegate.NukkitProtoChunk;
import com.dfsek.terra.nukkit.delegate.NukkitProtoWorld;
import com.dfsek.terra.nukkit.delegate.NukkitServerWorld;
import com.dfsek.terra.nukkit.delegate.NukkitWorldAccess;
import com.dfsek.terra.nukkit.delegate.NukkitWorldProperties;


public class TerraGenerator extends Generator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerraGenerator.class);

    private final Map<String, Object> options;

    private ChunkManager chunkManager;
    private long seed;

    private ConfigPack configPack;
    private ChunkGenerator terraGenerator;
    private BiomeProvider biomeProvider;
    private NukkitWorldProperties worldProperties;
    private NukkitServerWorld serverWorld;
    private DimensionData dimensionData;

    public TerraGenerator(Map<String, Object> options) {
        this.options = options;
    }

    @Override
    public void init(ChunkManager level, NukkitRandom random) {
        this.chunkManager = level;
        this.seed = level.getSeed();
        this.dimensionData = getDimensionData();
        this.worldProperties = new NukkitWorldProperties(seed, dimensionData.getMinHeight(), NukkitWorldAccess.terraMaxHeight(dimensionData));
        this.serverWorld = new NukkitServerWorld(this, chunkManager, dimensionData);

        NukkitPlatform platform = TerraNukkitPlugin.platform;
        if(platform == null) {
            LOGGER.error("Terra platform not initialized!");
            return;
        }

        String packName = getPackName();
        if(packName != null && !packName.isEmpty()) {
            this.configPack = platform.getConfigRegistry()
                .getByID(packName)
                .orElse(null);
            if(this.configPack == null) {
                LOGGER.warn("Config pack '{}' not found", packName);
            }
        }

        if(this.configPack == null) {
            // Use first available pack
            var iterator = platform.getConfigRegistry().entries().iterator();
            if(iterator.hasNext()) {
                this.configPack = iterator.next();
                LOGGER.info("Using default config pack: {}", configPack.getRegistryKey());
            } else {
                LOGGER.error("No Terra config packs available!");
                return;
            }
        }

        this.terraGenerator = configPack.getGeneratorProvider().newInstance(configPack);
        this.biomeProvider = configPack.getBiomeProvider();

        NukkitPlatform.GENERATORS.add(this);
        LOGGER.info("Terra generator initialized with pack: {}", configPack.getRegistryKey());
    }

    public void setConfigPack(ConfigPack configPack) {
        this.configPack = configPack;
        this.terraGenerator = configPack.getGeneratorProvider().newInstance(configPack);
        this.biomeProvider = configPack.getBiomeProvider();
    }

    @Override
    public void generateChunk(int chunkX, int chunkZ) {
        if(terraGenerator == null) return;

        ChunkManager cm = getChunkManager();
        FullChunk chunk = cm.getChunk(chunkX, chunkZ);
        if(chunk == null) return;

        NukkitProtoChunk protoChunk = new NukkitProtoChunk(chunk, dimensionData);
        terraGenerator.generateChunkData(protoChunk, worldProperties, biomeProvider, chunkX, chunkZ);

        // Set biomes
        int minHeight = dimensionData.getMinHeight();
        int maxHeight = NukkitWorldAccess.terraMaxHeight(dimensionData);
        for(int x = 0; x < 16; x++) {
            for(int y = minHeight; y < maxHeight; y++) {
                for(int z = 0; z < 16; z++) {
                    NukkitBiome biome = (NukkitBiome) biomeProvider.getBiome(
                        chunkX * 16 + x, y, chunkZ * 16 + z, seed).getPlatformBiome();
                    chunk.setBiomeId(x, y, z, biome.biomeId());
                }
            }
        }
    }

    @Override
    public void populateChunk(int chunkX, int chunkZ) {
        if(configPack == null) return;

        ChunkManager cm = getChunkManager();
        NukkitServerWorld currentWorld = new NukkitServerWorld(this, cm, dimensionData);
        NukkitProtoWorld protoWorld = new NukkitProtoWorld(
            currentWorld, cm, chunkX, chunkZ, dimensionData);

        try {
            for(GenerationStage stage : configPack.getStages()) {
                stage.populate(protoWorld);
            }
        } catch(Exception e) {
            LOGGER.error("Error while populating chunk ({}, {})", chunkX, chunkZ, e);
        }
    }

    @Override
    public void populateStructure(int chunkX, int chunkZ) {
        // Structures are handled in populateChunk stages
    }

    @Override
    public int getDimension() {
        return getDimensionSelection().dimension();
    }

    @Override
    public int getId() {
        return getDimensionSelection().generatorType();
    }

    @Override
    public Map<String, Object> getSettings() {
        return options;
    }

    @Override
    public String getName() {
        return "terra";
    }

    @Override
    public Vector3 getSpawn() {
        return new Vector3(0, 64, 0);
    }

    @Override
    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    public ChunkGenerator getTerraGenerator() {
        return terraGenerator;
    }

    public BiomeProvider getTerraBiomeProvider() {
        return biomeProvider;
    }

    public ConfigPack getConfigPack() {
        return configPack;
    }

    private DimensionSelection getDimensionSelection() {
        return switch(normalizePackName(getPackName())) {
            case "TARTARUS" -> new DimensionSelection(Level.DIMENSION_NETHER, TYPE_NETHER);
            case "REIMAGEND" -> new DimensionSelection(Level.DIMENSION_THE_END, TYPE_THE_END);
            default -> new DimensionSelection(Level.DIMENSION_OVERWORLD, TYPE_INFINITE);
        };
    }

    /**
     * Nukkit-MOT stores generator-settings as options.get("preset").
     * Supports both direct pack name and JSON format {"pack":"name"}.
     */
    private String getPackName() {
        if(options == null) return null;

        // Nukkit-MOT puts generator-settings value into "preset" key
        String preset = options.get("preset") instanceof String s ? s : null;
        if(preset != null && !preset.isEmpty()) {
            String trimmed = preset.trim();
            if(trimmed.startsWith("{")) {
                // JSON format: {"pack":"Overworld"}
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> json = new com.google.gson.Gson().fromJson(trimmed, Map.class);
                    Object pack = json.get("pack");
                    if(pack instanceof String s2) return normalizePackName(s2);
                } catch(Exception e) {
                    LOGGER.warn("Failed to parse generator-settings as JSON: {}", preset);
                }
            } else {
                // Direct pack name: Overworld
                return normalizePackName(trimmed);
            }
        }

        // Fallback: try legacy "pack" key
        return options.get("pack") instanceof String s ? normalizePackName(s) : null;
    }

    private String normalizePackName(String packName) {
        if(packName == null) return "";
        return packName.trim();
    }

    private record DimensionSelection(int dimension, int generatorType) {
    }
}
