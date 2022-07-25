package me.vannername.customheads;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;


public class Head {
    private String texture;
    private String name;

    public Head(String texture) {
        this.texture = texture;
        this.name = "";
    }
    public Head(String texture, String name) {
        this.texture = texture;
        this.name = name;
    }

    public String getName() {return this.name;}
    public String getTexture() {return this.texture;}

    public void setName(String name) {this.name = name;}
    public void setTexture(String texture) {this.texture = texture;}

    public ItemStack getItem() {

        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1, (short) 3);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.setCustomModelData(123);
        meta.setDisplayName(name);
        if (!texture.equals("")) {
            GameProfile profile = new GameProfile(UUID.fromString("c090b961-3f9b-47de-a1a7-d4fc0fa6bacc"), null);
            profile.getProperties().put("textures", new Property("textures", texture, ""));

            Field profileField = null;
            try {
                profileField = meta.getClass().getDeclaredField("profile");
            } catch (NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
            profileField.setAccessible(true);
            try {
                profileField.set(meta, profile);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        item.setItemMeta(meta);
        return item;
    }
}
