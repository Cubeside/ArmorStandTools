package de.iani.ast;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
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
        if (!canBuild(event.getPlayer(), set)) {
            event.getPlayer().sendMessage(ChatColor.BLUE + "[AST] " + ChatColor.DARK_RED + "Keine Baurechte!");
            event.setCancelled(true);
        }
    }

    private boolean canBuild(Player bplayer, ApplicableRegionSet set) {
        LocalPlayer player = worldGuard.wrapPlayer(bplayer);
        return hasBypass(player, bplayer.getWorld()) || set.testState(player, Flags.BUILD);
    }

    private boolean hasBypass(LocalPlayer wgPlayer, World world) {
        return new RegionPermissionModel(wgPlayer).mayIgnoreRegionProtection(BukkitAdapter.adapt(world));
    }
}
