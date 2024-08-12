package dev.fluffyworld.nxspawner.commands;

import dev.fluffyworld.nxspawner.NxSpawner;
import dev.fluffyworld.nxspawner.utils.ConfigUtils;
import dev.fluffyworld.nxspawner.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class NxSpawnerCommand implements TabExecutor {

    private final NxSpawner plugin;
    private final FileConfiguration dataConfig;

    public NxSpawnerCommand(NxSpawner plugin) {
        this.plugin = plugin;
        this.dataConfig = plugin.getDataConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MessageUtils.colorize(plugin.getConfig().getString("messages.usage-message")));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                if (!sender.hasPermission("nxspawner.give")) {
                    sender.sendMessage(MessageUtils.colorize(plugin.getMessagesConfig().getString("messages.no-permission")));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(MessageUtils.colorize(plugin.getConfig().getString("messages.usage-message")));
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(MessageUtils.colorize(plugin.getMessagesConfig().getString("messages.player-not-found")));
                    return true;
                }

                try {
                    EntityType type = EntityType.valueOf(args[2].toUpperCase());
                    ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
                    BlockStateMeta meta = (BlockStateMeta) spawnerItem.getItemMeta();
                    CreatureSpawner spawner = (CreatureSpawner) meta.getBlockState();
                    spawner.setSpawnedType(type);
                    meta.setBlockState(spawner);
                    spawnerItem.setItemMeta(meta);
                    target.getInventory().addItem(spawnerItem);
                    target.sendMessage(MessageUtils.colorize(String.format(plugin.getMessagesConfig().getString("messages.spawner-given"), type.name())));
                    sender.sendMessage(MessageUtils.colorize(String.format(plugin.getMessagesConfig().getString("messages.spawner-given-to"), target.getName())));
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(MessageUtils.colorize(plugin.getMessagesConfig().getString("messages.invalid-mob-type")));
                }
                break;

            case "claim":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtils.colorize("Only players can use this command."));
                    return true;
                }
                Player player = (Player) sender;
                if (!player.hasPermission("nxspawner.claim")) {
                    player.sendMessage(MessageUtils.colorize(plugin.getMessagesConfig().getString("messages.no-permission")));
                    return true;
                }
                Block block = player.getTargetBlockExact(5);
                if (block != null && block.getType() == Material.SPAWNER) {
                    CreatureSpawner spawner = (CreatureSpawner) block.getState();
                    EntityType spawnedType = spawner.getSpawnedType();
                    String locString = ConfigUtils.locationToString(block.getLocation());
                    if (dataConfig.contains(locString)) {
                        UUID ownerUUID = UUID.fromString(dataConfig.getString(locString));
                        if (player.getUniqueId().equals(ownerUUID)) {
                            block.setType(Material.AIR);
                            ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
                            BlockStateMeta meta = (BlockStateMeta) spawnerItem.getItemMeta();
                            CreatureSpawner newSpawner = (CreatureSpawner) meta.getBlockState();
                            newSpawner.setSpawnedType(spawnedType);
                            meta.setBlockState(newSpawner);
                            spawnerItem.setItemMeta(meta);
                            player.getInventory().addItem(spawnerItem);
                            dataConfig.set(locString, null);
                            plugin.saveDataConfig();
                            player.sendMessage(MessageUtils.colorize(plugin.getMessagesConfig().getString("messages.spawner-claimed")));
                        } else {
                            player.sendMessage(MessageUtils.colorize(plugin.getMessagesConfig().getString("messages.not-owner")));
                        }
                    } else {
                        player.sendMessage(MessageUtils.colorize(plugin.getMessagesConfig().getString("messages.not-player-placed")));
                    }
                } else {
                    player.sendMessage(MessageUtils.colorize(plugin.getMessagesConfig().getString("messages.no-spawner-in-range")));
                }
                break;

            case "reload":
                if (!sender.hasPermission("nxspawner.reload")) {
                    sender.sendMessage(MessageUtils.colorize(plugin.getMessagesConfig().getString("messages.no-permission")));
                    return true;
                }
                plugin.reloadMessagesConfig();
                sender.sendMessage(MessageUtils.colorize(plugin.getMessagesConfig().getString("messages.config-reloaded")));
                break;

            default:
                sender.sendMessage(MessageUtils.colorize(plugin.getConfig().getString("messages.usage-message")));
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("give", "claim", "reload").stream()
                    .filter(cmd -> sender.hasPermission("nxspawner." + cmd))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give") && sender.hasPermission("nxspawner.give")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give") && sender.hasPermission("nxspawner.give")) {
            return Arrays.stream(EntityType.values())
                    .map(Enum::name)
                    .collect(Collectors.toList());
        }
        return null;
    }
}
