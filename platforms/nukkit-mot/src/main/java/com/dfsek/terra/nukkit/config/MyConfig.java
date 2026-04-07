package com.dfsek.terra.nukkit.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dfsek.terra.nukkit.JeBlockState;
import com.dfsek.terra.nukkit.Mapping;
import com.dfsek.terra.nukkit.delegate.NukkitBlockState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dfsek.terra.nukkit.TerraNukkitPlugin.ec;
import static com.dfsek.terra.nukkit.TerraNukkitPlugin.instance;
import static com.dfsek.terra.nukkit.delegate.NukkitBlockState.BLOCK_REPLACEMENTS;


public class MyConfig {


    @ConfigItem(key = "debug", comment = "是否开启调试模式")
    public static boolean DEBUG = false;

    @ConfigItem(key = "block_replacements_enabled", comment = "是否启用方块替换功能")
    public static boolean BLOCK_REPLACEMENTS_ENABLED = true;

    @ConfigItem(key = "block_replacements", comment = "方块替换映射表")
    public static Map<String, String> BLOCK_REPLACEMENTS_SOURCE = new HashMap<>();

    public static void initConfig() {
        ec = new EasyConfig(instance.getDataFolder().getPath() + "/block_replacements.yml");
        ec.loadFromClass(MyConfig.class);
        ec.load();
        applyBlockReplacements();
    }


    @SuppressWarnings("unchecked")
    public static void applyBlockReplacements() {
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
                Mapping.NukkitBlockData srcBeData = Mapping.blockStateJeToBe(srcJeState);
                Mapping.NukkitBlockData dstBeData = Mapping.blockStateJeToBe(dstJeState);
                NukkitBlockState srcState = new NukkitBlockState(
                    srcBeData.blockId(), srcBeData.metadata(), srcJeState);
                NukkitBlockState dstState = new NukkitBlockState(
                    dstBeData.blockId(), dstBeData.metadata(), dstJeState);
                BLOCK_REPLACEMENTS.put(srcState, dstState);
                success++;
            } catch(Exception e) {
                instance.getLogger().info(String.format("加载方块替换规则失败: %s -> %s", entry.getKey(), entry.getValue()));
                failed++;
            }
        }
        instance.getLogger().info(String.format("方块替换规则加载完成: 成功 %d, 失败 %d", success, failed));
    }

}
