package com.tu.dnc.storage;

import com.tu.dnc.DNCEntry;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Quan ly luu tru tai plugins/DisplayNameColor/config.yml (danh sach DNC)
 * va plugins/DisplayNameColor/data.yml (DNC dang bat cua tung player).
 */
public class DataManager {

    private final JavaPlugin plugin;
    private final File configFile;
    private final File dataFile;
    private FileConfiguration config;
    private FileConfiguration data;

    private final Map<String, DNCEntry> entries = new LinkedHashMap<>();
    // uuid -> ten DNC dang bat (null/absent = khong bat cai nao)
    private final Map<UUID, String> activeSelections = new HashMap<>();

    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        File folder = plugin.getDataFolder(); // plugins/DisplayNameColor/
        if (!folder.exists()) folder.mkdirs();
        this.configFile = new File(folder, "config.yml");
        this.dataFile = new File(folder, "data.yml");
        reload();
    }

    public void reload() {
        entries.clear();
        activeSelections.clear();

        try {
            if (!configFile.exists()) configFile.createNewFile();
            if (!dataFile.exists()) dataFile.createNewFile();
        } catch (IOException e) {
            plugin.getLogger().severe("Khong the tao file config/data: " + e.getMessage());
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        data = YamlConfiguration.loadConfiguration(dataFile);

        if (config.isConfigurationSection("dncs")) {
            for (String key : config.getConfigurationSection("dncs").getKeys(false)) {
                String path = "dncs." + key;
                String matName = config.getString(path + ".material", "NAME_TAG");
                Material mat;
                try {
                    mat = Material.valueOf(matName.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    mat = Material.NAME_TAG;
                }
                List<String> colors = config.getStringList(path + ".colors");
                entries.put(key.toLowerCase(), new DNCEntry(key, mat, colors));
            }
        }

        if (data.isConfigurationSection("players")) {
            for (String uuidStr : data.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String active = data.getString("players." + uuidStr + ".active");
                    if (active != null) {
                        activeSelections.put(uuid, active.toLowerCase());
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public void saveConfig() {
        config.set("dncs", null); // clear truoc khi ghi lai
        for (DNCEntry e : entries.values()) {
            String path = "dncs." + e.getName();
            config.set(path + ".material", e.getMaterial().name());
            config.set(path + ".colors", e.getColors());
        }
        try {
            config.save(configFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("Khong the luu config.yml: " + ex.getMessage());
        }
    }

    public void saveData() {
        data.set("players", null);
        for (Map.Entry<UUID, String> en : activeSelections.entrySet()) {
            data.set("players." + en.getKey() + ".active", en.getValue());
        }
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("Khong the luu data.yml: " + ex.getMessage());
        }
    }

    // ---------- DNC management ----------

    public DNCEntry getEntry(String name) {
        return entries.get(name.toLowerCase());
    }

    public Collection<DNCEntry> getAllEntries() {
        return entries.values();
    }

    public boolean createEntry(String name, Material material, List<String> colors) {
        if (entries.containsKey(name.toLowerCase())) return false;
        entries.put(name.toLowerCase(), new DNCEntry(name, material, colors));
        saveConfig();
        return true;
    }

    public boolean removeEntry(String name) {
        DNCEntry removed = entries.remove(name.toLowerCase());
        if (removed == null) return false;
        // Neu ai dang bat DNC nay, tu dong tat
        activeSelections.values().removeIf(v -> v.equalsIgnoreCase(name));
        saveConfig();
        saveData();
        return true;
    }

    public void renameEntry(String oldName, String newName) {
        DNCEntry e = entries.remove(oldName.toLowerCase());
        if (e == null) return;
        e.setName(newName);
        entries.put(newName.toLowerCase(), e);
        // cap nhat lai active selection neu co nguoi dang bat
        for (Map.Entry<UUID, String> en : activeSelections.entrySet()) {
            if (en.getValue().equalsIgnoreCase(oldName)) {
                en.setValue(newName.toLowerCase());
            }
        }
        saveConfig();
        saveData();
    }

    // ---------- Player active selection ----------

    public String getActive(UUID uuid) {
        return activeSelections.get(uuid);
    }

    public void setActive(UUID uuid, String dncName) {
        if (dncName == null) {
            activeSelections.remove(uuid);
        } else {
            activeSelections.put(uuid, dncName.toLowerCase());
        }
        saveData();
    }

    /** Tra ve danh sach UUID dang bat 1 DNC cu the (dung de kiem tra khi revoke permission). */
    public List<UUID> getAllActiveOfEntry(String name) {
        List<UUID> result = new ArrayList<>();
        for (Map.Entry<UUID, String> en : activeSelections.entrySet()) {
            if (en.getValue().equalsIgnoreCase(name)) {
                result.add(en.getKey());
            }
        }
        return result;
    }

    /** Tat DNC cho tat ca player dang bat no (dung khi admin revoke permission hoac xoa DNC). */
    public List<UUID> clearActiveForEntry(String name) {
        List<UUID> affected = new ArrayList<>();
        Iterator<Map.Entry<UUID, String>> it = activeSelections.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, String> en = it.next();
            if (en.getValue().equalsIgnoreCase(name)) {
                affected.add(en.getKey());
                it.remove();
            }
        }
        if (!affected.isEmpty()) saveData();
        return affected;
    }
}
