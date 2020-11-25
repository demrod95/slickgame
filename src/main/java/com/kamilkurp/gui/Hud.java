package com.kamilkurp.gui;

import com.kamilkurp.Globals;
import com.kamilkurp.creatures.Creature;
import com.kamilkurp.creatures.NonPlayerCharacter;
import com.kamilkurp.creatures.PlayerCharacter;
import com.kamilkurp.spawn.PlayerRespawnPoint;
import com.kamilkurp.systems.GameSystem;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

public class Hud {
    Rectangle bottomRect;
    Rectangle rightRect;
    Rectangle maxHealthRect;
    Rectangle healthRect;
    Rectangle maxStaminaRect;
    Rectangle staminaRect;

    Color color;

    GameSystem gameSystem;

    public Hud(GameSystem gameSystem) {
        this.gameSystem = gameSystem;
        int w = Globals.SCREEN_WIDTH;
        int h = Globals.SCREEN_HEIGHT;
        float proportion = Globals.SCREEN_PROPORTION;
        bottomRect = new Rectangle(0, h * proportion, w, h - h * proportion);
        rightRect = new Rectangle(w * proportion, 0, w - w * proportion, h);
        color = new Color(20,15,20);

        if (gameSystem != null) {
            PlayerCharacter pc = gameSystem.getPlayerCharacter();
            maxHealthRect = new Rectangle(10, h * proportion - 40, 100, 10);
            healthRect = new Rectangle(10, h * proportion - 40, 100 * pc.getHealthPoints()/pc.getMaxHealthPoints(), 10);

            maxStaminaRect = new Rectangle(10, h * proportion - 25, 100, 10);
            staminaRect = new Rectangle(10, h * proportion - 25, 100 * pc.getHealthPoints()/pc.getMaxHealthPoints(), 10);
        }
    }

    public void render(Graphics g) {
        g.setColor(color);
        g.fill(bottomRect);
        g.fill(rightRect);

        if (gameSystem != null && gameSystem.getPlayerCharacter() != null) {
            g.setColor(Color.orange);
            g.fill(maxHealthRect);

            g.setColor(Color.red);
            g.fill(healthRect);

            g.setColor(Color.cyan);
            g.fill(maxStaminaRect);

            g.setColor(Color.green);
            g.fill(staminaRect);
        }

        if (gameSystem != null && !gameSystem.getDialogueWindow().isActivated()) {
            g.setColor(Color.white);

            String triggerMessage = "";

            for (Creature creature : gameSystem.getCurrentArea().getCreatures().values()) {
                if (creature == gameSystem.getPlayerCharacter()) continue;
                if (gameSystem.getPlayerCharacter().getRect().intersects(creature.getRect()) && creature instanceof NonPlayerCharacter && creature.getHealthPoints() > 0) {
                    triggerMessage = "> Talk";
                }
            }
            for (PlayerRespawnPoint playerRespawnPoint : gameSystem.getCurrentArea().getRespawnList()) {
                if (gameSystem.getPlayerCharacter().getRect().intersects(playerRespawnPoint.getRect())) {
                    triggerMessage = "> Set respawn";
                }
            }

            g.drawString(triggerMessage, 10, Globals.SCREEN_HEIGHT * Globals.SCREEN_PROPORTION + 10);

        }
    }

    public void update() {
        int w = Globals.SCREEN_WIDTH;
        int h = Globals.SCREEN_HEIGHT;
        float proportion = Globals.SCREEN_PROPORTION;
        PlayerCharacter pc = gameSystem.getPlayerCharacter();
        healthRect = new Rectangle(10, h * proportion - 40, 100 * pc.getHealthPoints()/pc.getMaxHealthPoints(), 10);
        staminaRect = new Rectangle(10, h * proportion - 25, 100 * pc.getStaminaPoints()/pc.getMaxStaminaPoints(), 10);

    }
}
