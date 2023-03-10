package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import data.utils.I18nUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_reactorexcursion extends BaseShipSystemScript {

    public static final float ENERGY_WEAPON_DAMAGE_BONUS = 0.5f;
    public static final float WEAPON_DEBUFF = 0.2f;
    public static final float MAX_RANGE = 600f;

    public static final Color EMP_CORE = new Color(23, 181, 220, 215);
    public static final Color EMP_FRINE = new Color(132, 243, 232, 126);

    private float TIMER = 0f;
    private boolean DEPEND = false;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        if (stats.getEntity() == null || !(stats.getEntity() instanceof ShipAPI) || Global.getCombatEngine() == null){
            return;
        }

        stats.getEnergyWeaponDamageMult().modifyFlat(id,effectLevel * ENERGY_WEAPON_DAMAGE_BONUS);

        CombatEngineAPI engine = Global.getCombatEngine();
        ShipAPI ship = (ShipAPI) stats.getEntity();

        if (!DEPEND){
            if (Math.random() <= WEAPON_DEBUFF){
                for (WeaponAPI weapon : ship.getAllWeapons()){
                    weapon.disable(false);
                    stats.getCombatWeaponRepairTimeMult().modifyMult(id,0.1f);
                }
            }

            DEPEND = true;

            //engine.addFloatingText(ship.getLocation(),"DISABLE",20f,Color.WHITE,ship,0f,0f);
        }

        for (WeaponAPI weapon : ship.getAllWeapons()){
            if (DEPEND && !weapon.isDisabled()){
                stats.getCombatWeaponRepairTimeMult().unmodifyMult(id);
            }
        }


        TIMER = TIMER + engine.getElapsedInLastFrame();

        if (TIMER >= 0.4f){
            for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()){
                if (!slot.isSystemSlot())continue;

                Vector2f effect_loc = slot.computePosition(ship);
                float effect_facing = slot.computeMidArcAngle(ship);

                for (int i = 0 ; i < 3 ; i = i + 1){
                    engine.addNebulaParticle(
                            effect_loc,
                            MathUtils.getRandomPointInCone(new Vector2f(),20f,effect_facing - 5f, effect_facing + 5f),
                            40f,
                            1.5f,
                            -1f,
                            0.4f,
                            0.5f,
                            EMP_CORE,
                            true
                    );
                }

                engine.spawnEmpArcVisual(
                        effect_loc,
                        ship,
                        MathUtils.getRandomPointInCircle(effect_loc,MAX_RANGE * 0.2f),
                        ship,
                        10f,
                        EMP_FRINE,
                        EMP_CORE
                );

            }

            TIMER = 0f;
        }

    }

    public void unapply(MutableShipStatsAPI stats, String id) {

        DEPEND = false;

        stats.getEnergyWeaponDamageMult().unmodify(id);
        stats.getCombatWeaponRepairTimeMult().unmodifyMult(id);


    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (effectLevel > 0) {
            if (index == 0) {
                return new StatusData(I18nUtil.getShipSystemString("FM_ReactoreExcursionInfo0") + (int) (ENERGY_WEAPON_DAMAGE_BONUS * effectLevel * 100f) + "%", false);
            }else if (index == 1){
                return new StatusData(I18nUtil.getShipSystemString("FM_ReactoreExcursionInfo1") + (int) (WEAPON_DEBUFF * 100f) + "%",true);
            }
        }

        return null;
    }
}
