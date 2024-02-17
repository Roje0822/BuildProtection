package xyz.chide1.buildprotection.message;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.chide1.buildprotection.BuildProtection;

import java.util.*;

public class MessageManager {


    private static MessageManager instance;
    private final Map<MessageType, Map<String, String>> configMap = new HashMap<>();
    private FileConfiguration file;

    private MessageManager() {}

    public static MessageManager getInstance() {
        if (instance == null) instance = new MessageManager();
        return instance;
    }

    public void initialize(FileConfiguration file) {
        configMap.clear();
        this.file = file;
        for (MessageType type : MessageType.values()) {
            ConfigurationSection configSection = file.getConfigurationSection(type.key);
            if (configSection != null) {
                initializeMessages(type, configSection);
            }
        }
    }

    private void initializeMessages(MessageType type, ConfigurationSection configSection) {
        Map<String, String> messages = configMap.computeIfAbsent(type, key -> new HashMap<>());
        for (String key : configSection.getKeys(true)) {
            messages.put(key, ChatColor.translateAlternateColorCodes('&', configSection.getString(key)));
        }
    }

    public Optional<String> getMessage(MessageType type, String key) {
        return Optional.ofNullable(configMap.getOrDefault(type, Collections.emptyMap()).get(key));
    }

    public Optional<String> getMessageAfterPrefix(MessageType type, String key) {
        String prefix = getMessage(type, "prefix").orElse("");
        return getMessage(type, key).map(message -> prefix + message);
    }

    public List<String> getMessages(MessageType type, String path) {
        Map<String, String> MessageTypeMap = configMap.getOrDefault(type, Collections.emptyMap());
        String messagesString = MessageTypeMap.getOrDefault(path, "");
        String[] messagesArray = messagesString.split("\\\\n");
        return Arrays.asList(messagesArray);
    }

    public List<String> getUsage(MessageType type, String path) {
        List<?> list = file.getList(type.key + "." + path);
        List<String> temp = new ArrayList<>();
        list.forEach(message -> {
            if (!message.toString().isEmpty()) {
                temp.add(getPrefix() + ChatColor.translateAlternateColorCodes('&', message.toString()));
            } else {
                temp.add("");
            }
        });
        return temp;
    }

    public int getInt(MessageType type, String key) {
        String value = configMap.getOrDefault(type, Collections.emptyMap()).get(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) { e.printStackTrace(); }
        }
        return 0;
    }

    public float getFloat(MessageType type, String key) {
        String value = configMap.getOrDefault(type, Collections.emptyMap()).get(key);
        if (value != null) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) { e.printStackTrace(); }
        }
        return 1;
    }

    public String getPrefix() {
        return getMessage(MessageType.NORMAL, "prefix").orElse("");
    }
}
