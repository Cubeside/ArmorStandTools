package de.iani.ast;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class ArmorStandTeleportEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final ArmorStand armorStand;
    private final Location location;
    private boolean cancelled;

    public ArmorStandTeleportEvent(ArmorStand armorStand, Location location, Player owner) {
        super(owner);
        this.armorStand = armorStand;
        this.location = location;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
