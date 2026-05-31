package com.dfsek.terra.nukkit.config;

import cn.nukkit.utils.Config;

import java.util.HashMap;
import java.util.Map;

import com.dfsek.terra.nukkit.JeBlockState;
import com.dfsek.terra.nukkit.Mapping;
import com.dfsek.terra.nukkit.delegate.NukkitBlockState;

import static com.dfsek.terra.nukkit.TerraNukkitPlugin.instance;
import static com.dfsek.terra.nukkit.delegate.NukkitBlockState.BLOCK_IDENTIFIER_REPLACEMENTS;
import static com.dfsek.terra.nukkit.delegate.NukkitBlockState.BLOCK_REPLACEMENTS;


public class MyConfig {


    public static boolean DEBUG = false;

    public static boolean BLOCK_REPLACEMENTS_ENABLED = true;

    public static Map<String, String> BLOCK_REPLACEMENTS_SOURCE = new HashMap<>();

    public static boolean loadConfig() {
        instance.saveResource("block_replacements.yml");
        Config config = new Config(instance.getDataFolder().getPath() + "/block_replacements.yml", Config.YAML);
        DEBUG = config.getBoolean("debug", false);
        BLOCK_REPLACEMENTS_ENABLED = config.getBoolean("block_replacements_enabled", true);
        BLOCK_REPLACEMENTS_SOURCE = config.get("block_replacements", new HashMap<>());
        applyBlockReplacements();
        saveConfig();
        return true;
    }
    public static void saveConfig() {
        Config config = new Config(instance.getDataFolder().getPath() + "/block_replacements.yml", Config.YAML);
        config.set("debug", DEBUG);
        config.set("block_replacements_enabled", BLOCK_REPLACEMENTS_ENABLED);
        config.set("block_replacements", BLOCK_REPLACEMENTS_SOURCE);
        config.save();
    }

    @SuppressWarnings("unchecked")
    public static void applyBlockReplacements() {
        BLOCK_REPLACEMENTS.clear();
        BLOCK_IDENTIFIER_REPLACEMENTS.clear();

        if(!BLOCK_REPLACEMENTS_ENABLED) {
            instance.getLogger().info("方块替换功能已禁用");
            return;
        }

        // 从配置中读取原始的字符串映射
        if(BLOCK_REPLACEMENTS_SOURCE == null || BLOCK_REPLACEMENTS_SOURCE.isEmpty()) {
            instance.getLogger().info("未配置方块替换规则");
            return;
        }
        int success = 0;
        int failed = 0;

        for(Map.Entry<String, String> entry : new HashMap<>(BLOCK_REPLACEMENTS_SOURCE).entrySet()) {
            try {
                JeBlockState srcJeState = JeBlockState.fromString(entry.getKey());
                JeBlockState dstJeState = JeBlockState.fromString(entry.getValue());
                Mapping.NukkitBlockData dstBeData = Mapping.blockStateJeToBe(dstJeState);
                NukkitBlockState dstState = new NukkitBlockState(
                    dstBeData.blockId(), dstBeData.metadata(), dstJeState);
                if(isExactBlockReplacementSource(entry.getKey())) {
                    Mapping.NukkitBlockData srcBeData = Mapping.blockStateJeToBe(srcJeState);
                    NukkitBlockState srcState = new NukkitBlockState(
                        srcBeData.blockId(), srcBeData.metadata(), srcJeState);
                    BLOCK_REPLACEMENTS.put(srcState, dstState);
                } else {
                    BLOCK_IDENTIFIER_REPLACEMENTS.put(srcJeState.getIdentifier(), dstState);
                }
                success++;
            } catch(Exception e) {
                instance.getLogger().info(String.format("加载方块替换规则失败: %s -> %s", entry.getKey(), entry.getValue()));
                failed++;
            }
        }
        instance.getLogger().info(String.format("方块替换规则加载完成: 成功 %d, 失败 %d", success, failed));
    }

    private static boolean isExactBlockReplacementSource(String source) {
        String trimmed = source.trim();
        return trimmed.contains("[") || trimmed.contains("{");
    }

}
