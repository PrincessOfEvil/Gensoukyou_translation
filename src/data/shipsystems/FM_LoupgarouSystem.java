package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicLensFlare;
import data.scripts.util.MagicRender;
import data.utils.FM_Colors;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_LoupgarouSystem extends BaseShipSystemScript {

    public static final float RANGE = 1300f;
    public static final float NUMBER_OF_MISSILE = 14f;

    private boolean missileLaunch = false;

    private float JITTER_TIMER = 0f;



    public float getSystemRange (ShipAPI ship){
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(
                RANGE
        );
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        super.apply(stats, id, state, effectLevel);

        if (!(stats.getEntity() instanceof ShipAPI))return;
        if (Global.getCombatEngine() == null)return;

        ShipAPI ship = (ShipAPI) stats.getEntity();
        ShipAPI target = findTarget(ship);
        CombatEngineAPI engine = Global.getCombatEngine();

        if (target != null && !missileLaunch){
            Vector2f targetloc = target.getLocation();
            float radius = target.getCollisionRadius() * 1.6f;
            Vector2f sizeOfImage = new Vector2f(64f,64f);
            Global.getSoundPlayer().playSound("FM_Nightbugs_expand_1", 1.5f, 0.5f, ship.getLocation(), new Vector2f());

            for (int i = 0; i <= NUMBER_OF_MISSILE; i = i +1){
                Vector2f missileloc = MathUtils.getRandomPointOnCircumference(targetloc,MathUtils.getRandomNumberInRange(radius - 50f,radius + 50f));
                MagicRender.battlespace(
                        Global.getSettings().getSprite("fx", "FM_modeffect_6"),
                        missileloc,
                        new Vector2f(),
                        sizeOfImage,
                        new Vector2f(),
                        0f,
                        0f,
                        Color.WHITE,
                        true,
                        0.1f,
                        0.4f,
                        0.2f
                );
                MagicLensFlare.createSharpFlare(
                        engine,
                        ship,
                        missileloc,
                        14f,
                        56f,
                        0,
                        FM_Colors.FM_RED_EMP_FRINGE,
                        FM_Colors.FM_RED_EMP_CORE
                );
                engine.spawnProjectile(
                        ship,
                        null,
                        "FM_WolfsFang",
                        missileloc,
                        VectorUtils.getAngle(missileloc,targetloc),
                        new Vector2f()
                );
            }
            missileLaunch = true;
        }

        if (effectLevel > 0) {
            if (state != State.IN) {
                JITTER_TIMER += Global.getCombatEngine().getElapsedInLastFrame();
            }
            float shipJitterLevel;
            if (state == State.IN) {
                shipJitterLevel = effectLevel;
            } else {
                float durOut = 0.25f;
                shipJitterLevel = Math.max(0, durOut - JITTER_TIMER) / durOut;
            }
            float maxRangeBonus = 20f;
            float jitterRangeBonus = shipJitterLevel * maxRangeBonus;
            if (shipJitterLevel > 0) {
                //ship.setJitterUnder(KEY_SHIP, JITTER_UNDER_COLOR, shipJitterLevel, 21, 0f, 3f + jitterRangeBonus);
                ship.setJitter(ship, FM_Colors.FM_RED_EXPLOSION, shipJitterLevel, 4, 4f, 0 + jitterRangeBonus);
            }
        }


    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        super.unapply(stats, id);
        missileLaunch = false;
        JITTER_TIMER = 0f;

    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        //if (true) return true;
        ShipAPI target = findTarget(ship);
        return target != null && target != ship;
    }


    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;

        ShipAPI target = findTarget(ship);
        if (target != null && target != ship) {
            return "READY";
        }
        if ((target == null) && ship.getShipTarget() != null) {
            return "OUT OF RANGE";
        }
        return "NO TARGET";
    }

    protected ShipAPI findTarget(ShipAPI ship) {

        float range = getSystemRange(ship);

        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        ShipAPI target = ship.getShipTarget();
        if (target != null) {
            float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
            float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
            if (dist > range + radSum) target = null;
        } else {
            if (player) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), ShipAPI.HullSize.FRIGATE, range, true);
            } else {
                Object test = ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET);
                if (test instanceof ShipAPI && ((ShipAPI) test).getOwner() != ship.getOwner()) {
                    target = (ShipAPI) test;
                    float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
                    float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
                    if (dist > range + radSum) target = null;
                }
            }
            if (target == null) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.FRIGATE, range, true);
            }
        }
        return target;
    }



}
