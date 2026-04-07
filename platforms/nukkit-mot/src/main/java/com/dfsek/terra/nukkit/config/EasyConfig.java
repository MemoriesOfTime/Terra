package com.dfsek.terra.nukkit.config;


import com.sun.tools.javac.Main;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.dfsek.terra.nukkit.TerraNukkitPlugin;

import static com.dfsek.terra.nukkit.TerraNukkitPlugin.instance;


public class EasyConfig {
    private final File configFile;
    private final Yaml yaml;
    // 注册的配置项（扁平键 -> 默认值）
    private final LinkedHashMap<String, Object> defaults = new LinkedHashMap<>();
    // 注释（扁平键 -> 注释文本）
    private final LinkedHashMap<String, String> comments = new LinkedHashMap<>();
    // 当前配置值（扁平键 -> 值）
    private final LinkedHashMap<String, Object> values = new LinkedHashMap<>();
    // 配置类引用（用于 syncToFields）
    private Class<?> configClass;


    public EasyConfig(String filePath) {
        configFile = new File(filePath);
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(true);
        yaml = new Yaml(options);
    }


    // ==================== 配置项注册 ====================

    /**
     * 添加配置项（无注释）
     */
    public EasyConfig add(String key, Object defaultValue) {
        return add(key, defaultValue, "");
    }

    /**
     * 添加配置项（带注释） F-02
     */
    public EasyConfig add(String key, Object defaultValue, String comment) {
        if (!defaults.containsKey(key)) {
            defaults.put(key, defaultValue);
            values.put(key, defaultValue);
            if (comment != null && !comment.isEmpty()) {
                comments.put(key, comment);
            }
        }
        return this;
    }

    /**
     * 从带有 @ConfigItem 注解的配置类自动加载所有静态字段为配置项 F-03
     */
    public void loadFromClass(Class<?> clazz) {
        configClass = clazz;
        for (Field field : clazz.getDeclaredFields()) {
            ConfigItem annotation = field.getAnnotation(ConfigItem.class);
            if (annotation != null) {
                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(null);
                    String key = annotation.key().isEmpty() ? field.getName() : annotation.key();
                    String comment = annotation.comment();
                    add(key, fieldValue, comment);
                } catch (IllegalAccessException e) {
                    instance.getLogger().warning("无法读取字段 " + field.getName() + ": " + e.getMessage());
                }
            }
        }
    }


    // ==================== 配置值读取（类型安全）====================

    /**
     * 通用获取方法，返回配置值或默认值 F-04
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        T val = (T) values.get(key);
        if (val == null) {
            val = (T) defaults.get(key);
        }
        return val != null ? val : (T) values.get(key);
    }

    /**
     * 通用获取方法，带默认回退值
     */
    public <T> T get(String key, T defaultValue) {
        T val = get(key);
        return val != null ? val : defaultValue;
    }

    /** F-05: 获取字符串 */
    public String getString(String key) {
        return getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        Object v = get(key);
        return v == null ? defaultValue : String.valueOf(v);
    }

    /** F-05: 获取整数 */
    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        Number n = get(key);
        return n != null ? n.intValue() : defaultValue;
    }

    /** F-05: 获取布尔值 */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object v = get(key);
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof String) {
            if ("true".equalsIgnoreCase((String) v)) return true;
            if ("false".equalsIgnoreCase((String) v)) return false;
        }
        return defaultValue;
    }

    /** F-05: 获取双精度浮点数 */
    public double getDouble(String key) {
        return getDouble(key, 0.0);
    }

    public double getDouble(String key, double defaultValue) {
        Number n = get(key);
        return n != null ? n.doubleValue() : defaultValue;
    }

    public long getLong(String key) { return getLong(key, 0L); }
    public long getLong(String key, long defaultValue) {
        Number n = get(key);
        return n != null ? n.longValue() : defaultValue;
    }

    /** 类型检查方法 */
    public boolean isInt(String key) { return get(key) instanceof Integer; }
    public boolean isLong(String key) { return get(key) instanceof Long; }
    public boolean isDouble(String key) { return get(key) instanceof Double; }
    public boolean isString(String key) { return get(key) instanceof String; }
    public boolean isBoolean(String key) { return get(key) instanceof Boolean; }

    /** F-06: 获取列表 */
    @SuppressWarnings("unchecked")
    public <E> List<E> getList(String key) { return getList(key, null); }
    @SuppressWarnings("unchecked")
    public <E> List<E> getList(String key, List<E> defaultList) {
        Object v = get(key);
        if (v instanceof List) return (List<E>) v;
        return defaultList;
    }
    public boolean isList(String key) { return get(key) instanceof List; }

    public List<String> getStringList(String key) {
        List<?> value = getList(key);
        if (value == null) return new ArrayList<>(0);
        List<String> result = new ArrayList<>();
        for (Object o : value) {
            if (o instanceof String || o instanceof Number || o instanceof Boolean || o instanceof Character) {
                result.add(String.valueOf(o));
            }
        }
        return result;
    }

    public List<Integer> getIntegerList(String key) {
        List<?> list = getList(key);
        if (list == null) return new ArrayList<>(0);
        List<Integer> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Integer) result.add((Integer) object);
            else if (object instanceof String) {
                try { result.add(Integer.valueOf((String) object)); } catch (Exception ignored) {}
            } else if (object instanceof Character) result.add((int)(Character) object);
            else if (object instanceof Number) result.add(((Number) object).intValue());
        }
        return result;
    }

    public List<Boolean> getBooleanList(String key) {
        List<?> list = getList(key);
        if (list == null) return new ArrayList<>(0);
        List<Boolean> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Boolean) result.add((Boolean) object);
            else if (object instanceof String) {
                if (Boolean.TRUE.toString().equals(object)) result.add(true);
                else if (Boolean.FALSE.toString().equals(object)) result.add(false);
            }
        }
        return result;
    }

    public List<Double> getDoubleList(String key) {
        List<?> list = getList(key);
        if (list == null) return new ArrayList<>(0);
        List<Double> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Double) result.add((Double) object);
            else if (object instanceof String) {
                try { result.add(Double.valueOf((String) object)); } catch (Exception ignored) {}
            } else if (object instanceof Character) result.add((double)(Character) object);
            else if (object instanceof Number) result.add(((Number) object).doubleValue());
        }
        return result;
    }

    public List<Float> getFloatList(String key) {
        List<?> list = getList(key);
        if (list == null) return new ArrayList<>(0);
        List<Float> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Float) result.add((Float) object);
            else if (object instanceof String) {
                try { result.add(Float.valueOf((String) object)); } catch (Exception ignored) {}
            } else if (object instanceof Character) result.add((float)(Character) object);
            else if (object instanceof Number) result.add(((Number) object).floatValue());
        }
        return result;
    }

    public List<Long> getLongList(String key) {
        List<?> list = getList(key);
        if (list == null) return new ArrayList<>(0);
        List<Long> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Long) result.add((Long) object);
            else if (object instanceof String) {
                try { result.add(Long.valueOf((String) object)); } catch (Exception ignored) {}
            } else if (object instanceof Character) result.add((long)(Character) object);
            else if (object instanceof Number) result.add(((Number) object).longValue());
        }
        return result;
    }

    public List<Byte> getByteList(String key) {
        List<?> list = getList(key);
        if (list == null) return new ArrayList<>(0);
        List<Byte> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Byte) result.add((Byte) object);
            else if (object instanceof String) {
                try { result.add(Byte.valueOf((String) object)); } catch (Exception ignored) {}
            } else if (object instanceof Character) result.add((byte)((Character) object).charValue());
            else if (object instanceof Number) result.add(((Number) object).byteValue());
        }
        return result;
    }

    public List<Character> getCharacterList(String key) {
        List<?> list = getList(key);
        if (list == null) return new ArrayList<>(0);
        List<Character> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Character) result.add((Character) object);
            else if (object instanceof String) {
                String str = (String) object;
                if (str.length() == 1) result.add(str.charAt(0));
            } else if (object instanceof Number) result.add((char)((Number) object).intValue());
        }
        return result;
    }

    public List<Short> getShortList(String key) {
        List<?> list = getList(key);
        if (list == null) return new ArrayList<>(0);
        List<Short> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Short) result.add((Short) object);
            else if (object instanceof String) {
                try { result.add(Short.valueOf((String) object)); } catch (Exception ignored) {}
            } else if (object instanceof Character) result.add((short)((Character) object).charValue());
            else if (object instanceof Number) result.add(((Number) object).shortValue());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<Map> getMapList(String key) {
        List<Map> list = getList(key);
        List<Map> result = new ArrayList<>();
        if (list == null) return result;
        for (Object object : list) {
            if (object instanceof Map) result.add((Map) object);
        }
        return result;
    }

    /** F-06: 获取 Map */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(String key) {
        Object v = get(key);
        if (v instanceof Map) return (Map<String, Object>) v;
        return null;
    }


    // ==================== 运行时修改 F-19 ====================

    /**
     * 运行时动态修改配置值
     */
    public void set(String key, Object value) {
        values.put(key, value);
    }


    // ==================== 文件持久化 ====================

    /**
     * 从 YAML 文件加载配置；文件不存在时用默认值创建并保存 F-11
     * 加载时自动检测新增的默认配置项并合并（不覆盖已有值）F-12
     * 首次创建文件后自动调用 syncToFields() F-18
     */
    @SuppressWarnings("unchecked")
    public void load() {
        if (!configFile.exists()) {
            save();
            syncToFields();
            return;
        }

        try (InputStream is = new FileInputStream(configFile)) {
            Map<String, Object> loaded = yaml.load(is);
            if (loaded == null) loaded = new HashMap<>();

            // 将嵌套 Map 展平为点号分隔的键
            Map<String, Object> flattened = flattenMap("", loaded);

            // 合并：已存在的值保留，新增的用默认值填充 F-12
            boolean hasNewKeys = false;
            for (Map.Entry<String, Object> entry : defaults.entrySet()) {
                String key = entry.getKey();
                if (!flattened.containsKey(key)) {
                    values.put(key, entry.getValue());
                    hasNewKeys = true;
                } else {
                    values.put(key, flattened.get(key));
                }
            }

            // 如果有新增的配置项，回写到文件 F-12
            if (hasNewKeys) {
                save();
            }

            syncToFields(); // F-18
        } catch (IOException e) {
            instance.getLogger().warning("加载配置文件失败: " + e.getMessage());
            // 降级：使用所有默认值
            values.clear();
            values.putAll(defaults);
        }
    }

    /**
     * 将当前内存中的配置写入 YAML 文件 F-13
     * 保存时保留注释信息 F-14
     */
    public void save() {
        // 确保父目录存在 F-15
        File parentDir = configFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        try {
            // 构建带注释的输出内容
            StringBuilder sb = buildYamlWithComments();

            try (OutputStream os = new FileOutputStream(configFile);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                writer.write(sb.toString());
            }
        } catch (IOException e) {
            instance.getLogger().warning("保存配置文件失败: " + e.getMessage()); // NFR-01
        }
    }


    // ==================== 字段同步机制 ====================

    /**
     * 将当前配置值回写到配置类的 static 字段 F-17
     */
    public void syncToFields() {
        if (configClass == null) return;

        for (Field field : configClass.getDeclaredFields()) {
            ConfigItem annotation = field.getAnnotation(ConfigItem.class);
            if (annotation != null) {
                String key = annotation.key().isEmpty() ? field.getName() : annotation.key();
                Object currentValue = values.get(key);
                if (currentValue != null) {
                    field.setAccessible(true);
                    try {
                        field.set(null, convertType(currentValue, field.getType()));
                    } catch (IllegalAccessException e) {
                        instance.getLogger().warning("同步字段 " + field.getName() + " 失败: " + e.getMessage()); // NFR-01
                    }
                }
            }
        }
    }


    // ==================== 内部工具方法 ====================

    /**
     * 将嵌套 Map 展平为点号分隔的键
     */
    private Map<String, Object> flattenMap(String prefix, Map<String, Object> nested) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : nested.entrySet()) {
            String fullKey = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                result.putAll(flattenMap(fullKey, (Map<String, Object>) value));
            } else {
                result.put(fullKey, value);
            }
        }
        return result;
    }

    /**
     * 将扁平键值对转换为嵌套 Map 结构用于 YAML 输出
     */
    private LinkedHashMap<Object, Object> toNestedMap(Map<String, Object> flatMap) {
        LinkedHashMap<Object, Object> root = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : flatMap.entrySet()) {
            String[] parts = entry.getKey().split("\\.");
            LinkedHashMap<Object, Object> current = root;
            for (int i = 0; i < parts.length - 1; i++) {
                current.computeIfAbsent(parts[i], k -> new LinkedHashMap<>());
                Object next = current.get(parts[i]);
                if (!(next instanceof LinkedHashMap)) {
                    LinkedHashMap<Object, Object> replacement = new LinkedHashMap<>();
                    replacement.put(parts[i], next);
                    current.put(parts[i], replacement);
                    next = replacement;
                }
                current = (LinkedHashMap<Object, Object>) next;
            }
            current.put(parts[parts.length - 1], entry.getValue());
        }
        return root;
    }

    /**
     * 构建 YAML 格式字符串并保留注释
     */
    private StringBuilder buildYamlWithComments() {
        StringBuilder sb = new StringBuilder();
        LinkedHashMap<Object, Object> nestedValues = toNestedMap(values);
        appendNode(sb, nestedValues, "", 0);
        // 移除末尾多余的空行
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("\n");
        return sb;
    }

    /**
     * 递归构建带注释的 YAML 节点
     */
    private void appendNode(StringBuilder sb, LinkedHashMap<Object, Object> node, String pathPrefix, int indent) {
        Iterator<Map.Entry<Object, Object>> iterator = node.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Object> entry = iterator.next();
            String keyStr = String.valueOf(entry.getKey());
            String fullPath = pathPrefix.isEmpty() ? keyStr : pathPrefix + "." + keyStr;
            Object value = entry.getValue();
            boolean hasNext = iterator.hasNext();

            // 写入注释（位于对应 key 上方）F-14
            String comment = comments.get(fullPath);
            if (comment != null && !comment.isEmpty()) {
                for (String line : comment.split("\n")) {
                    appendIndent(sb, indent).append("# ").append(line).append("\n");
                }
            }

            appendIndent(sb, indent);

            if (value instanceof LinkedHashMap) {
                // 嵌套 Map
                sb.append(keyStr).append(":");
                if (((LinkedHashMap<?, ?>) value).isEmpty()) {
                    sb.append(" {}").append("\n");
                } else {
                    sb.append("\n");
                    appendNode(sb, (LinkedHashMap<Object, Object>) value, fullPath, indent + 2);
                }
            } else if (value instanceof List) {
                // 列表类型
                sb.append(keyStr).append(":");
                if (((List<?>) value).isEmpty()) {
                    sb.append(" []").append("\n");
                } else {
                    sb.append("\n");
                    appendListItems(sb, (List<?>) value, indent + 2);
                }
            } else {
                // 基本类型
                sb.append(keyStr).append(": ");
                sb.append(formatYamlValue(value)).append("\n");
            }

            // 在非最后一个元素后添加空行（仅顶层）
            if (!hasNext && indent == 0) {
                sb.append("\n");
            }
        }
    }
    /**
     * 追加缩进
     */
    private StringBuilder appendIndent(StringBuilder sb, int count) {
        for (int i = 0; i < count; i++) sb.append(' ');
        return sb;
    }

    /**
     * 格式化列表项
     */
    @SuppressWarnings("unchecked")
    private void appendListItems(StringBuilder sb, List<?> list, int indent) {
        for (Object item : list) {
            appendIndent(sb, indent).append("- ");
            if (item instanceof Map) {
                sb.append("\n");
                appendNode(sb, (LinkedHashMap<Object, Object>) ((LinkedHashMap<?, ?>) item), "", indent + 2);
            } else {
                sb.append(formatYamlValue(item)).append("\n");
            }
        }
    }

    /**
     * 格式化 YAML 值：特殊字符需要加引号转义
     */
    private String formatYamlValue(Object value) {
        if (value == null) return "null";
        String str = String.valueOf(value);
        if (value instanceof String) {
            if (str.isEmpty() || needsQuoting(str)) {
                return quoteString(str);
            }
        }
        return str;
    }

    /**
     * 判断字符串是否需要引号包裹
     */
    private static final Pattern NEEDS_QUOTING = Pattern.compile("[#:{}]\\s|\\s#|^$|[\\r\\n]");
    private boolean needsQuoting(String str) {
        return NEEDS_QUOTING.matcher(str).find()
               || str.startsWith("!") || str.startsWith("&") || str.equals("true")
               || str.equals("false") || str.equals("null") || str.matches("^\\d.*")
               || str.contains("<<");
    }

    /**
     * 双引号转义
     */
    private String quoteString(String str) {
        return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }

    /**
     * 类型转换：将配置值转换为目标字段的类型
     */
    private Object convertType(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isAssignableFrom(value.getClass())) return value;

        String strVal = String.valueOf(value);
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(strVal);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(strVal);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(strVal);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(strVal);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(strVal);
        } else if (targetType == String.class) {
            return strVal;
        } else if (targetType == short.class || targetType == Short.class) {
            return Short.parseShort(strVal);
        } else if (targetType == byte.class || targetType == Byte.class) {
            return Byte.parseByte(strVal);
        } else if (targetType == char.class || targetType == Character.class) {
            return strVal.charAt(0);
        }
        return value;
    }
}
