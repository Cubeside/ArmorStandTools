package de.iani.ast;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class Messages {
    private Messages() {
        throw new RuntimeException();
    }

    public static Component activation(boolean b) {
        return b ? Component.text("AKTIV", NamedTextColor.GREEN) : Component.text("INAKTIV", NamedTextColor.RED);
    }

    public static void sendSuccess(CommandSender target, String message) {
        sendSuccess(target, Component.text(message));
    }

    public static void sendSuccess(CommandSender target, Component message) {
        message = message.color(NamedTextColor.GREEN);
        send(target, message);
    }

    public static void sendPart(CommandSender target, String message, Component part) {
        send(target, Component.text(message).append(part));
    }

    public static void sendError(CommandSender target, String message) {
        sendError(target, Component.text(message));
    }

    public static void sendError(CommandSender target, Component message) {
        message = message.color(NamedTextColor.DARK_RED);
        send(target, message);
    }

    public static void send(CommandSender target, String message) {
        send(target, Component.text(message));
    }

    public static void send(CommandSender target, Component message) {
        if (message.color() == null) {
            message = message.color(NamedTextColor.GOLD);
        }
        target.sendMessage(Component.text("[AST] ", NamedTextColor.BLUE).append(message));
    }
}
