package com.me4502.armourclicker;

import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class ArmourClicker extends JavaPlugin implements Listener {

    private Map<String, String> ownerToServer = Maps.newHashMap();

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getServer().getPluginManager().registerEvents(this, this);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        try {
            getConfig().getConfigurationSection("head-server-map").getValues(false).entrySet().forEach(entry -> {
                ownerToServer.put(entry.getKey(), String.valueOf(entry.getValue()));
            });
        } catch (Exception e) {}

        if (ownerToServer.isEmpty()) {
            ownerToServer.put("Me4502", "lobby");
            getConfig().set("head-server-map", ownerToServer);
            saveConfig();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getConfig().set("head-server-map", ownerToServer);
        saveConfig();
    }

    @EventHandler
    public void onClickEntity(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getRightClicked();
            ItemStack helmet = armorStand.getHelmet();
            if (helmet != null && (helmet.getType() == Material.SKULL || helmet.getType() == Material.SKULL_ITEM) && helmet.hasItemMeta()) {
                SkullMeta skullMeta = (SkullMeta) helmet.getItemMeta();
                if (skullMeta.hasOwner()) {
                    String server = ownerToServer.get(skullMeta.getOwner());
                    if (server != null) {
                        sendPlayerToServer(event.getPlayer(), server);
                    }
                }
            }
        }
    }

    public void sendPlayerToServer(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);

        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }
}
