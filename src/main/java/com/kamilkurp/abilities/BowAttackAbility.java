package com.kamilkurp.abilities;

import com.kamilkurp.animations.AttackAnimation;
import com.kamilkurp.assets.Assets;
import com.kamilkurp.creatures.AttackType;
import com.kamilkurp.creatures.Creature;
import com.kamilkurp.projectile.Arrow;
import com.kamilkurp.terrain.TerrainTile;
import com.kamilkurp.utils.Camera;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Sound;
import org.newdawn.slick.geom.Vector2f;

import java.util.List;
import java.util.Map;

public class BowAttackAbility extends Ability {
    protected AttackAnimation swordAttackAnimation;
    private final Sound bowReleaseSound = Assets.bowReleaseSound;
    private final Sound bowPullSound = Assets.bowPullSound;


    private BowAttackAbility(Creature abilityCreature) {
        super(abilityCreature);

        attackType = AttackType.BOW;
    }

    @Override
    public void init() {
        cooldownTime = 1500;
        activeTime = 300;
        channelTime = 500;
    }

    @Override
    protected void onActiveStart() {

        bowReleaseSound.play(1.0f, 0.1f);

        abilityCreature.setAttackingVector(abilityCreature.getFacingVector());

        List<Arrow> arrowList = abilityCreature.getArea().getArrowList();
        List<TerrainTile> tiles = abilityCreature.getArea().getTiles();
        Map<String, Creature> areaCreatures = abilityCreature.getArea().getCreatures();

        if (!abilityCreature.getFacingVector().equals(new Vector2f(0.f, 0f))) {
            Arrow arrow = new Arrow(abilityCreature.getRect().getX(), abilityCreature.getRect().getY(), abilityCreature.getArea(), abilityCreature.getFacingVector(), arrowList, tiles, areaCreatures, this.abilityCreature);
            arrowList.add(arrow);
        }

        abilityCreature.takeStaminaDamage(40f);

    }

    public void render(Graphics g, Camera camera) {

    }

    @Override
    public void onChannellingStart() {
        bowPullSound.play(1.0f, 0.3f);
        abilityCreature.setAttacking(true);
    }

    public static BowAttackAbility newInstance(Creature abilityCreature) {
        BowAttackAbility ability = new BowAttackAbility(abilityCreature);
        ability.init();
        ability.setTimerStartingPosition();
        return ability;
    }

    @Override
    protected void onStop() {
        abilityCreature.setAttacking(false);
    }
}
