package com.dfsek.terra.nukkit;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.level.LevelUnloadEvent;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.plugin.PluginBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dfsek.terra.api.event.events.platform.PlatformInitializationEvent;
import com.dfsek.terra.nukkit.generator.TerraGenerator;


public class TerraNukkitPlugin extends PluginBase implements Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerraNukkitPlugin.class);

    public static TerraNukkitPlugin instance;
    public static NukkitPlatform platform;

    @Override
    public void onLoad() {
        instance = this;

        LOGGER.info("Starting Terra...");

        LOGGER.info("Loading mapping...");
        Mapping.init();

        LOGGER.info("Initializing Nukkit-MOT platform...");
        platform = new NukkitPlatform();
        platform.getEventManager().callEvent(new PlatformInitializationEvent());

        LOGGER.info("Registering generator...");
        Generator.addGenerator(TerraGenerator.class, "terra", Generator.TYPE_INFINITE);

        LOGGER.info("Terra started");
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        LOGGER.info("Terra enabled");
    }

    @Override
    public void onDisable() {
        LOGGER.info("Terra disabled");
    }

    @EventHandler
    private void onLevelUnload(LevelUnloadEvent event) {
        NukkitPlatform.GENERATORS.removeIf(
            generator -> generator.getChunkManager() == event.getLevel());
    }
}
