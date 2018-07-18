package de.iani.ast;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Objects;

public class ArmorStandTools extends JavaPlugin {

    private HashMap<UUID, PlayerArmorStandEditData> armorStandEdits;

    @Override
    public void onEnable() {
        armorStandEdits = new HashMap<UUID, PlayerArmorStandEditData>();
        getServer().getPluginManager().registerEvents(new ArmorStandListener(this), this);

        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            getServer().getPluginManager().registerEvents(new WorldGuardProtectionListener(this), this);
        }
    }

    public void startEditing(Player player, ArmorStand armorstand) {
        if (!armorStandEdits.containsKey(player.getUniqueId())) {
            PlayerArmorStandEditData data = new PlayerArmorStandEditData(this, player, armorstand);
            armorStandEdits.put(player.getUniqueId(), data);
        }
    }

    public void stopEditing(Player player, boolean stopIfFreeMove) {
        PlayerArmorStandEditData old = armorStandEdits.get(player.getUniqueId());
        if (old != null) {
            if (stopIfFreeMove || old.getEditState() != PlayerArmorStandEditData.EditState.RotationWithoutWindow) {
                armorStandEdits.remove(player.getUniqueId());
                old.getInventory().clear();
                player.closeInventory();
            }
        }
    }

    public PlayerArmorStandEditData getEditData(Player player) {
        return armorStandEdits.isEmpty() ? null : armorStandEdits.get(player.getUniqueId());
    }

    public void checkArmorStandNoEquip(ArmorStand armorStand) {
        if (!armorStand.isVisible()) {
            boolean hasEquip = false;
            EntityEquipment equip = armorStand.getEquipment();
            hasEquip = hasEquip || !ArmorStandTools.itemStackEquals(equip.getHelmet(), null);
            hasEquip = hasEquip || !ArmorStandTools.itemStackEquals(equip.getChestplate(), null);
            hasEquip = hasEquip || !ArmorStandTools.itemStackEquals(equip.getLeggings(), null);
            hasEquip = hasEquip || !ArmorStandTools.itemStackEquals(equip.getBoots(), null);
            hasEquip = hasEquip || !ArmorStandTools.itemStackEquals(equip.getItemInMainHand(), null);
            hasEquip = hasEquip || !ArmorStandTools.itemStackEquals(equip.getItemInOffHand(), null);
            if (!hasEquip) {
                armorStand.setVisible(true);
            }
        }
    }

    public void checkArmorStandNoEquipLater(ArmorStand armorStand) {
        if (!armorStand.isVisible()) {
            getServer().getScheduler().runTask(this, new Runnable() {
                @Override
                public void run() {
                    checkArmorStandNoEquip(armorStand);
                }
            });
        }
    }

    public static boolean itemStackEquals(ItemStack stack1, ItemStack stack2) {
        if (stack1 != null && stack1.getType() == Material.AIR) {
            stack1 = null;
        }
        if (stack2 != null && stack2.getType() == Material.AIR) {
            stack2 = null;
        }
        return Objects.equal(stack1, stack2);
    }
}
