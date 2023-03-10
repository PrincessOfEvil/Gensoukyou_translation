package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import data.utils.I18nUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FM_eastwind extends BaseShipSystemScript {

    public static final float RANGE = 800f;
    //五角星的端点
    public Map<Integer, Vector2f> NODES = new HashMap<>();
    //五条边
    public Map<Integer, Vector2f> LINES = new HashMap<>();
    //边上的点的集合
    public Map<Vector2f, List<Vector2f>> POINTS = new HashMap<>();
    //每个弹头的飞行方向
    public Map<Vector2f, Float> DIRECTION = new HashMap<>();


    private boolean EFFECT = false;
    private float TIMER = 0;
    private float FIR_ANGLE = 0;

    public float getSystemRange (ShipAPI ship){
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(
                RANGE
        );
    }


    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        if (Global.getCombatEngine() == null)return;


        ShipAPI the_ship = (ShipAPI) stats.getEntity();

        //弹道偏转相关
        List<CombatEntityAPI> entities = CombatUtils.getEntitiesWithinRange(the_ship.getLocation(), getSystemRange(the_ship));
        for (CombatEntityAPI entity : entities) {
            Vector2f entity_vel = entity.getVelocity();
            float vel_length = entity_vel.length();

            Vector2f PS = new Vector2f();
            Vector2f.sub(the_ship.getLocation(), entity.getLocation(), PS);

            float facing_1 = VectorUtils.getFacing(PS);
            float facing_2 = VectorUtils.getFacing(entity_vel);


            if (entity instanceof DamagingProjectileAPI && entity.getOwner() != the_ship.getOwner() && effectLevel > 0f && !(entity instanceof MissileAPI)) {

                if (facing_2 < 90f && facing_1 > 270f) {
                    CombatUtils.applyForce(entity, facing_1 + 90f, 4f);
                    if (facing_2 + 360f - facing_1 < 30f) {
                        entity.setFacing(facing_2 + 0.2f);
                    }
                } else if (facing_2 > 270f && facing_1 < 90f) {
                    CombatUtils.applyForce(entity, facing_1 - 90f, 4f);
                    if (facing_1 + 360f - facing_2 < 30f) {
                        entity.setFacing(facing_2 - 0.2f);
                    }
                } else if (facing_1 > facing_2) {
                    CombatUtils.applyForce(entity, facing_1 - 90f, 4f);
                    if (facing_1 - facing_2 < 30f) {
                        entity.setFacing(facing_2 - 0.2f);
                    }
                } else {
                    CombatUtils.applyForce(entity, facing_1 + 90f, 4f);
                    if (facing_2 - facing_1 < 30f) {
                        entity.setFacing(facing_2 + 0.2f);
                    }

                }
                VectorUtils.clampLength(entity_vel, vel_length);
            }
        }


        //计时
        TIMER = TIMER + Global.getCombatEngine().getElapsedInLastFrame();

        if (TIMER >= 1) {
            EFFECT = !EFFECT;
            TIMER = 0;
            FIR_ANGLE = FIR_ANGLE + 27f;
        }


        if (EFFECT) {
            //五角星生成

            for (int i = 0; i < 5; i = i + 1) {
                float r = the_ship.getCollisionRadius();
                float direction = the_ship.getFacing();
                Vector2f node = MathUtils.getPoint(the_ship.getLocation(), r, FIR_ANGLE + 72 * i);
                NODES.put(i, node);
            }
            for (int i = 0; i < 5; i = i + 1) {
                Vector2f line = new Vector2f();
                if (i <= 2) {
                    Vector2f.sub(NODES.get(i + 2), NODES.get(i), line);
                } else {
                    Vector2f.sub(NODES.get(i - 3), NODES.get(i), line);
                }
                LINES.put(i, line);
            }

            for (int i = 0; i < 5; i++) {
                Vector2f line = LINES.get(i);
                List<Vector2f> points = new ArrayList<>();
                for (int k = 1; k < 11; k++) {
                    Vector2f point = new Vector2f();
                    VectorUtils.resize(line, line.length() * k / 11, point);

                    Vector2f point_0 = new Vector2f();
                    Vector2f.add(NODES.get(i), point, point_0);
                    points.add(point_0);

                }
                points.add(NODES.get(i));
                POINTS.put(line, points);
            }

            //生成弹头
            CombatEngineAPI engine = Global.getCombatEngine();
            ShipAPI enemy = Misc.findClosestShipEnemyOf(the_ship, the_ship.getLocation(), ShipAPI.HullSize.FRIGATE, getSystemRange(the_ship), true);

            //if ( enemy != null){
            for (int i = 0; i < 5; i++) {
                for (Vector2f point : POINTS.get(LINES.get(i))) {
                    if (i <= 1) {
                        DIRECTION.put(point, VectorUtils.getAngle(point, NODES.get(i + 3)));
                    } else {
                        DIRECTION.put(point, VectorUtils.getAngle(point, NODES.get(i - 2)));
                    }


                }
            }
            //}else {
            //    for (int i = 0; i < 5 ; i++){
            //        for (Vector2f point : POINTS.get(LINES.get(i))){
            //            DIRECTION.put(point,the_ship.getFacing());
            //        }
            //    }

            //}
            for (int i = 0; i < 5; i++) {
                for (Vector2f point : POINTS.get(LINES.get(i))) {
                    engine.spawnProjectile(the_ship, null, "FM_accball", point, DIRECTION.get(point), null);
                    Global.getSoundPlayer().playSound("FM_Nightbugs_expand_1", 2f, 0.25f, point, new Vector2f());
                }
            }

            EFFECT = !EFFECT;
        }


    }

    public void unapply(MutableShipStatsAPI stats, String id) {

        TIMER = 0;
        FIR_ANGLE = 0;

    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(I18nUtil.getShipSystemString("FM_EastWindInfo0"), false);
        } else if (index == 1) {
            return new StatusData(I18nUtil.getShipSystemString("FM_EastWindInfo1"), false);
        }
        return null;
    }


}
