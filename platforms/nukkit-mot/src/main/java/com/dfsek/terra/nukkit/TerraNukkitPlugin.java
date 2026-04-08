package com.dfsek.terra.nukkit;

import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.level.LevelUnloadEvent;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.plugin.PluginBase;

import com.dfsek.terra.nukkit.config.MyConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dfsek.terra.api.event.events.platform.PlatformInitializationEvent;
import com.dfsek.terra.nukkit.generator.TerraGenerator;

import static com.dfsek.terra.nukkit.config.MyConfig.BLOCK_REPLACEMENTS_SOURCE;
import static com.dfsek.terra.nukkit.config.MyConfig.loadConfig;


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
        loadConfig();
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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(MyConfig.DEBUG) {
            var block = event.getBlock();
            int fullId = (block.getId() << Block.DATA_BITS) | block.getDamage();
            JeBlockState jeState = Mapping.blockFullIdToJe(fullId);
            String msg = String.format("[Debug] 破坏方块: %s (fullId=%d, id=%d, damage=%d)",
                    jeState != null ? jeState.toString() : "未知",
                    fullId,
                    block.getId(),
                    block.getDamage());
            event.getPlayer().sendMessage(msg);
            this.getLogger().info(msg);
            event.setCancelled(true);

            // 快速添加到 BLOCK_REPLACEMENTS（替换为 minecraft:air）并保存配置
            if(jeState != null) {
                String jeKey = jeState.toString().split(";")[0];
                if(BLOCK_REPLACEMENTS_SOURCE.containsKey(jeKey)) {
                    return;
                }
                BLOCK_REPLACEMENTS_SOURCE.put(jeKey, "minecraft:air");
                MyConfig.saveConfig();
                MyConfig.applyBlockReplacements();
                String addMsg = String.format("[Debug] 已添加替换规则: %s -> minecraft:air 并保存配置", jeKey);
                event.getPlayer().sendMessage(addMsg);
                this.getLogger().info(addMsg);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("terradebug")) {
            return false;
        }

        boolean newValue;
        if (args.length > 0) {
            String arg = args[0].toLowerCase();
            if ("on".equals(arg) || "true".equals(arg)) {
                newValue = true;
            } else if ("off".equals(arg) || "false".equals(arg)) {
                newValue = false;
            } else {
                sender.sendMessage("§c用法: /terradebug [on|off]");
                return true;
            }
        } else {
            // 无参数时切换状态
            newValue = !MyConfig.DEBUG;
        }
        MyConfig.DEBUG = newValue;
        MyConfig.saveConfig();
        String status = newValue ? "§a开启" : "§c关闭";
        String msg = "§7[Terra] 调试模式已" + status + " §7(当前: " + status + ")";
        sender.sendMessage(msg);
        LOGGER.info("调试模式已{}, 操作者: {}", newValue ? "开启" : "关闭", sender.getName());
        return true;
    }

}
