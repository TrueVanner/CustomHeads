package me.vannername.customheads;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Main extends JavaPlugin {
    public static List<String> textures = new ArrayList<>();
    public static List<String> names = new ArrayList<>();

    @Override
    public void onEnable() {
        loadFromConfig(getConfig());
        new WanderingTraderHeads(this);
        new HeadPlacement(this);
    }

    public void loadFromConfig(FileConfiguration config) {
        if (config.contains("Textures")) {
            textures = (List<String>) config.getList("Textures");
            names = (List<String>) config.getList("Names");
        }
    }

    public void loadInConfig(FileConfiguration config) {
        config.set("Textures", textures);
        config.set("Names", names);
        saveConfig();
    }

    String fullName(String[] args, int start) { //собирает вместе элементы из args, начиная от элемента start. используется для сбора многословного названия
        String res = "";
        for (int i = start; i < args.length; i++)
        {
            res += args[i];
            res += " ";
        }
        return res.trim();
    }
    /*String fullName(String[] args, int start, String end) {                убрано по ненадобности
        String res = "";
        for (int i = start; i < args.length && !args[i].equalsIgnoreCase(end); i++)
        {
            res += args[i];
            res += " ";
        }
        return res.trim();
    }*/

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {sender.sendMessage("You can`t run this command from a console!"); return false;}

        FileConfiguration config = getConfig();

        Player p = (Player) sender;
        if (args[0].equalsIgnoreCase("getmine")) {
            if (p.getInventory().containsAtLeast(new ItemStack(Material.EMERALD), 2)) {
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1, (short) 3);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                meta.setDisplayName(p.getName());
                meta.setOwner(p.getName());
                meta.setCustomModelData(456);
                skull.setItemMeta(meta);
                p.getInventory().addItem(skull);
                p.getInventory().removeItem(new ItemStack(Material.EMERALD, 2));
                p.sendMessage(ChatColor.GREEN + "Your head was added to inventory!");
            } else
                p.sendMessage(ChatColor.AQUA+"You do not have enough emeralds! (You need 2 to get your head)");
        }
        else if (args[0].equalsIgnoreCase("add")) {
            if (args.length == 2) {
                if (!textures.contains(args[1])) {
                    textures.add(args[1]);

                    int i = 1;
                    while (names.contains("head" + i)) i++;
                    names.add("head" + i);

                    loadInConfig(config);

                    p.sendMessage(ChatColor.GREEN+"Head \"head" + i + "\" successfully added!");

                } else
                    p.sendMessage(ChatColor.RED+"Head already exists");
            }
            else if (args.length >= 3) {
                if (!textures.contains(args[1])) {
                    textures.add(args[1]);
                    if (fullName(args, 2).contains("texture") || fullName(args, 2).contains("name")) {
                        p.sendMessage(ChatColor.RED+"Words \"name\" and \"texture\" are reserved for the program and cannot be used.");
                        int i = 1;
                        while (names.contains("head" + i)) i++;
                        names.add("head" + i);
                        p.sendMessage(ChatColor.RED+"Head will be saved with the name \"" +"head"+i+ "\" instead.");
                    }
                    else {
                        names.add(fullName(args, 2));
                        p.sendMessage(ChatColor.GREEN+"Head \"" + fullName(args, 2) + "\" successfully added!");
                    }

                    loadInConfig(config);

                } else p.sendMessage(ChatColor.RED+"Head already exists");
            }
            else {
                p.sendMessage(ChatColor.RED+"Usage: /heads add <texture value> <name (optional)>");
            }
        }
        else if (args[0].equalsIgnoreCase("get")) {
            if (p.getInventory().containsAtLeast(new ItemStack(Material.EMERALD), 4)) {

                if (names.contains(fullName(args, 1))) {
                    Head head = new Head(textures.get(names.indexOf(fullName(args, 1))), fullName(args, 1));
                    p.getInventory().addItem(head.getItem());
                    p.getInventory().removeItem(new ItemStack(Material.EMERALD, 4));
                    p.sendMessage(ChatColor.GREEN+"Successfully purchased \"" + fullName(args, 1) + "\"!");
                } else
                    p.sendMessage(ChatColor.RED+"Head with that name does not exist");
            } else
                p.sendMessage(ChatColor.AQUA+"You do not have enough emeralds! (You need 4 to buy one head)");
        }
        else if (args[0].equalsIgnoreCase("redact")) {
            if(args.length < 4) {p.sendMessage(ChatColor.RED+"Incorrect format! (Correct: /heads redact <head name> [texture/name] <new data>)");}
            else {
                int i = 1;
                String res = "";
                while (i < args.length && !(args[i].equals("texture") || args[i].equals("name")))
                {
                    res += args[i];
                    res += " ";
                    i++;
                }
                if (i == args.length + 1) {p.sendMessage(ChatColor.RED+"/heads redact <head name> [texture/name] <new data>"); return false;}
                res = res.trim();
                if (names.contains(res)) {
                    if (args[i].equalsIgnoreCase("texture")) {
                        if (!textures.contains(args[i+1])) {
                            textures.set(names.indexOf(res), args[i + 1]);
                            p.sendMessage(ChatColor.GREEN+"Changes saved!");
                        }
                        else
                            p.sendMessage(ChatColor.RED+"Head with that texture already exists");
                    } else {
                        if (fullName(args, i+1).contains("texture") || fullName(args, i+1).contains("name")) {
                            p.sendMessage(ChatColor.RED+"Words \"name\" and \"texture\" are reserved for the program and cannot be used. ");
                            return false;
                        }
                        else if (!names.contains(fullName(args, i+1))) {
                            names.set(names.indexOf(res), fullName(args, i + 1));
                            p.sendMessage(ChatColor.GREEN+"Changes saved!");
                        }
                        else
                            p.sendMessage(ChatColor.RED+"Head with that name already exists");
                        loadInConfig(config);
                    }
                }
                else
                    p.sendMessage(ChatColor.RED+"Head with that name does not exist");
            }
        }
        else if (args[0].equalsIgnoreCase("refund")) {
            if(p.getInventory().getItemInMainHand().getType() == Material.PLAYER_HEAD) {

                if(p.getInventory().getItemInMainHand().getItemMeta().hasCustomModelData()) {
                    for(int i = 0; i < p.getInventory().getItemInMainHand().getAmount(); i++) {
                        if (p.getInventory().getItemInMainHand().getItemMeta().getCustomModelData() == 123) {
                            p.getInventory().addItem(new ItemStack(Material.EMERALD, 4));
                        } else if (p.getInventory().getItemInMainHand().getItemMeta().getCustomModelData() == 456)
                            p.getInventory().addItem(new ItemStack(Material.EMERALD, 2));
                        else if (p.getInventory().getItemInMainHand().getItemMeta().getCustomModelData() == 789) {
                            p.getInventory().addItem(new ItemStack(Material.EMERALD, 1));
                        }
                    }
                    p.getInventory().removeItem(p.getInventory().getItemInMainHand());
                } else p.sendMessage(ChatColor.RED + "This head does not exist");
                p.sendMessage(ChatColor.GREEN+"Refund successful!");
            } else
                p.sendMessage(ChatColor.AQUA+"To refund you must be holding the player head in your main hand!");
        }
        else if (args[0].equalsIgnoreCase("remove")) {
           if (args.length >= 2) {
                if (names.contains(fullName(args, 1))) {
                    textures.remove(names.indexOf(fullName(args, 1)));
                    names.remove(fullName(args, 1));
                    loadInConfig(config);
                    p.sendMessage(ChatColor.GREEN+"Removed head \"" + fullName(args, 1) + "\" successfully!");
                } else
                    p.sendMessage(ChatColor.RED+"Head with that name does not exist");
           } else
               p.sendMessage(ChatColor.RED+"Incorrect format! (Correct: /heads remove <head name>)");
        } else if (args[0].equalsIgnoreCase("help")) {
            p.sendMessage(ChatColor.GOLD+"Heads` plugin help page:");
            p.sendMessage(ChatColor.YELLOW+"\n/heads add <texture value>"+ChatColor.AQUA+" - adds a head to the database. The texture value should be taken from the \"Value\" bar in the \"Other\" section at the end of a head`s page from the site minecraft-heads.com");
            p.sendMessage(ChatColor.YELLOW+"\n/heads remove <head name>"+ChatColor.AQUA+" - removes the head from the database");
            p.sendMessage(ChatColor.YELLOW+"\n/heads get <head name>"+ChatColor.AQUA+" - sells you a head from the database for 4 emeralds");
            p.sendMessage(ChatColor.YELLOW+"\n/heads getmine"+ChatColor.AQUA+" - gives you your own head, for 2 emeralds");
            p.sendMessage(ChatColor.YELLOW+"\n/heads redact <head name> [texture/name] <new data>"+ChatColor.AQUA+" - changes either name or the texture value of the specified head");
            p.sendMessage(ChatColor.YELLOW+"\n/heads refund"+ChatColor.AQUA+" - returns the emeralds spent on a head. To refund you must hold the specified head in your main hand.");
            p.sendMessage(ChatColor.YELLOW+"\n/heads help"+ChatColor.AQUA+" - sends this text");
        }
        else {
            p.sendMessage(ChatColor.RED+"Possible arguments: [add/remove/get/getmine/redact/refund/help]");
            return false;
        }
        return true;
    }

    List<String> s1 = Arrays.asList("add","remove","get","getmine","refund","redact","help");
    List<String> s2;
    List<String> sFinal;

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String lbl, String[] strings) {
        s2 = names;
        sFinal = new ArrayList<>();
        if(strings.length == 1) {
            for(String s : s1) {
                if(s.toLowerCase().startsWith(strings[0])) {
                    sFinal.add(s);
                }
            }
        }
        if(strings.length == 2 && (strings[0].equalsIgnoreCase("get") || strings[0].equalsIgnoreCase("remove") || strings[0].equalsIgnoreCase("redact"))) {
            for(String s : s2) {
                if(s.toLowerCase().startsWith(strings[1])) {
                    sFinal.add(s);
                }
            }
        }
        return sFinal;
    }
}
