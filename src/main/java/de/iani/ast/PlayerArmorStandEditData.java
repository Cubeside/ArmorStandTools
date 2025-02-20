package de.iani.ast;

import java.text.NumberFormat;
import java.util.ArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;

public class PlayerArmorStandEditData {
    public static enum EditState {
        MainWindow,
        RotationWindow,
        RotationWithoutWindow
    }

    public static enum RotatablePart {
        Head,
        Body,
        LeftArm,
        RightArm,
        LeftLeg,
        RightLeg,
        Position,
        Size
    }

    private ArmorStandTools plugin;
    private Player owner;
    private ArmorStand armorStand;

    private Inventory armorStandInventory;
    private ItemStack aktiv;
    private ItemStack inaktiv;
    private EditState editState;
    private RotatablePart rotationToEdit;
    private Location initialLocation;
    private Location lastLocation;
    private int freeRotationAxis;
    private int freeMoveStartTick;

    // Inventory armorStandEditInventory = plugin.getServer().createInventory(event.getPlayer(), 9 * 6, "Rüstungsständer bearbeiten");
    // event.getPlayer().openInventory(armorStandEditInventory);

    // base plate
    // arms
    // can move
    // small
    // visible
    //
    // item in hand
    // item in off hand
    // helmet,chest,legs,feet
    //
    // postion (x,y,z)
    //
    // body pose (x,y,z)
    // head pose (x,y,z)
    // left arm pose (x,y,z)
    // right arm pose (x,y,z)
    // left leg pose (x,y,z)
    // right leg pose (x,y,z)

    public PlayerArmorStandEditData(ArmorStandTools plugin, Player owner, ArmorStand armorStand) {
        this.plugin = plugin;
        this.owner = owner;
        this.armorStand = armorStand;
        this.editState = EditState.MainWindow;
        aktiv = setItemStackName(new ItemStack(Material.GREEN_DYE), Component.text("aktiv", NamedTextColor.GREEN));
        inaktiv = setItemStackName(new ItemStack(Material.RED_DYE), Component.text("inaktiv", NamedTextColor.RED));
        Component name = armorStand.customName();
        if (name == null) {
            name = Component.text("Rüstungsständer");
        }
        armorStandInventory = plugin.getServer().createInventory(owner, 9 * 6, Component.empty().append(name).append(Component.text(" bearbeiten")));
        editGeneral();
        owner.openInventory(armorStandInventory);
    }

    private void editGeneral() {
        this.editState = EditState.MainWindow;
        armorStandInventory.clear();

        armorStandInventory.setItem(9 * 0, setItemStackLore(setItemStackNameGold(new ItemStack(Material.STONE_PRESSURE_PLATE), "Bodenplatte")));
        updateHasBasePlate();
        armorStandInventory.setItem(9 * 1, setItemStackLore(setItemStackNameGold(new ItemStack(Material.STICK), "Arme")));
        updateHasArms();
        armorStandInventory.setItem(9 * 2, setItemStackLore(setItemStackNameGold(new ItemStack(Material.CLAY_BALL), "Verkleinert")));
        updateIsSmall();
        armorStandInventory.setItem(9 * 3, setItemStackLore(setItemStackNameGold(new ItemStack(Material.RAIL), "Unbeweglich")));
        updateIsUnmoveable();
        armorStandInventory.setItem(9 * 4, setItemStackLore(setItemStackNameGold(new ItemStack(Material.POTION), "Unsichtbar")));
        updateIsInvisible();
        armorStandInventory.setItem(9 * 5, setItemStackLore(setItemStackNameGold(new ItemStack(Material.NAME_TAG), "Name sichtbar"), "Der Name kann mit einem", "Namensschild geändert werden."));
        updateNameIsVisible();

        armorStandInventory.setItem(9 * 0 + 7, setItemStackHideAttributes(setItemStackLore(setItemStackNameGold(new ItemStack(Material.LEATHER_HELMET), "Helm"))));
        armorStandInventory.setItem(9 * 1 + 7, setItemStackHideAttributes(setItemStackLore(setItemStackNameGold(new ItemStack(Material.LEATHER_CHESTPLATE), "Brustpanzer"))));
        armorStandInventory.setItem(9 * 2 + 7, setItemStackHideAttributes(setItemStackLore(setItemStackNameGold(new ItemStack(Material.LEATHER_LEGGINGS), "Hose"))));
        armorStandInventory.setItem(9 * 3 + 7, setItemStackHideAttributes(setItemStackLore(setItemStackNameGold(new ItemStack(Material.LEATHER_BOOTS), "Schuhe"))));
        armorStandInventory.setItem(9 * 4 + 7, setItemStackLore(setItemStackNameGold(new ItemStack(Material.STICK), "Linke Hand")));
        armorStandInventory.setItem(9 * 5 + 7, setItemStackLore(setItemStackNameGold(new ItemStack(Material.STICK), "Rechte Hand")));
        updateArmorstandInventory();
        updateArmorstandInventoryLater();

        armorStandInventory.setItem(9 * 1 + 4, setItemStackHideAttributes(setItemStackLore(setItemStackNameGold(new ItemStack(Material.IRON_HELMET), "Kopfdrehung"))));
        armorStandInventory.setItem(9 * 2 + 4, setItemStackHideAttributes(setItemStackLore(setItemStackNameGold(new ItemStack(Material.IRON_CHESTPLATE), "Körperdrehung"))));
        armorStandInventory.setItem(9 * 2 + 3, setItemStackLore(setItemStackNameGold(new ItemStack(Material.STICK), "Drehung linker Arm")));
        armorStandInventory.setItem(9 * 2 + 5, setItemStackLore(setItemStackNameGold(new ItemStack(Material.STICK), "Drehung rechter Arm")));

        armorStandInventory.setItem(9 * 3 + 4, setItemStackLore(setItemStackNameGold(new ItemStack(Material.IRON_BLOCK), "Position")));
        armorStandInventory.setItem(9 * 4 + 3, setItemStackLore(setItemStackNameGold(new ItemStack(Material.STICK), "Drehung linkes Bein")));
        armorStandInventory.setItem(9 * 4 + 5, setItemStackLore(setItemStackNameGold(new ItemStack(Material.STICK), "Drehung rechtes Bein")));

        armorStandInventory.setItem(9 * 0 + 4, setItemStackLore(setItemStackNameGold(new ItemStack(Material.PUFFERFISH), "Größe")));
    }

    public Inventory getInventory() {
        return armorStandInventory;
    }

    private static ItemStack setItemStackNameGold(ItemStack stack, String name) {
        return setItemStackName(stack, Component.text(name, NamedTextColor.GOLD, TextDecoration.BOLD));
    }

    private static ItemStack setItemStackName(ItemStack stack, String name) {
        return setItemStackName(stack, Component.text(name));
    }

    private static ItemStack setItemStackName(ItemStack stack, Component name) {
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack setItemStackLore(ItemStack stack, String... lore) {
        ItemMeta meta = stack.getItemMeta();
        ArrayList<Component> loreComponents = new ArrayList<>();
        for (String loreLine : lore) {
            loreComponents.add(Component.text(loreLine).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(loreComponents);
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack setItemStackHideAttributes(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        meta.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(NamespacedKey.fromString("armorstandtools:justhide"), 0, Operation.ADD_SCALAR));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        return stack;
    }

    public void updateHasBasePlate() {
        if (editState == EditState.MainWindow) {
            if (armorStand.hasBasePlate()) {
                armorStandInventory.setItem(9 * 0 + 1, aktiv);
            } else {
                armorStandInventory.setItem(9 * 0 + 1, inaktiv);
            }
        }
    }

    public void toggleHasBasePlate() {
        armorStand.setBasePlate(!armorStand.hasBasePlate());
        Messages.sendPart(owner, "Bodenplatte ist nun ", Messages.activation(armorStand.hasBasePlate()));
        updateHasBasePlate();
    }

    public void updateHasArms() {
        if (editState == EditState.MainWindow) {
            if (armorStand.hasArms()) {
                armorStandInventory.setItem(9 * 1 + 1, aktiv);
            } else {
                armorStandInventory.setItem(9 * 1 + 1, inaktiv);
            }
        }
    }

    public void toggleHasArms() {
        armorStand.setArms(!armorStand.hasArms());
        Messages.sendPart(owner, "Arme sind nun ", Messages.activation(armorStand.hasArms()));
        updateHasArms();
    }

    public void updateIsSmall() {
        if (editState == EditState.MainWindow) {
            if (armorStand.isSmall()) {
                armorStandInventory.setItem(9 * 2 + 1, aktiv);
            } else {
                armorStandInventory.setItem(9 * 2 + 1, inaktiv);
            }
        }
    }

    public void toggleIsSmall() {
        armorStand.setSmall(!armorStand.isSmall());
        Messages.sendPart(owner, "Verkleinert ist nun ", Messages.activation(armorStand.isSmall()));
        updateIsSmall();
    }

    public void updateIsUnmoveable() {
        if (editState == EditState.MainWindow) {
            if (!armorStand.hasGravity()) {
                armorStandInventory.setItem(9 * 3 + 1, aktiv);
            } else {
                armorStandInventory.setItem(9 * 3 + 1, inaktiv);
            }
        }
    }

    public void toggleIsUnmoveable() {
        armorStand.setGravity(!armorStand.hasGravity());
        Messages.sendPart(owner, "Unbeweglich ist nun ", Messages.activation(!armorStand.hasGravity()));
        updateIsUnmoveable();
    }

    public void updateIsInvisible() {
        if (editState == EditState.MainWindow) {
            if (!armorStand.isVisible()) {
                armorStandInventory.setItem(9 * 4 + 1, aktiv);
            } else {
                armorStandInventory.setItem(9 * 4 + 1, inaktiv);
            }
        }
    }

    public void toggleIsInvisible() {
        armorStand.setVisible(!armorStand.isVisible());
        Messages.sendPart(owner, "Unsichtbar ist nun ", Messages.activation(!armorStand.isVisible()));
        updateIsInvisible();
    }

    public void updateNameIsVisible() {
        if (editState == EditState.MainWindow) {
            if (armorStand.isCustomNameVisible()) {
                armorStandInventory.setItem(9 * 5 + 1, aktiv);
            } else {
                armorStandInventory.setItem(9 * 5 + 1, inaktiv);
            }
        }
    }

    public void toggleNameIsVisible() {
        armorStand.setCustomNameVisible(!armorStand.isCustomNameVisible());
        Messages.sendPart(owner, "Namenanzeige ist nun ", Messages.activation(armorStand.isCustomNameVisible()));
        updateNameIsVisible();
    }

    public void updateArmorstandInventory() {
        if (editState == EditState.MainWindow) {
            armorStandInventory.setItem(9 * 0 + 8, armorStand.getEquipment().getHelmet());
            armorStandInventory.setItem(9 * 1 + 8, armorStand.getEquipment().getChestplate());
            armorStandInventory.setItem(9 * 2 + 8, armorStand.getEquipment().getLeggings());
            armorStandInventory.setItem(9 * 3 + 8, armorStand.getEquipment().getBoots());
            armorStandInventory.setItem(9 * 4 + 8, armorStand.getEquipment().getItemInOffHand());
            armorStandInventory.setItem(9 * 5 + 8, armorStand.getEquipment().getItemInMainHand());
        }
    }

    private void updateArmorstandInventoryLater() {
        if (editState == EditState.MainWindow) {
            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    updateArmorstandInventory();
                }
            });
        }
    }

    public void updateRotationInventory() {
        double x = 0;
        double y = 0;
        double z = 0;
        double yaw = 0;

        EulerAngle angle = null;
        final boolean isMove = rotationToEdit == RotatablePart.Position;
        String appendix = isMove ? "" : "°";
        if (rotationToEdit == RotatablePart.Head) {
            angle = armorStand.getHeadPose();
        } else if (rotationToEdit == RotatablePart.Body) {
            angle = armorStand.getBodyPose();
        } else if (rotationToEdit == RotatablePart.LeftArm) {
            angle = armorStand.getLeftArmPose();
        } else if (rotationToEdit == RotatablePart.RightArm) {
            angle = armorStand.getRightArmPose();
        } else if (rotationToEdit == RotatablePart.LeftLeg) {
            angle = armorStand.getLeftLegPose();
        } else if (rotationToEdit == RotatablePart.RightLeg) {
            angle = armorStand.getRightLegPose();
        } else if (rotationToEdit == RotatablePart.Size) {
            x = armorStand.getAttribute(Attribute.SCALE).getBaseValue();
            NumberFormat format = NumberFormat.getNumberInstance();
            ItemStack stackx = setItemStackName(new ItemStack(Material.YELLOW_DYE), "Size = " + format.format(x));
            stackx = setItemStackLore(stackx, "Mit Klick auf 1 setzen.");
            armorStandInventory.setItem(9 * 0 + 3, stackx);
            return;
        } else if (isMove) {
            x = armorStand.getLocation().getX();
            y = armorStand.getLocation().getY();
            z = armorStand.getLocation().getZ();
            yaw = armorStand.getLocation().getYaw();
        }
        if (angle != null) {
            x = angle.getX();
            y = angle.getY();
            z = angle.getZ();
        }
        NumberFormat format = NumberFormat.getNumberInstance();
        ItemStack stackx = setItemStackName(new ItemStack(Material.YELLOW_DYE), "x = " + format.format(isMove ? x : radToDegree(x)) + appendix);
        ItemStack stacky = setItemStackName(new ItemStack(Material.YELLOW_DYE), "y = " + format.format(isMove ? y : radToDegree(y)) + appendix);
        ItemStack stackz = setItemStackName(new ItemStack(Material.YELLOW_DYE), "z = " + format.format(isMove ? z : radToDegree(z)) + appendix);
        if (rotationToEdit != RotatablePart.Position) {
            stackx = setItemStackLore(stackx, "Mit Klick auf 0 setzen.");
            stacky = setItemStackLore(stacky, "Mit Klick auf 0 setzen.");
            stackz = setItemStackLore(stackz, "Mit Klick auf 0 setzen.");
        }
        armorStandInventory.setItem(9 * 0 + 3, stackx);
        armorStandInventory.setItem(9 * 1 + 3, stacky);
        armorStandInventory.setItem(9 * 2 + 3, stackz);
        if (isMove) {
            armorStandInventory.setItem(9 * 3 + 3, setItemStackLore(setItemStackName(new ItemStack(Material.YELLOW_DYE), "yaw = " + format.format(yaw) + "°"), "Mit Klick auf 0 setzen."));
            // armorStandInventory.setItem(9 * 4 + 3, setItemStackName(new Dye(DyeColor.YELLOW).toItemStack(1), "pitch = " + format.format(pitch)));
        }
    }

    private void editRotation(RotatablePart part) {
        editState = EditState.RotationWindow;
        rotationToEdit = part;
        armorStandInventory.clear();

        if (part == RotatablePart.Position) {
            for (int row = 0; row < 4; row++) {
                if (row < 3) {
                    armorStandInventory.setItem(9 * row + 0, setItemStackLore(setItemStackName(new ItemStack(Material.RED_DYE), "-1.0"), "Shift+Klick: -0.001"));
                    armorStandInventory.setItem(9 * row + 1, setItemStackLore(setItemStackName(new ItemStack(Material.RED_DYE), "-0.1"), "Shift+Klick: -0.0001"));
                    armorStandInventory.setItem(9 * row + 2, setItemStackLore(setItemStackName(new ItemStack(Material.RED_DYE), "-0.01"), "Shift+Klick: -0.00001"));
                    armorStandInventory.setItem(9 * row + 4, setItemStackLore(setItemStackName(new ItemStack(Material.GREEN_DYE), "+0.01"), "Shift+Klick: +0.00001"));
                    armorStandInventory.setItem(9 * row + 5, setItemStackLore(setItemStackName(new ItemStack(Material.GREEN_DYE), "+0.1"), "Shift+Klick: +0.0001"));
                    armorStandInventory.setItem(9 * row + 6, setItemStackLore(setItemStackName(new ItemStack(Material.GREEN_DYE), "+1.0"), "Shift+Klick: +0.001"));
                    armorStandInventory.setItem(9 * row + 8, setItemStackName(new ItemStack(Material.CYAN_DYE), "Frei bearbeiten"));
                } else {
                    setRowRotationItems(row);
                }
            }
        } else if (part == RotatablePart.Size) {
            int row = 0;
            armorStandInventory.setItem(9 * row + 0, setItemStackLore(setItemStackName(new ItemStack(Material.RED_DYE), "-1.0"), "Shift+Klick: -0.001"));
            armorStandInventory.setItem(9 * row + 1, setItemStackLore(setItemStackName(new ItemStack(Material.RED_DYE), "-0.1"), "Shift+Klick: -0.0001"));
            armorStandInventory.setItem(9 * row + 2, setItemStackLore(setItemStackName(new ItemStack(Material.RED_DYE), "-0.01"), "Shift+Klick: -0.00001"));
            armorStandInventory.setItem(9 * row + 4, setItemStackLore(setItemStackName(new ItemStack(Material.GREEN_DYE), "+0.01"), "Shift+Klick: +0.00001"));
            armorStandInventory.setItem(9 * row + 5, setItemStackLore(setItemStackName(new ItemStack(Material.GREEN_DYE), "+0.1"), "Shift+Klick: +0.0001"));
            armorStandInventory.setItem(9 * row + 6, setItemStackLore(setItemStackName(new ItemStack(Material.GREEN_DYE), "+1.0"), "Shift+Klick: +0.001"));
            armorStandInventory.setItem(9 * row + 8, setItemStackName(new ItemStack(Material.CYAN_DYE), "Frei bearbeiten"));
        } else {
            for (int row = 0; row < 3; row++) {
                setRowRotationItems(row);
            }
        }

        armorStandInventory.setItem(9 * 5 + 0, setItemStackName(new ItemStack(Material.MAGENTA_DYE), "zurück"));

        updateRotationInventory();
    }

    private void setRowRotationItems(int row) {
        armorStandInventory.setItem(9 * row + 0, setItemStackLore(setItemStackName(new ItemStack(Material.RED_DYE), "-90.0°"), "Shift+Klick: -0.1°"));
        armorStandInventory.setItem(9 * row + 1, setItemStackLore(setItemStackName(new ItemStack(Material.RED_DYE), "-10.0°"), "Shift+Klick: -0.01°"));
        armorStandInventory.setItem(9 * row + 2, setItemStackLore(setItemStackName(new ItemStack(Material.RED_DYE), "-1.0°"), "Shift+Klick: -0.001°"));
        armorStandInventory.setItem(9 * row + 4, setItemStackLore(setItemStackName(new ItemStack(Material.GREEN_DYE), "+1.0°"), "Shift+Klick: +0.01°"));
        armorStandInventory.setItem(9 * row + 5, setItemStackLore(setItemStackName(new ItemStack(Material.GREEN_DYE), "+10.0°"), "Shift+Klick: +0.01°"));
        armorStandInventory.setItem(9 * row + 6, setItemStackLore(setItemStackName(new ItemStack(Material.GREEN_DYE), "+90.0°"), "Shift+Klick: +0.1°"));
        armorStandInventory.setItem(9 * row + 8, setItemStackName(new ItemStack(Material.CYAN_DYE), "Frei bearbeiten"));
    }

    private void editRotation(int xyz, double diff) {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(5);

        EulerAngle angle = null;
        if (rotationToEdit == RotatablePart.Head) {
            angle = armorStand.getHeadPose();
        } else if (rotationToEdit == RotatablePart.Body) {
            angle = armorStand.getBodyPose();
        } else if (rotationToEdit == RotatablePart.LeftArm) {
            angle = armorStand.getLeftArmPose();
        } else if (rotationToEdit == RotatablePart.RightArm) {
            angle = armorStand.getRightArmPose();
        } else if (rotationToEdit == RotatablePart.LeftLeg) {
            angle = armorStand.getLeftLegPose();
        } else if (rotationToEdit == RotatablePart.RightLeg) {
            angle = armorStand.getRightLegPose();
        } else if (rotationToEdit == RotatablePart.Size) {
            if (xyz == 0) {
                AttributeInstance scaleAttribute = armorStand.getAttribute(Attribute.SCALE);
                double value = Double.isFinite(diff) ? scaleAttribute.getBaseValue() + diff : 1.0;
                if (value < 1f / 16f) {
                    value = 1f / 16f;
                }
                int max = owner.hasPermission("armorstandtools.unlimitedsize") ? 16 : 4;
                if (value > max) {
                    value = max;
                }
                scaleAttribute.setBaseValue(value);
                if (editState != EditState.RotationWithoutWindow) {
                    Messages.send(owner, "Scale = " + format.format(value));
                }
            }
        } else if (rotationToEdit == RotatablePart.Position) {
            Location location = armorStand.getLocation();
            if (xyz == 0 && Double.isFinite(diff)) {
                location.setX(location.getX() + diff);
                if (editState != EditState.RotationWithoutWindow) {
                    Messages.send(owner, "X = " + format.format(location.getX()));
                }
            } else if (xyz == 1 && Double.isFinite(diff)) {
                location.setY(Math.max(Math.min(location.getY() + diff, location.getWorld().getMaxHeight()), location.getWorld().getMinHeight()));
                if (editState != EditState.RotationWithoutWindow) {
                    Messages.send(owner, "Y = " + format.format(location.getY()));
                }
            } else if (xyz == 2 && Double.isFinite(diff)) {
                location.setZ(location.getZ() + diff);
                if (editState != EditState.RotationWithoutWindow) {
                    Messages.send(owner, "Z = " + format.format(location.getZ()));
                }
            } else if (xyz == 3) {
                location.setYaw(Double.isFinite(diff) ? ((float) (location.getYaw() + radToDegree(diff))) : 0.0f);
                if (editState != EditState.RotationWithoutWindow) {
                    Messages.send(owner, "Yaw = " + format.format(location.getYaw()) + "°");
                }
                // } else if (xyz == 4) {
                // location.setPitch((float) (location.getPitch() + diff * 30));
                // if (editState != EditState.RotationWithoutWindow) {
                // owner.sendMessage(ChatColor.BLUE + "[AST] " + ChatColor.GOLD + "Pitch = " + format.format(location.getPitch()));
                // }
            }
            ArmorStandTeleportEvent event = new ArmorStandTeleportEvent(armorStand, location, owner);
            plugin.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                armorStand.teleport(location);
                if (rotationToEdit == RotatablePart.Position && xyz == 1) {
                    armorStand.setGravity(false);
                }
                // ((CraftArmorStand) armorStand).getHandle().impulse = true;
            }
        }
        if (angle != null) {
            if (xyz == 0) {
                angle = angle.setX(Double.isFinite(diff) ? (angle.getX() + diff) : 0.0f);
                if (editState != EditState.RotationWithoutWindow) {
                    Messages.send(owner, "X = " + format.format(radToDegree(angle.getX())) + "°");
                }
            } else if (xyz == 1) {
                angle = angle.setY(Double.isFinite(diff) ? (angle.getY() + diff) : 0.0f);
                if (editState != EditState.RotationWithoutWindow) {
                    Messages.send(owner, "Y = " + format.format(radToDegree(angle.getY())) + "°");
                }
            } else if (xyz == 2) {
                angle = angle.setZ(Double.isFinite(diff) ? (angle.getZ() + diff) : 0.0f);
                if (editState != EditState.RotationWithoutWindow) {
                    Messages.send(owner, "Z = " + format.format(radToDegree(angle.getZ())) + "°");
                }
            }

            if (rotationToEdit == RotatablePart.Head) {
                armorStand.setHeadPose(angle);
            } else if (rotationToEdit == RotatablePart.Body) {
                armorStand.setBodyPose(angle);
            } else if (rotationToEdit == RotatablePart.LeftArm) {
                armorStand.setLeftArmPose(angle);
            } else if (rotationToEdit == RotatablePart.RightArm) {
                armorStand.setRightArmPose(angle);
            } else if (rotationToEdit == RotatablePart.LeftLeg) {
                armorStand.setLeftLegPose(angle);
            } else if (rotationToEdit == RotatablePart.RightLeg) {
                armorStand.setRightLegPose(angle);
            }
        }

        updateRotationInventory();
    }

    public void onInventoryClicked(int slot, InventoryClickEvent event) {
        int col = slot % 9;
        // int row = slot / 9;
        if (editState == EditState.MainWindow && col == 8) {
            boolean cancel = true;
            if (event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
                if (placeItemInSlot(slot, event.getCursor())) {
                    cancel = false;
                }
            } else if (event.getAction() == InventoryAction.PLACE_ALL) {
                ItemStack stack = new ItemStack(event.getCursor());
                ItemStack existing = armorStandInventory.getItem(slot);
                if (existing == null || existing.isSimilar(stack)) {
                    if (existing != null) {
                        stack.setAmount(stack.getAmount() + existing.getAmount());
                    }
                    if (placeItemInSlot(slot, stack)) {
                        cancel = false;
                    }
                }
            } else if (event.getAction() == InventoryAction.PLACE_ONE) {
                ItemStack stack = new ItemStack(event.getCursor());
                ItemStack existing = armorStandInventory.getItem(slot);
                if (existing == null || existing.isSimilar(stack)) {
                    stack.setAmount(1);
                    if (existing != null) {
                        stack.setAmount(stack.getAmount() + existing.getAmount());
                    }
                    if (placeItemInSlot(slot, stack)) {
                        cancel = false;
                    }
                }
            }
            if (cancel) {
                event.setCancelled(true);
            }
        } else {
            handleModificationSlotClick(slot, event.isShiftClick());
            event.setCancelled(true);
        }
    }

    public void onInventoryDrag(int slot, ItemStack newInSlot, InventoryDragEvent event) {
        int col = slot % 9;
        if (editState == EditState.MainWindow && col == 8) {
            if (!placeItemInSlot(slot, newInSlot)) {
                event.setCancelled(true);
            }
        } else {
            handleModificationSlotClick(slot, false);
            event.setCancelled(true);
        }
    }

    private double degreeToRad(double degree) {
        return degree * (Math.PI / 180.0);
    }

    private double radToDegree(double rad) {
        return rad * (180.0 / Math.PI);
    }

    private void handleModificationSlotClick(int slot, boolean shift) {
        if (editState == EditState.MainWindow) {
            if (slot == 9 * 0 + 1) {
                toggleHasBasePlate();
            } else if (slot == 9 * 1 + 1) {
                toggleHasArms();
            } else if (slot == 9 * 2 + 1) {
                toggleIsSmall();
            } else if (slot == 9 * 3 + 1) {
                toggleIsUnmoveable();
            } else if (slot == 9 * 4 + 1) {
                toggleIsInvisible();
            } else if (slot == 9 * 5 + 1) {
                toggleNameIsVisible();
            } else if (slot == 9 * 1 + 4) {
                editRotation(RotatablePart.Head);
            } else if (slot == 9 * 2 + 4) {
                editRotation(RotatablePart.Body);
            } else if (slot == 9 * 2 + 3) {
                editRotation(RotatablePart.LeftArm);
            } else if (slot == 9 * 2 + 5) {
                editRotation(RotatablePart.RightArm);
            } else if (slot == 9 * 4 + 3) {
                editRotation(RotatablePart.LeftLeg);
            } else if (slot == 9 * 4 + 5) {
                editRotation(RotatablePart.RightLeg);
            } else if (slot == 9 * 3 + 4) {
                editRotation(RotatablePart.Position);
            } else if (slot == 9 * 0 + 4) {
                editRotation(RotatablePart.Size);
            }
        } else if (editState == EditState.RotationWindow) {
            int row = slot / 9;
            int slotInRow = slot % 9;

            int rowsAvailable = 3;
            boolean degree = true;
            if (rotationToEdit == RotatablePart.Position) {
                rowsAvailable = 4;
                if (row < 3) {
                    degree = false;
                }
            } else if (rotationToEdit == RotatablePart.Size) {
                rowsAvailable = 1;
                degree = false;
            }

            if (row >= 0 && row < rowsAvailable) {
                if (slotInRow >= 0 && slotInRow <= 6) {
                    // +/-
                    double add = 0;
                    if (slotInRow == 0) {
                        add = !degree ? -1 : degreeToRad(-90);
                    } else if (slotInRow == 1) {
                        add = !degree ? -0.1 : degreeToRad(-10);
                    } else if (slotInRow == 2) {
                        add = !degree ? -0.01 : degreeToRad(-1);
                    } else if (slotInRow == 3) {
                        add = Double.NaN;
                    } else if (slotInRow == 4) {
                        add = !degree ? 0.01 : degreeToRad(1);
                    } else if (slotInRow == 5) {
                        add = !degree ? 0.1 : degreeToRad(10);
                    } else if (slotInRow == 6) {
                        add = !degree ? 1 : degreeToRad(90);
                    }
                    if (shift && !Double.isNaN(add)) {
                        if (!degree || (slotInRow != 0 && slotInRow != 6)) {
                            add *= 0.001;
                        } else {
                            add = degreeToRad(slotInRow == 0 ? -0.1 : 0.1);
                        }
                    }
                    if (add != 0.0) {
                        editRotation(row, add);
                    }
                }
                if (slotInRow == 8) {
                    editState = EditState.RotationWithoutWindow;
                    initialLocation = owner.getLocation();
                    lastLocation = initialLocation.clone();
                    freeRotationAxis = row;
                    freeMoveStartTick = plugin.getServer().getCurrentTick();
                    owner.closeInventory();
                    Messages.send(owner, "Freie Modifikation aktiviert.");
                    Messages.send(owner, "Bewege dich, um den Rüstungsständer zu verändern.");
                    Messages.sendSuccess(owner, "Linksklick zum Bestätigen.");
                }
            } else if (slot == 9 * 5 + 0) {
                editGeneral();
            }
        }
    }

    private boolean placeItemInSlot(int slot, ItemStack newInSlot) {
        updateArmorstandInventoryLater();
        // we have to check if the item in the display inventory is the item on the armor stand
        // - if it is not the same, it was modified by something else and we cancel and refresh the inv
        if (slot == 9 * 0 + 8) {
            if (ArmorStandTools.itemStackEquals(armorStand.getEquipment().getHelmet(), armorStandInventory.getItem(9 * 0 + 8))) {
                armorStand.getEquipment().setHelmet(newInSlot);
                return true;
            }
        } else if (slot == 9 * 1 + 8) {
            if (ArmorStandTools.itemStackEquals(armorStand.getEquipment().getChestplate(), armorStandInventory.getItem(9 * 1 + 8))) {
                armorStand.getEquipment().setChestplate(newInSlot);
                return true;
            }
        } else if (slot == 9 * 2 + 8) {
            if (ArmorStandTools.itemStackEquals(armorStand.getEquipment().getLeggings(), armorStandInventory.getItem(9 * 2 + 8))) {
                armorStand.getEquipment().setLeggings(newInSlot);
                return true;
            }
        } else if (slot == 9 * 3 + 8) {
            if (ArmorStandTools.itemStackEquals(armorStand.getEquipment().getBoots(), armorStandInventory.getItem(9 * 3 + 8))) {
                armorStand.getEquipment().setBoots(newInSlot);
                return true;
            }
        } else if (slot == 9 * 4 + 8) {
            if (ArmorStandTools.itemStackEquals(armorStand.getEquipment().getItemInOffHand(), armorStandInventory.getItem(9 * 4 + 8))) {
                armorStand.getEquipment().setItemInOffHand(newInSlot);
                return true;
            }
        } else if (slot == 9 * 5 + 8) {
            if (ArmorStandTools.itemStackEquals(armorStand.getEquipment().getItemInMainHand(), armorStandInventory.getItem(9 * 5 + 8))) {
                armorStand.getEquipment().setItemInMainHand(newInSlot);
                return true;
            }
        }
        return false;
    }

    public void onPlayerMove(Location to) {
        if (editState == EditState.RotationWithoutWindow && lastLocation != null && initialLocation != null) {
            if (to.getWorld() != initialLocation.getWorld() || to.distanceSquared(initialLocation) > 15 * 15) {
                plugin.stopEditing(owner, true);
                Messages.sendSuccess(owner, "Rüstungsständerbearbeitung abgebrochen.");
            } else {
                double dx = to.getX() - lastLocation.getX();
                double dz = to.getZ() - lastLocation.getZ();
                double dtotal = (dx + dz) * 0.5;
                if (dtotal != 0) {
                    editRotation(freeRotationAxis, dtotal);
                    lastLocation = to.clone();
                }
            }
        }
    }

    public EditState getEditState() {
        return editState;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public int getFreeMoveStartTick() {
        return freeMoveStartTick;
    }
}