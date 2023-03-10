package data.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import data.shipsystems.FM_Nocturne;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class FM_Nocturne_ai implements ShipSystemAIScript {

    private ShipwideAIFlags flags;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private ArrayList<MissileAPI> dangerMissiles = new ArrayList<>();
    private MissileAPI missile;

    private float timer = 0f;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        if (system.getAmmo() == 0 || system.getState() == ShipSystemAPI.SystemState.COOLDOWN ){
            dangerMissiles.clear();
            return;
        }
        MissileAPI nearestEnemyMissile = AIUtils.getNearestEnemyMissile(ship);
        if (nearestEnemyMissile == null)return;
        List<MissileAPI> nearbyEnemyMissiles = AIUtils.getNearbyEnemyMissiles(ship,FM_Nocturne.RANGE);
        if (nearbyEnemyMissiles.isEmpty())return;
        if (!dangerMissiles.contains(nearestEnemyMissile) && nearestEnemyMissile.getMaxFlightTime() < nearestEnemyMissile.getFlightTime()){
            dangerMissiles.add(nearestEnemyMissile);
        }
        for (MissileAPI nearbyEnemyMissile : nearbyEnemyMissiles) {
            if (nearbyEnemyMissile.isFading() || nearbyEnemyMissile.getMaxFlightTime() < nearbyEnemyMissile.getFlightTime())continue;
            if (nearbyEnemyMissile.getDamageAmount() >= nearestEnemyMissile.getDamageAmount()){
                dangerMissiles.add(nearbyEnemyMissile);
            }
        }

        float maxDanger = 0;
        for (MissileAPI dangerMissile : dangerMissiles) {
            if (dangerMissile.isFading() || dangerMissile.getMaxFlightTime() < dangerMissile.getFlightTime())continue;
            if (maxDanger >= dangerMissile.getDamageAmount()) {

            } else {
                maxDanger = dangerMissile.getDamageAmount() * ((FM_Nocturne.RANGE - MathUtils.getDistance(dangerMissile,ship))/FM_Nocturne.RANGE);
                missile = dangerMissile;
            }
        }
        if (missile == null)return;
        Vector2f missileLoc = missile.getLocation();
        if (!CombatUtils.getMissilesWithinRange(ship.getMouseTarget(), FM_Nocturne.EFFECT_RANGE).isEmpty()){
            ship.useSystem();
        }else {
            ship.getMouseTarget().set(missileLoc);
        }

        flags.setFlag(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS,0.25f,missileLoc);




        //debug
        //Global.getCombatEngine().addHitParticle(ship.getMouseTarget(),new Vector2f(),20f,100f,1f, Color.BLUE);



    }
}
