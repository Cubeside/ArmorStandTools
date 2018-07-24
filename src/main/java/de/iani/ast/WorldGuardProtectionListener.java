package de.iani.ast;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;

public class WorldGuardProtectionListener implements Listener {
    // private ArmorStandTools plugin;
    private WorldGuardPlugin worldGuard;

    public WorldGuardProtectionListener(ArmorStandTools plugin) {
        // this.plugin = plugin;
        worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
    }

    @EventHandler
    public void onArmorStandTeleport(ArmorStandTeleportEvent event) {
        ApplicableRegionSet set = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(event.getLocation()));
        if (!set.testState(worldGuard.wrapPlayer(event.getPlayer()), Flags.BUILD)) {
            event.getPlayer().sendMessage(ChatColor.BLUE + "[AST] " + ChatColor.DARK_RED + "Keine Baurechte!");
            event.setCancelled(true);
        }
    }
}
