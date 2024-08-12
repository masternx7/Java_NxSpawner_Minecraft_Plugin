package dev.fluffyworld.nxspawner;

import dev.fluffyworld.nxspawner.commands.NxSpawnerCommand;
import dev.fluffyworld.nxspawner.listeners.SpawnerEventListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class NxSpawner extends JavaPlugin {

    private File dataFile;
    private FileConfiguration dataConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadDataConfig();

        getServer().getPluginManager().registerEvents(new SpawnerEventListener(this), this);
        getCommand("nxspawner").setExecutor(new NxSpawnerCommand(this));
        getCommand("nxspawner").setTabCompleter(new NxSpawnerCommand(this));

        getLogger().info("NxSpawner plugin enabled.");
    }

    @Override
    public void onDisable() {
        saveDataConfig();
        getLogger().info("NxSpawner plugin disabled.");
    }

    public FileConfiguration getDataConfig() {
        return dataConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return getConfig();
    }

    private void loadDataConfig() {
        dataFile = new File(getDataFolder(), "data-player.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveDataConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadMessagesConfig() {
        reloadConfig();
    }
}
