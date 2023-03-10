package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_ProjectEffect;
import data.utils.I18nUtil;
import data.utils.visual.FM_DiamondParticle3DTest;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;


public class FM_windgoddrive extends BaseShipSystemScript {


    public static final float SHIP_ALPHA_MULT = 0.25f;

    private float particleTimer = 0.1f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship;
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
        } else {
            stats.getMaxSpeed().modifyFlat(id, 600f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 600f * effectLevel);
            //stats.getAcceleration().modifyPercent(id, 200f * effectLevel);
        }

        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();

        } else {
            return;
        }

        //Global.getCombatEngine().addFloatingText(ship.getLocation(),state.toString(),10f, Color.WHITE,ship,0f,0f);
        if (state == State.COOLDOWN || state == State.IDLE) {
            unapply(stats, id);
            return;
        }

        //float f = VULNERABLE_FRACTION;

        float levelForAlpha = effectLevel;

        if (state == State.IN || state == State.ACTIVE) {
            ship.setPhased(true);
            levelForAlpha = effectLevel;
        } else if (state == State.OUT) {
            if (effectLevel > 0.5f) {
                ship.setPhased(true);
            } else {
                ship.setPhased(false);
            }
            levelForAlpha = effectLevel;

        }

        ship.setExtraAlphaMult(1f - (1f - SHIP_ALPHA_MULT) * levelForAlpha);
        ship.setApplyExtraAlphaToEngines(true);



//		float mitigationLevel = jitterLevel;
//		if (mitigationLevel > 0) {
//			stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * mitigationLevel);
//			stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * mitigationLevel);
//			stats.getEmpDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * mitigationLevel);
//		} else {
//			stats.getHullDamageTakenMult().unmodify(id);
//			stats.getArmorDamageTakenMult().unmodify(id);
//			stats.getEmpDamageTakenMult().unmodify(id);
//		}

        particleTimer = particleTimer - Global.getCombatEngine().getElapsedInLastFrame();


        if (particleTimer <= 0){

            for (int i = 0 ; i < 7 ; i = i + 1){
                Vector2f particleloc = MathUtils.getRandomPointInCone(ship.getLocation(),100f,ship.getFacing() - 160f, ship.getFacing() - 220f);
                Vector2f particlevel = new Vector2f(-ship.getVelocity().x * 0.25f,-ship.getVelocity().y * 0.25f);
                FM_DiamondParticle3DTest.FM_DP3DParams DP = new FM_DiamondParticle3DTest.FM_DP3DParams();
                DP.fadeIn = 0.25f;
                DP.fadeOut = 0.65f;
                DP.radius = MathUtils.getRandomNumberInRange(6f,10f);
//            SP.radius = 40f;
                DP.spin = MathUtils.getRandomNumberInRange(240,480);
                DP.spinZ = MathUtils.getRandomNumberInRange(90,180);
                DP.color = Misc.scaleAlpha(FM_ProjectEffect.EFFECT_3,1f);
                DP.thickness = 7f;
                DP.vel = particlevel;
                DP.loc = particleloc;
                CombatEntityAPI visualEntity = Global.getCombatEngine().addLayeredRenderingPlugin(new FM_DiamondParticle3DTest(DP));
            }
            particleTimer = 0.1f;
        }
    }


    public void unapply(MutableShipStatsAPI stats, String id) {

        ShipAPI ship;

        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();

        } else {
            return;
        }

        //Global.getCombatEngine().addFloatingText(ship.getLocation(),ship.getSystem().getState().toString(),10f, Color.WHITE,ship,0f,0f);

        ship.setPhased(false);
        ship.setExtraAlphaMult(1f);
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);

        particleTimer = 0.2f;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(I18nUtil.getShipSystemString("FM_WindGodDriveInfo"), false);
        }
        return null;
    }


}
