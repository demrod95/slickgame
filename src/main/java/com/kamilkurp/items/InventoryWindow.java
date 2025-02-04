package com.kamilkurp.items;

import com.kamilkurp.Globals;
import com.kamilkurp.KeyInput;
import com.kamilkurp.assets.Assets;
import com.kamilkurp.creatures.PlayerCharacter;
import com.kamilkurp.systems.GameSystem;
import com.kamilkurp.terrain.Area;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

// too many responsibilities
public class InventoryWindow {

    private final Rectangle background = new Rectangle((int)(Globals.SCREEN_WIDTH * 0.1), (int)(Globals.SCREEN_HEIGHT * 0.1), (int)(Globals.SCREEN_WIDTH * 0.6), (int)(Globals.SCREEN_HEIGHT * 0.6));

    private final List<Rectangle> slotList;
    private final List<Rectangle> equipmentSlotList;
    private final List<Rectangle> traderInventorySlotList;


    private boolean inventoryOpen = false;


    private int currentSelected = 0;

    private boolean moving = false;
    private int currentMoved = 0;
    private boolean movingInEquipment = false;

    private final Map<Integer, Item> inventoryItems;


    private Map<Integer, Item> equipmentItems;

    private Map<Integer, Item> traderInventoryItems;


    private final List<String> equipmentSlotNameList;

    private boolean inEquipment = false;

    private boolean inTraderInventory = false;

    private boolean trading = false;

    private final int inventoryRows = 4;
    private final int inventoryColumns = 5;
    private final int inventorySlots = inventoryRows * inventoryColumns;

    private final int space = 10;
    private final int margin = 10;
    private final int slotWidth = 40;
    private final int slotHeight = 40;

    private final int equipmentSlots = 6;

    private final int tradeInventoryRows = 6;
    private final int tradeInventoryColumns = 2;
    private final int tradeInventorySlots = tradeInventoryRows * tradeInventoryColumns;

    private int gold = 0;
    private PlayerCharacter playerCharacter;

    private GameSystem gameSystem;

    public InventoryWindow(GameSystem gameSystem) {
        this.gameSystem = gameSystem;

        slotList = new LinkedList<>();
        equipmentSlotList = new LinkedList<>();
        traderInventorySlotList = new LinkedList<>();

        equipmentSlotNameList = Arrays.asList("Weapon", "Helmet","Body","Gloves","Ring","Boots");

        ItemType.loadItemTypes();

        for (int i = 0; i < inventorySlots; i++) {
            int col = i % inventoryColumns;
            int row = i / inventoryColumns;
            Rectangle slot = new Rectangle(background.getX() + margin + (space + slotWidth) * col,background.getY() + margin + (space + slotHeight) * row, slotWidth, slotHeight);
            slotList.add(slot);
        }

        for (int i = 0; i < equipmentSlots; i++) {
            int col = inventoryColumns + 2;
            Rectangle slot = new Rectangle(background.getX() + margin + (space + slotWidth) * col,background.getY() + margin + (space + slotHeight) * i, slotWidth, slotHeight);
            equipmentSlotList.add(slot);
        }

        for (int i = 0; i < tradeInventorySlots; i++) {
            int col = inventoryColumns + 1 + i % (tradeInventoryColumns);
            int row = i / (tradeInventoryColumns);
            Rectangle slot = new Rectangle(background.getX() + margin + (space + slotWidth) * col,background.getY() + margin + (space + slotHeight) * row + 30, slotWidth, slotHeight);
            traderInventorySlotList.add(slot);
        }

        inventoryItems = new TreeMap<>();

        traderInventoryItems = new TreeMap<>();

    }

    public void render(Graphics g) {
        if (inventoryOpen) {
            g.setColor(Color.darkGray);
            g.fill(background);

            renderInventory(g);

            renderEquipment(g);

            renderTraderInventory(g);

            renderItemDescription(g);
        }

    }

    private void renderTraderInventory(Graphics g) {
        if (trading) {
            for (int i = 0; i < tradeInventorySlots; i++) {
                g.setColor(Color.black);

                if (inTraderInventory) {
                    if (currentSelected == i) {
                        g.setColor(Color.red);
                    }
                }

                g.draw(traderInventorySlotList.get(i));
                if (traderInventoryItems.get(i) != null) {
                    traderInventoryItems.get(i).getItemType().getImage().draw(traderInventorySlotList.get(i).getX(), traderInventorySlotList.get(i).getY(), slotWidth, slotHeight);
                    if (traderInventoryItems.get(i).getQuantity() > 1) {
                        g.setColor(Color.cyan);
                        g.drawString("" + traderInventoryItems.get(i).getQuantity(), traderInventorySlotList.get(i).getX(), traderInventorySlotList.get(i).getY());
                    }
                }
            }
            g.setColor(Color.white);
            g.drawString("Trader:", traderInventorySlotList.get(0).getX() + 5f, background.getY() + 15f);
        }
    }

    public void renderEquipment(Graphics g) {
        if (!trading) {
            for (int i = 0; i < equipmentSlots; i++) {
                g.setColor(Color.black);

                if (moving && currentMoved == i && movingInEquipment) {
                    g.setColor(Color.orange);
                } else if (inEquipment) {
                    if (currentSelected == i) {
                        g.setColor(Color.red);
                    }
                }

                g.draw(equipmentSlotList.get(i));
                if (equipmentItems.get(i) != null) {
                    equipmentItems.get(i).getItemType().getImage().draw(equipmentSlotList.get(i).getX(), equipmentSlotList.get(i).getY(), slotWidth, slotHeight);
                    if (equipmentItems.get(i).getQuantity() > 1) {
                        g.setColor(Color.cyan);
                        g.drawString("" + equipmentItems.get(i).getQuantity(), equipmentSlotList.get(i).getX(), equipmentSlotList.get(i).getY());
                    }
                }

                g.setColor(Color.white);
                g.drawString(equipmentSlotNameList.get(i), equipmentSlotList.get(i).getX() - 60, equipmentSlotList.get(i).getY());
            }
        }
    }

    public void renderItemDescription(Graphics g) {
        g.setColor(Color.white);
        if (inEquipment) {
            Item item = equipmentItems.get(currentSelected);
            if (item != null) {
                Assets.verdanaTtf.drawString(background.getX() + space, background.getY() + margin + (space + slotHeight) * inventoryRows + space, item.getName(), Color.orange);
            }
            if (item != null) {
                g.drawString(item.getItemInformation(false),
                        background.getX() + space, background.getY() + margin + (space + slotHeight) * inventoryRows + space + 25);
            }

        } else if (inTraderInventory) {
            Item item = traderInventoryItems.get(currentSelected);
            if (item != null) {
                Assets.verdanaTtf.drawString(background.getX() + space, background.getY() + margin + (space + slotHeight) * inventoryRows + space, item.getName(), Color.orange);
            }
            if (item != null) {
                g.drawString(item.getItemInformation(true), background.getX() + space, background.getY() + margin + (space + slotHeight) * inventoryRows + space + 25);
            }

        } else {
            Item item = inventoryItems.get(currentSelected);
            if (item != null) {
                Assets.verdanaTtf.drawString(background.getX() + space, background.getY() + margin + (space + slotHeight) * inventoryRows + space, item.getName(), Color.orange);

            }
            if (item != null) {
                g.drawString(item.getItemInformation(false), background.getX() + space, background.getY() + margin + (space + slotHeight) * inventoryRows + space + 25);
            }

        }
}

    public void renderInventory(Graphics g) {
        for (int i = 0; i < inventorySlots; i++) {

            g.setColor(Color.black);

            if (moving && currentMoved == i && !movingInEquipment) {
                g.setColor(Color.orange);
            }
            else if (!inEquipment && !inTraderInventory) {

                if (currentSelected == i){
                    g.setColor(Color.red);
                }
            }

            g.draw(slotList.get(i));
            if (inventoryItems.get(i) != null) {
                inventoryItems.get(i).getItemType().getImage().draw(slotList.get(i).getX(), slotList.get(i).getY(), slotWidth, slotHeight);
                if (inventoryItems.get(i).getQuantity() > 1) {
                    g.setColor(Color.cyan);
                    g.drawString("" + inventoryItems.get(i).getQuantity(), slotList.get(i).getX(), slotList.get(i).getY());
                }
            }
        }

        g.setColor(Color.yellow);
        g.drawString("Gold: " + gold, background.getX() + 5, background.getY() + 20f + (space + slotHeight) * (float)inventorySlots/ inventoryColumns + 110f);


    }


    public void update(KeyInput keyInput) {
        if (keyInput.isKeyPressed(KeyInput.Key.I)) {
            if (!inventoryOpen) {
                openInventory();
            }
            else {
                closeInventory();
            }
        }

        if (inventoryOpen) {

            if (keyInput.isKeyPressed(KeyInput.Key.ESC)) {
                if (!gameSystem.isEscRecently()) {
                    closeInventory();
                    gameSystem.setEscRecently(true);
                }

            }
            if (keyInput.isKeyPressed(KeyInput.Key.W)) {
                if (inEquipment) {
                    if (currentSelected > 0) {
                        currentSelected--;
                    }
                } else if (inTraderInventory) {
                    if (currentSelected >= tradeInventoryColumns) {
                        currentSelected = currentSelected - tradeInventoryColumns;
                    }
                } else {
                    if (currentSelected >= inventoryColumns) {
                        currentSelected = currentSelected - inventoryColumns;
                    }
                }
            }
            if (keyInput.isKeyPressed(KeyInput.Key.A)) {
                if (inEquipment) {
                    if (currentSelected * inventoryColumns + (inventoryColumns - 1) < inventorySlots) {
                        inEquipment = false;
                        currentSelected = currentSelected * inventoryColumns + (inventoryColumns - 1);
                    }
                } else if (inTraderInventory) {
                    if (currentSelected % tradeInventoryColumns == 0) {
                        if (currentSelected / tradeInventoryColumns < inventoryColumns - 1) {
                            inTraderInventory = false;
                            currentSelected = currentSelected / tradeInventoryColumns * inventoryColumns + (inventoryColumns - 1);
                        }
                    }
                    else {
                        if (currentSelected >= 1) currentSelected--;
                    }
                } else {
                    if (currentSelected >= 1) currentSelected--;
                }
            }

            if (keyInput.isKeyPressed(KeyInput.Key.S)) {
                if (inEquipment) {
                    if (currentSelected < equipmentSlots - 1) {
                        currentSelected++;
                    }
                } else if (inTraderInventory) {
                    if (currentSelected <= tradeInventorySlots - tradeInventoryColumns -1) currentSelected = currentSelected + tradeInventoryColumns;

                } else {
                    if (currentSelected <= inventorySlots- inventoryColumns -1) currentSelected = currentSelected + inventoryColumns;
                }
            }
            if (keyInput.isKeyPressed(KeyInput.Key.D)) {
                if (inTraderInventory) {
                    if (currentSelected < tradeInventorySlots -1) currentSelected++;
                }
                else if (!inEquipment) {
                    if ((currentSelected + 1) % inventoryColumns == 0) {
                        if (!trading) {
                            inEquipment = true;
                            currentSelected = currentSelected / inventoryColumns;
                        }
                        else {
                            inTraderInventory = true;
                            currentSelected = currentSelected / inventoryColumns * tradeInventoryColumns;
                        }

                    }
                    else {
                        if (currentSelected < inventorySlots-1) currentSelected++;
                    }

                }

            }
            if (keyInput.isKeyPressed(KeyInput.Key.SPACE)) {
                if (!trading) {
                    if (!moving) {
                        boolean itemExistsInSlot;

                        if (inEquipment) {
                            itemExistsInSlot = equipmentItems.get(currentSelected) != null;
                        } else {
                            itemExistsInSlot = inventoryItems.get(currentSelected) != null;
                        }

                        if (itemExistsInSlot) {
                            currentMoved = currentSelected;
                            moving = true;
                            movingInEquipment = inEquipment;
                        }
                    }
                    else {
                        if (movingInEquipment) {
                            if (inEquipment) {
                                Item from = equipmentItems.get(currentMoved);
                                Item to = equipmentItems.get(currentSelected);

                                String currentEquipmentType = getEquipmentSlotName(currentSelected);

                                if (from == null || from.getItemType().getEquipmentType().equals(currentEquipmentType)) {
                                    equipmentItems.put(currentMoved, to);
                                    equipmentItems.put(currentSelected, from);
                                    moving = false;

                                    playerCharacter.updateAttackType();
                                }
                            }
                            else {
                                Item from = equipmentItems.get(currentMoved);
                                Item to = inventoryItems.get(currentSelected);

                                String currentEquipmentType = getEquipmentSlotName(currentMoved);

                                if (to == null || to.getItemType().getEquipmentType().equals(currentEquipmentType)) {
                                    equipmentItems.put(currentMoved, to);
                                    inventoryItems.put(currentSelected, from);
                                    moving = false;

                                    playerCharacter.updateAttackType();
                                }
                            }
                        }
                        else {
                            if (inEquipment) {
                                Item from = inventoryItems.get(currentMoved);
                                Item to = equipmentItems.get(currentSelected);

                                String currentEquipmentType = getEquipmentSlotName(currentSelected);

                                if (from == null || from.getItemType().getEquipmentType().equals(currentEquipmentType)) {
                                    inventoryItems.put(currentMoved, to);
                                    equipmentItems.put(currentSelected, from);
                                    moving = false;

                                    playerCharacter.updateAttackType();
                                }
                            }
                            else {
                                Item from = inventoryItems.get(currentMoved);
                                Item to = inventoryItems.get(currentSelected);
                                inventoryItems.put(currentMoved, to);
                                inventoryItems.put(currentSelected, from);
                                moving = false;

                                playerCharacter.updateAttackType();
                            }

                        }

                        if (playerCharacter.getHealthPoints() > playerCharacter.getMaxHealthPoints()) playerCharacter.setHealthPoints(playerCharacter.getMaxHealthPoints());
                    }
                }


            }
            if (keyInput.isKeyPressed(KeyInput.Key.E)) {
                if (inventoryOpen) {
                    if (trading) {
                        if (!inTraderInventory) {
                            if (inventoryItems.get(currentSelected) != null) {
                                sellSelectedItem();

                            }
                        } else {
                            if (traderInventoryItems.get(currentSelected) != null) {

                                if (gold - traderInventoryItems.get(currentSelected).getItemType().getWorth() >= 0) {
                                    takeItem(traderInventoryItems.get(currentSelected));
                                    gold -= traderInventoryItems.get(currentSelected).getItemType().getWorth();
                                    traderInventoryItems.remove(currentSelected);
                                }

                            }
                        }


                    }
                    else {

                        if (!inEquipment && !inTraderInventory) {
                            Item item = inventoryItems.get(currentSelected);
                            if (item != null && item.getItemType().isConsumable()) {
                                playerCharacter.useItem(item);
                                if (item.getQuantity() <= 1) {
                                    inventoryItems.remove(currentSelected);
                                }
                                else {
                                    item.setQuantity(item.getQuantity()-1);
                                }


                            }
                        }

                    }
                }
            }
            if (keyInput.isKeyPressed(KeyInput.Key.ALT)) {
                // drop item
                if (inventoryOpen) {
                    if (!inEquipment && !inTraderInventory) {
                        Item item = inventoryItems.get(currentSelected);
                        if (item != null) {
                            PlayerCharacter character = gameSystem.getPlayerCharacter();
                            gameSystem.getLootSystem().spawnLootPile(gameSystem.getCurrentArea(), character.getRect().getCenterX(), character.getRect().getCenterY(), item);
                            inventoryItems.remove(currentSelected);
                        }
                    }
                }

            }

        }
    }

    public String getEquipmentSlotName(int currentSelected) {
        String currentEquipmentType = null;
        if (currentSelected == 0) {
            currentEquipmentType = "weapon";
        }
        if (currentSelected == 1) {
            currentEquipmentType = "helmet";
        }
        if (currentSelected == 2) {
            currentEquipmentType = "body";
        }
        if (currentSelected == 3) {
            currentEquipmentType = "gloves";
        }
        if (currentSelected == 4) {
            currentEquipmentType = "ring";
        }
        if (currentSelected == 5) {
            currentEquipmentType = "boots";
        }
        return currentEquipmentType;
    }

    public void openInventory() {
        inventoryOpen = true;
    }

    public void closeInventory() {
        inventoryOpen = false;
        currentSelected = 0;
        moving = false;
        trading = false;
        inTraderInventory = false;
        inEquipment = false;
        moving = false;
        currentMoved = 0;
        currentSelected = 0;
        movingInEquipment = false;
    }

    public void sellSelectedItem() {
        gold += inventoryItems.get(currentSelected).getItemType().getWorth() * 0.3f;
        inventoryItems.remove(currentSelected);
    }

    public boolean isInventoryOpen() {
        return inventoryOpen;
    }

    public boolean pickUpItem(Item item, List<Item> itemList) {
        ItemType itemType = item.getItemType();

        boolean stackable = itemType.isStackable();

        if (stackable) {
            int invPos = -1;
            for (Map.Entry<Integer, Item> invItem : inventoryItems.entrySet()) {
                if (invItem.getValue() != null && invItem.getValue().getItemType() == itemType) {
                    invPos = invItem.getKey();
                    break;
                }
            }
            if (invPos != -1) {
                // add quantity to existing item
                inventoryItems.get(invPos).setQuantity(inventoryItems.get(invPos).getQuantity() + item.getQuantity());

                if (item.getLootPileBackref().itemList.size() == 1) {
                    item.getLootPileBackref().setVisible(false);
                }
                item.removeFromLoot();
                itemList.remove(item);
                return true;
            }
        }

        for (int i = 0; i < inventorySlots; i++) {
            if (inventoryItems.get(i) == null) { // if slot empty

                inventoryItems.put(i, item);

                if (item.getLootPileBackref() instanceof Treasure) {
                    //register treasure picked up, dont spawn it again for this save
                    try {
                        FileWriter writer = new FileWriter("saves/treasure_collected.sav", true);

                        Area area = item.getLootPileBackref().getArea();

                        Treasure treasure = (Treasure)item.getLootPileBackref();
                        writer.write("treasure " + area.getId() + " " + area.getTreasureList().indexOf(treasure) + "\n");

                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (item.getLootPileBackref().itemList.size() == 1) {
                    item.getLootPileBackref().setVisible(false);
                }
                item.removeFromLoot();
                itemList.remove(item);

                return true;
            }
        }

        return false;
    }

    public boolean takeItem(Item item) {
        ItemType itemType = item.getItemType();

        boolean stackable = itemType.isStackable();

        if (stackable) {
            int invPos = -1;
            for (Map.Entry<Integer, Item> invItem : inventoryItems.entrySet()) {
                if (invItem.getValue() != null && invItem.getValue().getItemType() == itemType) {
                    invPos = invItem.getKey();
                    break;
                }
            }
            if (invPos != -1) {
                // add quantity to existing item
                inventoryItems.get(invPos).setQuantity(inventoryItems.get(invPos).getQuantity() + item.getQuantity());

                return true;
            }
        }

        for (int i = 0; i < inventorySlots; i++) {
            if (inventoryItems.get(i) == null) { // if slot empty

                inventoryItems.put(i, item);

                return true;
            }
        }

        return false;
    }

    public void openTradeWindow() {
        inventoryOpen = true;
        trading = true;
    }

    public boolean isTrading() {
        return trading;
    }

    public void setTraderInventory(List<Item> traderInventory) {
        traderInventoryItems = new TreeMap<>();
        int i = 0;
        for (Item traderItem : traderInventory) {
            traderInventoryItems.put(i, traderItem);
            i++;
        }
    }

    public Map<Integer, Item> getEquipmentItems() {
        return equipmentItems;
    }

    public void setPlayerCharacter(PlayerCharacter playerCharacter) {
        this.playerCharacter = playerCharacter;
        this.equipmentItems = playerCharacter.getEquipmentItems();
    }

    public Map<Integer, Item> getInventoryItems() {
        return inventoryItems;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }
}
