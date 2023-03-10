package data.utils.missileAI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import org.lazywizard.lazylib.combat.AIUtils;

import java.awt.*;

public class FM_SkySerpent_missile_ai implements MissileAIPlugin{

    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private final ShipAPI ship;
    private static final float SECOND_STAGE_RANGE = 800f;

    private static final Color EFFECT = new Color(44, 217, 203, 228);



    public FM_SkySerpent_missile_ai(MissileAPI missile, ShipAPI ship){
        this.missile = missile;
        this.ship = ship;
    }

    public void advance(float amount){

        engine = Global.getCombatEngine();
        if (engine == null)return;
        if (missile.getWeapon() == null)return;

        ShipAPI target = findTarget(ship,missile);

        if (target == null){
            return;
        }





    }

    private ShipAPI findTarget(ShipAPI ship, MissileAPI missile){
        ShipAPI target = ship.getShipTarget();
        if (target == null){
            target = AIUtils.getNearestEnemy(missile);
        }

        return target;
    }
}
