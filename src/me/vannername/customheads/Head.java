package me.vannername.customheads;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;


public class Head {
    private String texture;
    private String name;
    private HeadType type;

    public enum HeadType {
        BASIC,
        PLAYER,
        FROM_SHOP
    }

    public Head(String name, HeadType type) {
        this.name = name;
        this.type = type;

        if (this.type == HeadType.PLAYER)
            this.texture = Main.player_heads.get(this.name);
        else
            this.texture = Main.textures.get(Main.names.indexOf(this.name));

    }

    public Head(String name, String texture, HeadType type) {
        this.name = name;
        this.texture = texture;
        this.type = type;
    }

    static Head fromItem(ItemStack i) throws NullPointerException, IllegalStateException {
        if(i.getType() == Material.PLAYER_HEAD) {
            ItemMeta meta = i.getItemMeta();

            return new Head(meta.getDisplayName(), Head.getType(meta.getCustomModelData()));

        } else throw new IllegalStateException("The item provided is not the player head");
    }

    public static HeadType getType(int cmd) {
        switch (cmd) {
            case 123 -> {return HeadType.BASIC;}
            case 456 -> {return HeadType.PLAYER;}
            case 789 -> {return HeadType.FROM_SHOP;}
        }
        return HeadType.FROM_SHOP;
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1, (short) 3);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.setDisplayName(name);

        switch (this.type) {
            case BASIC -> meta.setCustomModelData(123);
            case PLAYER -> meta.setCustomModelData(456);
            case FROM_SHOP -> meta.setCustomModelData(789);
        }

        if (!texture.equals("")) {
            GameProfile profile = new GameProfile(UUID.fromString("c090b961-3f9b-47de-a1a7-d4fc0fa6bacc"), null); // static UUID to let the heads stack
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

    static int priceFor(HeadType type) {
        switch (type) {
            case BASIC -> {return 4;}
            case PLAYER -> {return 2;}
            case FROM_SHOP -> {return 1;}
        }
        return 1;
    }

    public int price() {
        return Head.priceFor(this.type);
    }
}
