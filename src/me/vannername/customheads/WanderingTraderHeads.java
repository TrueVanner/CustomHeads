package me.vannername.customheads;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class WanderingTraderHeads implements Listener {
    private Main plugin;
    public WanderingTraderHeads(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    static int[] randomArr = new int[4];
    int r, i;
    boolean stop;

    public void randomizeArr() {
        for(int i = 0; i < 4; i++) randomArr[i] = -1;

        while (randomArr[3] == -1) {
            stop = false;
            r = (int) (Math.random() * Main.textures.size());
            for (i = 0; randomArr[i] != -1; i++) {
                if (randomArr[i] == r) stop = true;
            }

            if(!stop) randomArr[i] = r;

            //trust me, it works. Yes I've spent too much time on it. No, it is not the problem.
        }
    }

    @EventHandler
    public void clickEvent(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof WanderingTrader) {

            if (((WanderingTrader) e.getRightClicked()).getRecipeCount() <= 6) {

                List<MerchantRecipe> newlist = new LinkedList<>();
                newlist.addAll(((WanderingTrader) e.getRightClicked()).getRecipes());

                if (Main.textures.size() > 4) {
                    randomizeArr();
                    for (int i = 0; i < 4; i++) {

                        Head head = new Head(Main.names.get(randomArr[i]), Head.HeadType.FROM_SHOP);

                        MerchantRecipe recipe = new MerchantRecipe(head.getItem(), 2147483647);
                        recipe.addIngredient(new ItemStack(Material.EMERALD, head.price()));
                        newlist.add(recipe);
                    }
                }
                else {
                    for (int i = 0; i < Main.names.size(); i++) {
                        Head head = new Head(Main.names.get(i), Head.HeadType.FROM_SHOP);

                        MerchantRecipe recipe = new MerchantRecipe(head.getItem(), 2147483647);
                        recipe.addIngredient(new ItemStack(Material.EMERALD, head.price()));
                        newlist.add(recipe);
                    }
                }
                ((WanderingTrader) e.getRightClicked()).setRecipes(newlist);
            }
        }
    }
}
