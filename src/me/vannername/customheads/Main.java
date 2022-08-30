package me.vannername.customheads;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Main extends JavaPlugin {
    public static List<String> textures = new ArrayList<>();
    public static List<String> names = new ArrayList<>();
    public static Map<String, String> player_heads = new HashMap<>();

    @Override
    public void onEnable() {
        loadFromConfig(getConfig());
        new WanderingTraderHeads(this);
        new HeadPlacement(this);
    }

    public void loadFromConfig(FileConfiguration config) {
        if (config.contains("Textures")) {
            textures = config.getStringList("Textures");
            names = config.getStringList("Names");
        }

        if(config.contains("PlayerHeads")) {
            player_heads = (Map<String, String>) config.getMapList("PlayerHeads").get(0);
        }
    }

    public void loadInConfig(FileConfiguration config) {
        config.set("Textures", textures);
        config.set("Names", names);

        ArrayList<Map<?, ?>> temp = new ArrayList<>();
        temp.add(player_heads);
        config.set("PlayerHeads", temp);

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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//
        FileConfiguration config = getConfig();

        if(args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "Heads' plugin help page:\n");
            sender.sendMessage(ChatColor.YELLOW + "/heads add <texture value>" + ChatColor.AQUA + " - adds a head to the database. The texture value should be taken from the \"Value\" bar in the \"Other\" section at the end of a head`s page from the site minecraft-heads.com");
            sender.sendMessage(ChatColor.YELLOW + "/heads remove <head name>" + ChatColor.AQUA + " - removes the head from the database");
            sender.sendMessage(ChatColor.YELLOW + "/heads get <head name>" + ChatColor.AQUA + " - sells you a head from the database");
            sender.sendMessage(ChatColor.YELLOW + "/heads getmine" + ChatColor.AQUA + " - gives you your own head (only if it was added; otherwise, ask the admin to do it for you)");
            sender.sendMessage(ChatColor.YELLOW + "/heads redact <head name> [texture/name] <new data>" + ChatColor.AQUA + " - changes either name or the texture value of the specified head");
            sender.sendMessage(ChatColor.YELLOW + "/heads refund" + ChatColor.AQUA + " - returns the emeralds spent on a head. To refund you must hold the specified head in your main hand.");
            sender.sendMessage(ChatColor.YELLOW + "/heads help" + ChatColor.AQUA + " - sends this text");

            return false;
        }

        if(args[0].contains("get") || args[0].equals("refund")) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("You can`t run this command from a console!");
                return false;
            }

            if (args[0].equalsIgnoreCase("getmine")) {
                if (p.getInventory().containsAtLeast(new ItemStack(Material.EMERALD), Head.priceFor(Head.HeadType.PLAYER))) {
                    
                    if(player_heads.containsKey(p.getName())) {
                        Head head = new Head(p.getName(), player_heads.get(p.getName()), Head.HeadType.PLAYER);
                        p.getInventory().removeItem(new ItemStack(Material.EMERALD, head.price()));
                        p.getInventory().addItem(head.getItem());

                        p.sendMessage(ChatColor.GREEN + "Your head was added to inventory!");
                    } else
                        p.sendMessage(ChatColor.RED + "Your head hasn't been added yet! Ask " + ChatColor.AQUA + "VannerName" + ChatColor.RED + " to help you with that! :)");
                } else
                    p.sendMessage(ChatColor.AQUA + "You do not have enough emeralds! (You need "+Head.priceFor(Head.HeadType.PLAYER)+" to get your head)");
                
            } else if (args[0].equalsIgnoreCase("get")) {
                if (p.getInventory().containsAtLeast(new ItemStack(Material.EMERALD), Head.priceFor(Head.HeadType.BASIC))) {

                    if (names.contains(fullName(args, 1))) {
                        Head head = new Head(fullName(args, 1), Head.HeadType.BASIC);
                        p.getInventory().addItem(head.getItem());
                        p.getInventory().removeItem(new ItemStack(Material.EMERALD, head.price()));
                        p.sendMessage(ChatColor.GREEN + "Successfully purchased \"" + fullName(args, 1) + "\"!");
                    } else
                        p.sendMessage(ChatColor.RED + "A head with that name does not exist");
                } else
                    p.sendMessage(ChatColor.AQUA + "You do not have enough emeralds! (You need "+Head.priceFor(Head.HeadType.BASIC)+" to buy one head)");
            } else if (args[0].equalsIgnoreCase("refund")) {

                PlayerInventory inv = p.getInventory();
                try {
                    Head head = Head.fromItem(inv.getItemInMainHand());
                    for(int i = 0; i < inv.getItemInMainHand().getAmount(); i++) {
                        inv.addItem(new ItemStack(Material.EMERALD, head.price()));
                    }
                    inv.removeItem(inv.getItemInMainHand());
                    p.sendMessage(ChatColor.GREEN + "Refund successful!");

                } catch (IllegalStateException e) {
                    p.sendMessage(ChatColor.RED + "The item you're holding isn't a player head! Hold the head you want to refund in your main hand and try again!");
                } catch (NullPointerException | IndexOutOfBoundsException e) {
                    p.sendMessage(ChatColor.RED + "This head is corrupted or isn't a player head from the plugin!");
                }

            }
        } else {
            if (args[0].equalsIgnoreCase("add")) {
                if (args.length == 2) {
                    if (!textures.contains(args[1])) {
                        textures.add(args[1]);

                        int i = 1;
                        while (names.contains("head" + i)) i++;
                        names.add("head" + i);

                        loadInConfig(config);

                        sender.sendMessage(ChatColor.GREEN + "Head \"head" + i + "\" successfully added!");

                    } else
                        sender.sendMessage(ChatColor.RED + "Head already exists");
                } else if (args.length >= 3) {
                    if (!textures.contains(args[1])) {
                        textures.add(args[1]);
                        if (fullName(args, 2).contains("texture") || fullName(args, 2).contains("name")) {
                            sender.sendMessage(ChatColor.RED + "Words \"name\" and \"texture\" are reserved for the program and cannot be used.");
                            int i = 1;
                            while (names.contains("head" + i)) i++;
                            names.add("head" + i);
                            sender.sendMessage(ChatColor.RED + "Head will be saved with the name \"" + "head" + i + "\" instead.");
                        } else {
                            names.add(fullName(args, 2));
                            sender.sendMessage(ChatColor.GREEN + "Head \"" + fullName(args, 2) + "\" successfully added!");
                        }

                        loadInConfig(config);

                    } else sender.sendMessage(ChatColor.RED + "A head with this texture already exists!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /heads add <texture value> <name (optional)>");
                }
            } else if (args[0].equalsIgnoreCase("addplayerhead")) {
                if(sender instanceof Player) {
                    sender.sendMessage(ChatColor.RED + "This command can be performed from console only!");
                    return false;
                }

                player_heads.put(args[1], args[2]);
                loadInConfig(config);
                sender.sendMessage("Success");

            } else if (args[0].equalsIgnoreCase("redact")) {
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Incorrect format! (Correct: /heads redact <head name> [texture/name] <new data>)");
                } else {
                    int i = 1;
                    String res = "";
                    while (i < args.length && !(args[i].equals("texture") || args[i].equals("name"))) {
                        res += args[i];
                        res += " ";
                        i++;
                    }
                    if (i == args.length + 1) {
                        sender.sendMessage(ChatColor.RED + "/heads redact <head name> [texture/name] <new data>");
                        return false;
                    }
                    res = res.trim();
                    if (names.contains(res)) {
                        if (args[i].equalsIgnoreCase("texture")) {
                            if (!textures.contains(args[i + 1])) {
                                textures.set(names.indexOf(res), args[i + 1]);
                                sender.sendMessage(ChatColor.GREEN + "Changes saved!");
                            } else
                                sender.sendMessage(ChatColor.RED + "Head with that texture already exists");
                        } else {
                            if (fullName(args, i + 1).contains("texture") || fullName(args, i + 1).contains("name")) {
                                sender.sendMessage(ChatColor.RED + "Words \"name\" and \"texture\" are reserved for the program and cannot be used. ");
                                return false;
                            } else if (!names.contains(fullName(args, i + 1))) {
                                names.set(names.indexOf(res), fullName(args, i + 1));
                                sender.sendMessage(ChatColor.GREEN + "Changes saved!");
                            } else
                                sender.sendMessage(ChatColor.RED + "Head with that name already exists");
                            loadInConfig(config);
                        }
                    } else
                        sender.sendMessage(ChatColor.RED + "Head with that name does not exist");
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (args.length >= 2) {
                    if (names.contains(fullName(args, 1))) {
                        textures.remove(names.indexOf(fullName(args, 1)));
                        names.remove(fullName(args, 1));
                        loadInConfig(config);
                        sender.sendMessage(ChatColor.GREEN + "Removed head \"" + fullName(args, 1) + "\" successfully!");
                    } else
                        sender.sendMessage(ChatColor.RED + "Head with that name does not exist");
                } else
                    sender.sendMessage(ChatColor.RED + "Incorrect format! (Correct: /heads remove <head name>)");
            } else {
                sender.sendMessage(ChatColor.GOLD + "Heads' plugin help page:\n");
                sender.sendMessage(ChatColor.YELLOW + "/heads add <texture value>" + ChatColor.AQUA + " - adds a head to the database. The texture value should be taken from the \"Value\" bar in the \"Other\" section at the end of a head`s page from the site minecraft-heads.com");
                sender.sendMessage(ChatColor.YELLOW + "/heads remove <head name>" + ChatColor.AQUA + " - removes the head from the database");
                sender.sendMessage(ChatColor.YELLOW + "/heads get <head name>" + ChatColor.AQUA + " - sells you a head from the database");
                sender.sendMessage(ChatColor.YELLOW + "/heads getmine" + ChatColor.AQUA + " - gives you your own head (if it was added before; otherwise, ask the admin to do it for you)");
                sender.sendMessage(ChatColor.YELLOW + "/heads redact <head name> [texture/name] <new data>" + ChatColor.AQUA + " - changes either name or the texture value of the specified head");
                sender.sendMessage(ChatColor.YELLOW + "/heads refund" + ChatColor.AQUA + " - returns the emeralds spent on a head. To refund you must hold the specified head in your main hand.");
                sender.sendMessage(ChatColor.YELLOW + "/heads help" + ChatColor.AQUA + " - sends this text");

                return false;
            }
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
