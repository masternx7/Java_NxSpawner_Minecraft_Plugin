package dev.fluffyworld.nxspawner.listeners;

import dev.fluffyworld.nxspawner.NxSpawner;
import dev.fluffyworld.nxspawner.utils.ConfigUtils;
import dev.fluffyworld.nxspawner.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

import java.util.UUID;

public class SpawnerEventListener implements Listener {

    private final NxSpawner plugin;
    private final FileConfiguration dataConfig;

    public SpawnerEventListener(NxSpawner plugin) {
        this.plugin = plugin;
        this.dataConfig = plugin.getDataConfig();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.SPAWNER) {
            Player player = event.getPlayer();
            String locString = ConfigUtils.locationToString(block.getLocation());
            dataConfig.set(locString, player.getUniqueId().toString());
            plugin.saveDataConfig();
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.SPAWNER) {
            Player player = event.getPlayer();
            String locString = ConfigUtils.locationToString(event.getClickedBlock().getLocation());
            if (dataConfig.contains(locString)) {
                UUID ownerUUID = UUID.fromString(dataConfig.getString(locString));
                if (!player.getUniqueId().equals(ownerUUID)) {
                    player.sendMessage(MessageUtils.colorize(plugin.getMessagesConfig().getString("messages.not-owner")));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.SPAWNER) {
            Player player = event.getPlayer();
            String locString = ConfigUtils.locationToString(block.getLocation());
            if (dataConfig.contains(locString)) {
                UUID ownerUUID = UUID.fromString(dataConfig.getString(locString));
                if (!player.getUniqueId().equals(ownerUUID)) {
                    player.sendMessage(MessageUtils.colorize(plugin.getMessagesConfig().getString("messages.not-owner")));
                    event.setCancelled(true);
                } else {
                    player.sendMessage(MessageUtils.colorize(plugin.getMessagesConfig().getString("messages.not-broken")));
                    event.setCancelled(true);
                }
            }
        }
    }
}
