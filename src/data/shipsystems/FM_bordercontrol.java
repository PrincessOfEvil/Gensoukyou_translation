package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WingRole;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicLensFlare;
import data.utils.FM_Misc;
import data.utils.I18nUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FM_bordercontrol extends BaseShipSystemScript {

    public static final float RANGE = 3000f;


    public static final Color EFFECT_COLOR = new Color(123, 101, 255, 183);

    public static final Color FLARE_1 = new Color(104, 146, 255, 205);
    public static final Color FLARE_2 = new Color(129, 156, 234, 255);


    public static class TargetData {
        public ShipAPI ship;
        public ShipAPI target;

        public TargetData(ShipAPI ship, ShipAPI target) {

            this.ship = ship;
            this.target = target;

        }
    }

    public float getSystemRange (ShipAPI ship){
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(
                RANGE
        );
    }

    private List<ShipAPI> fighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<>();

        for (ShipAPI fighter : FM_Misc.getFighters(carrier)) {
            if (!fighter.isFighter()) continue;
            if (fighter.getWing() == null) continue;
            if (fighter.getWing().getRole() != WingRole.BOMBER) {
                result.add(fighter);
            }
        }
        return result;
    }

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null)return;

        final String targetDataKey = ship.getId() + "_FM_bordercontrol_target_data";

        //Global.getCombatEngine().addFloatingText(ship.getLocation(),state.toString(),10f, Color.WHITE,ship,0f,0f);

        java.lang.Object targetDataObj = Global.getCombatEngine().getCustomData().get(targetDataKey);

        if (effectLevel > 0) {
            ShipAPI target = findTarget(ship);
            Global.getCombatEngine().getCustomData().put(targetDataKey, new FM_bordercontrol.TargetData(ship, target));

            ship.setJitter(ship, EFFECT_COLOR, 2, 3, 1);


            if (effectLevel >= 0.5f && effectLevel < 0.6f) {
                for (ShipAPI fighter_0 : fighters(ship)) {
                    fighter_0.setJitter(fighter_0, EFFECT_COLOR, 4, 6, 3);
                    MagicLensFlare.createSharpFlare(engine, fighter_0, fighter_0.getLocation(), 7, 160, 90, FLARE_1, FLARE_2);
                }
            }

            if (target != null && effectLevel == 1f) {
                //折跃战机
                Vector2f target_loc = target.getLocation();
                for (ShipAPI fighter : fighters(ship)) {
                    Vector2f fighter_loc_n = MathUtils.getRandomPointOnCircumference(target_loc, 200f);
                    fighter.getLocation().set(fighter_loc_n);
                    Global.getSoundPlayer().playSound("system_phase_skimmer", 1f, 1.3f, ship.getLocation(), ship.getVelocity());

                }
            }
        } else if (state == State.IDLE && targetDataObj != null) {
            Global.getCombatEngine().getCustomData().remove(targetDataKey);
        }


        //Global.getCombatEngine().addFloatingText(ship.getLocation(), String.valueOf(findTarget(ship)),20,Color.WHITE,ship,1,1);
    }

    public void unapply(MutableShipStatsAPI stats, String id) {

    }

    //索敌
    protected ShipAPI findTarget(ShipAPI ship) {
        ShipAPI target = ship.getShipTarget();
        float effect_range = getSystemRange(ship);
        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        if (target != null) {
            float distance = Misc.getDistance(target.getLocation(), ship.getLocation());
            if (distance > getSystemRange(ship)) {
                target = null;
            }
        } else {
            if (player) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), ShipAPI.HullSize.FRIGATE, effect_range, true);
            } else {
                Object test = ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET);
                if (test instanceof ShipAPI) {
                    target = (ShipAPI) test;
                    float distance = Misc.getDistance(ship.getLocation(), target.getLocation());
                    if (distance > effect_range) target = null;
                }
                if (target == null) {
                    target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.FRIGATE, effect_range, true);
                }
            }

        }
        return target;
    }
    //UI相关

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (effectLevel > 0) {
            if (index == 0) {
                return new StatusData(I18nUtil.getShipSystemString("FM_BorderControlInfo"), false);
            }
        }

        return null;
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


    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        //if (true) return true;
        ShipAPI target = findTarget(ship);
        return target != null && target != ship;
    }
}
