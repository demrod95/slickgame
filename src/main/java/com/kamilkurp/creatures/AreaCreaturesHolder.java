package com.kamilkurp.creatures;

import com.kamilkurp.KeyInput;
import com.kamilkurp.areagate.AreaGate;
import com.kamilkurp.dialogue.DialogueWindow;
import com.kamilkurp.gui.LootOptionWindow;
import com.kamilkurp.items.InventoryWindow;
import com.kamilkurp.items.Item;
import com.kamilkurp.terrain.Area;
import com.kamilkurp.terrain.CurrentAreaManager;
import com.kamilkurp.utils.Camera;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class AreaCreaturesHolder {
    private Map<String, Creature> creaturesMap;
    private Area area;

    private Queue<Creature> renderPriorityQueue;


    public AreaCreaturesHolder(Area area) {
        this.area = area;
        creaturesMap = new HashMap<>();
    }

    public void onAreaChange() {
        for (Creature creature : creaturesMap.values()) {
            if (!(creature instanceof PlayerCharacter || creature instanceof NonPlayerCharacter)) {
                if (!creature.isAlive()) {
                    creature.markForDeletion();
                }

            }
        }

        creaturesMap.entrySet().removeIf(e -> e.getValue().isToBeRemoved());
    }

    public void updateCreatures(InventoryWindow inventoryWindow, LootOptionWindow lootOptionWindow, DialogueWindow dialogueWindow, GameContainer gc, int i, KeyInput keyInput, List<AreaGate> gateList) throws SlickException {
        for (Creature creature : creaturesMap.values()) {
            if (creature instanceof PlayerCharacter) {
                if (!inventoryWindow.isInventoryOpen() && !lootOptionWindow.isActivated() && !dialogueWindow.isActivated()) {
                    creature.update(gc, i, area.getTiles(), creaturesMap, keyInput, area.getArrowList(), gateList);

                    creature.areaGateLogic(gateList);
                }
            }
            else {
                creature.update(gc, i, area.getTiles(), creaturesMap, keyInput, area.getArrowList(), gateList);
            }

        }
    }

    public void processAreaChanges(List<Creature> creaturesToMove) throws SlickException {
        for (Creature creature : creaturesMap.values()) {
            if (creature.getPendingArea() != null) {
                creaturesToMove.add(creature);
            }
        }

        area.updateSpawns();
    }

    public void updateRenderPriorityQueue() {
        renderPriorityQueue = new PriorityQueue<>((o1, o2) -> {
            if (o1.getHealthPoints() <= 0.0f) return -1;
            if (o2.getHealthPoints() <= 0.0f) return 1;
            if (o1.getRect().getY() == o2.getRect().getY()) return 0;
            return (o1.getRect().getY() - o2.getRect().getY() > 0.0f) ? 1 : -1;
        });

        renderPriorityQueue.addAll(creaturesMap.values());
    }

    public void renderCreatures(Graphics g, Camera camera) {
        if (renderPriorityQueue != null) {
            while (!renderPriorityQueue.isEmpty()) {
                Creature creature = renderPriorityQueue.poll();

                creature.render(g, camera);
            }

        }


        for (Creature creature : creaturesMap.values()) {
            creature.renderAttackAnimation(g, camera);
        }
    }

    public void saveToFile(FileWriter writer) throws IOException {
        for (Creature creature : creaturesMap.values()) {
            if (creature.getClass() != PlayerCharacter.class && creature.getClass() != NonPlayerCharacter.class) continue;
            writer.write("creature " + creature.getId() + "\n");
            writer.write("pos " + creature.getRect().getX() + " " + creature.getRect().getY() + "\n");
            writer.write("area " + creature.getArea().getId() + "\n");
            writer.write("health " + creature.getHealthPoints() + "\n");

            Map<Integer, Item> equipmentItems = creature.getEquipmentItems();

            for (Map.Entry<Integer, Item> equipmentItem : equipmentItems.entrySet()) {
                if (equipmentItem.getValue() != null) {
                    String damage = equipmentItem.getValue().getDamage() == null ? "0" : "" + equipmentItem.getValue().getDamage().intValue();

                    String armor = equipmentItem.getValue().getArmor() == null ? "0" : "" + equipmentItem.getValue().getArmor().intValue();
                    writer.write("equipment_item " + equipmentItem.getKey() + " " + equipmentItem.getValue().getItemType().getId() + " " + damage + " " + armor + "\n");
                }
            }
        }
    }

    public Creature getCreatureById(String id) {
        return creaturesMap.get(id);
    }

    public void insertCreature(Creature creature) {
        creaturesMap.put(creature.getId(), creature);
    }

    public void updateAttackTypes() {
        for (Creature creature : creaturesMap.values()) {
            creature.updateAttackType();
        }
    }

    public void removeCreature(String id) {
        creaturesMap.remove(id);

    }

    public void updateGatesLogic(AreaGate areaGate, CurrentAreaManager currentAreaManager) {
        for (Creature creature : creaturesMap.values()) {
            if (creature instanceof PlayerCharacter) {
                if (!creature.isPassedGateRecently()) {
                    Rectangle gateRect = null;
                    Area destinationArea = null;
                    Area oldArea = null;
                    Rectangle destinationRect = null;
                    if (area == areaGate.getAreaFrom()) {
                        gateRect = areaGate.getFromRect();
                        oldArea = areaGate.getAreaFrom();
                        destinationArea = areaGate.getAreaTo();
                        destinationRect = areaGate.getToRect();
                    }
                    if (area == areaGate.getAreaTo()) {
                        gateRect = areaGate.getToRect();
                        oldArea = areaGate.getAreaTo();
                        destinationArea = areaGate.getAreaFrom();
                        destinationRect = areaGate.getFromRect();
                    }

                    if (creature.getRect().intersects(gateRect)) {
                        creature.setPassedGateRecently(true);

                        creature.transport(destinationArea, destinationRect.getX(), destinationRect.getY());

                        currentAreaManager.setCurrentArea(destinationArea);


                        oldArea.onLeave();
                        destinationArea.onEntry();

                    }
                }
            }
        }
    }
}
