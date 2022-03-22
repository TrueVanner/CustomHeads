package me.vannername.customheads;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class HeadPlacement implements Listener {
    private Main plugin;

    public HeadPlacement(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public int placementLength(FileConfiguration config) {
        int l = 0;
        while(config.contains("Placement." + l)) l++;
        return l;
    }

    @EventHandler
    public void place_e(BlockPlaceEvent e) {
        if (e.getBlockPlaced().getType() == Material.PLAYER_HEAD || e.getBlockPlaced().getType() == Material.PLAYER_WALL_HEAD) {
            FileConfiguration config = plugin.getConfig();

            int i;
            for (i = 0; i < placementLength(config) && !config.getBoolean("Placement." + i + ".broken"); i++);

            config.set("Placement." + i + ".name", e.getItemInHand().getItemMeta().getDisplayName());
            config.set("Placement." + i + ".cmd", e.getItemInHand().getItemMeta().getCustomModelData());
            config.set("Placement." + i + ".x", e.getBlockPlaced().getX());
            config.set("Placement." + i + ".y", e.getBlockPlaced().getY());
            config.set("Placement." + i + ".z", e.getBlockPlaced().getZ());
            config.set("Placement." + i + ".broken", false);
            plugin.saveConfig();
        }

    }

    int i, cmd;
    Location l;
    ItemStack head;
    SkullMeta meta;
    boolean player_head = false;

    @EventHandler
    public void break_e(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.PLAYER_HEAD || e.getBlock().getType() == Material.PLAYER_WALL_HEAD) {
            FileConfiguration c = plugin.getConfig();

            for (i = 0; c.contains("Placement." + i); i++) {
                l = new Location(e.getBlock().getWorld(), c.getDouble("Placement." + i + ".x"), c.getDouble("Placement." + i + ".y"), c.getDouble("Placement." + i + ".z"));
                if (l.equals(e.getBlock().getLocation())) {
                    break;
                }
            }
            if (i >= placementLength(c)) return;

            for (OfflinePlayer p : plugin.getServer().getOfflinePlayers()) {
                if (c.getString("Placement." + i + ".name").equals(p.getName())) {
                    player_head = true;
                }
            }
            if (player_head) {
                head = new ItemStack(Material.PLAYER_HEAD, 1, (short) 3);
                meta = (SkullMeta) head.getItemMeta();
                meta.setOwner(c.getString("Placement." + i + ".name"));
                meta.setDisplayName(c.getString("Placement." + i + ".name"));
            } else {
                try {
                    head = new Head(Main.textures.get(Main.names.indexOf(c.getString("Placement." + i + ".name"))), c.getString("Placement." + i + ".name")).getItem();
                    cmd = c.getInt("Placement." + i + ".cmd");
                } catch (IndexOutOfBoundsException e2) {
                    head = new Head("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWZkMjQwMDAwMmFkOWZiYmJkMDA2Njk0MWViNWIxYTM4NGFiOWIwZTQ4YTE3OGVlOTZlNGQxMjlhNTIwODY1NCJ9fX0=", "404: Head Not Found").getItem();
                    cmd = 789;
                }
                meta = (SkullMeta) head.getItemMeta();
            }
            meta.setCustomModelData(cmd);
            head.setItemMeta(meta);
            e.setDropItems(false);
            if (e.getPlayer().getGameMode() != GameMode.CREATIVE)
                e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), head);
            c.set("Placement." + i + ".broken", true);
            plugin.saveConfig();
        }
    }
}
