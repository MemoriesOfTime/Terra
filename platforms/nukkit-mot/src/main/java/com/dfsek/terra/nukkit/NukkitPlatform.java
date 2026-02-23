package com.dfsek.terra.nukkit;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import com.dfsek.tectonic.api.TypeRegistry;
import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.exception.LoadException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.dfsek.terra.AbstractPlatform;
import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.handle.ItemHandle;
import com.dfsek.terra.api.handle.WorldHandle;
import com.dfsek.terra.api.world.biome.PlatformBiome;
import com.dfsek.terra.nukkit.delegate.NukkitBiome;
import com.dfsek.terra.nukkit.generator.TerraGenerator;
import com.dfsek.terra.nukkit.handle.NukkitItemHandle;
import com.dfsek.terra.nukkit.handle.NukkitWorldHandle;


public class NukkitPlatform extends AbstractPlatform {

    private static final Logger LOGGER = LoggerFactory.getLogger(NukkitPlatform.class);

    public static final Set<TerraGenerator> GENERATORS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static final NukkitWorldHandle WORLD_HANDLE = new NukkitWorldHandle();
    private static final NukkitItemHandle ITEM_HANDLE = new NukkitItemHandle();

    public NukkitPlatform() {
        load();
    }

    @Override
    public boolean reload() {
        getTerraConfig().load(this);
        boolean succeed = loadConfigPacks();

        GENERATORS.forEach(generator -> {
            if(generator.getConfigPack() == null) return;
            getConfigRegistry().get(generator.getConfigPack().getRegistryKey()).ifPresent(pack -> {
                generator.setConfigPack(pack);
                LOGGER.info("Replaced pack in generator for level: {}", generator.getName());
            });
        });

        return succeed;
    }

    @Override
    public @NotNull String platformName() {
        return "Nukkit-MOT";
    }

    @Override
    public @NotNull WorldHandle getWorldHandle() {
        return WORLD_HANDLE;
    }

    @Override
    public @NotNull ItemHandle getItemHandle() {
        return ITEM_HANDLE;
    }

    @Override
    public @NotNull File getDataFolder() {
        return TerraNukkitPlugin.instance.getDataFolder();
    }

    @Override
    public void runPossiblyUnsafeTask(@NotNull Runnable task) {
        PluginBase plugin = TerraNukkitPlugin.instance;
        Server.getInstance().getScheduler().scheduleDelayedTask(plugin, task, 1);
    }

    @Override
    public void register(TypeRegistry registry) {
        super.register(registry);
        registry.registerLoader(BlockState.class, (type, o, loader, depthTracker) -> WORLD_HANDLE.createBlockState((String) o))
            .registerLoader(PlatformBiome.class, (type, o, loader, depthTracker) -> parseBiome((String) o, depthTracker));
    }

    private NukkitBiome parseBiome(String id, DepthTracker depthTracker) throws LoadException {
        if(!id.startsWith("minecraft:")) throw new LoadException("Invalid biome identifier " + id, depthTracker);
        return new NukkitBiome(Mapping.biomeIdJeToBe(id));
    }
}
