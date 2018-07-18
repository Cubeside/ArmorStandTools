package de.iani.ast;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class WorldGuardProtectionListener implements Listener {
    // private ArmorStandTools plugin;
    private WorldGuardPlugin worldGuard;

    public WorldGuardProtectionListener(ArmorStandTools plugin) {
        // this.plugin = plugin;
        worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
    }

    @EventHandler
    public void onArmorStandTeleport(ArmorStandTeleportEvent event) {
        if (!worldGuard.canBuild(event.getPlayer(), event.getLocation())) {
            event.getPlayer().sendMessage(ChatColor.BLUE + "[AST] " + ChatColor.DARK_RED + "Keine Baurechte!");
            event.setCancelled(true);
        }
    }
}
