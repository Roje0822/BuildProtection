package xyz.chide1.buildprotection.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.chide1.buildprotection.util.Tuple;

import java.lang.reflect.Array;
import java.util.*;

public class ConfigManager {

    private static ConfigManager instance;
    private final Map<ConfigType, Map<String, String>> map = new HashMap<>();

    private ConfigManager() {}

    public static ConfigManager getInstance() {
        if (instance == null) instance = new ConfigManager();
        return instance;
    }

    public void initialize(FileConfiguration file) {
        map.clear();
        Arrays.stream(ConfigType.values()).map(values -> new Tuple(values, file.getConfigurationSection(values.getKey())))
                .forEach(this::initializeMessages);
    }

    private void initializeMessages(Tuple<ConfigType, ConfigurationSection> pair) {
        Map<String, String> messages = map.computeIfAbsent(pair.getA(), (unused) -> new HashMap<>());
        pair.getB().getKeys(false).forEach(key ->
                messages.put(key, ChatColor.translateAlternateColorCodes('&', pair.getB().getString(key))));
    }

    public Optional<String> getMessage(ConfigType type, String key) {
        return Optional.ofNullable(map.get(type).get(key));
    }

    public int getInt(ConfigType type, String key) {
        String value = map.getOrDefault(type, Collections.emptyMap()).get(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) { e.printStackTrace(); }
        }
        return 0;
    }

}
