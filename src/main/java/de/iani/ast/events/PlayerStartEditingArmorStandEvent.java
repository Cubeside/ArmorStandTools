package de.iani.ast.events;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerStartEditingArmorStandEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled;
    private ArmorStand armorStand;

    public PlayerStartEditingArmorStandEvent(Player who, ArmorStand armorStand) {
        super(who);
        this.armorStand = armorStand;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
