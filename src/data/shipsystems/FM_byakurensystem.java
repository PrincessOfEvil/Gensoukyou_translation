package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import data.scripts.util.MagicRender;
import data.utils.I18nUtil;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_byakurensystem extends BaseShipSystemScript {

    public static final Color EFFECT = new Color(255, 255, 255, 193);
    public static final Color JITTER = new Color(92, 217, 255, 255);
    public static final float SPEED_BONUS = 180f;

    private boolean SYSTEM = true;


    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = (ShipAPI) stats.getEntity();

        if (ship != null) {
            Vector2f effect_size = new Vector2f();

            effect_size.set(ship.getShieldRadiusEvenIfNoShield() * 2f, ship.getShieldRadiusEvenIfNoShield() * 2f);

            Vector2f effect_vel;
            Vector2f effect_growth = new Vector2f();

            effect_vel = new Vector2f();

            ship.setJitter(ship, JITTER, 0.4f, 1, 1f);
            ship.setJitterUnder(ship, JITTER, 0.8f, 2, 3f);


            if (state != State.OUT && SYSTEM) {

                if (MagicRender.screenCheck(0.5f, ship.getRenderOffset())) {
                    for (int i = 0; i < 12; i++) {
                        MagicRender.objectspace(Global.getSettings().getSprite("fx", "FM_modeffect_1"),
                                ship,
                                ship.getRenderOffset(),
                                effect_vel,
                                effect_size,
                                effect_growth,
                                ship.getFacing() + 1.7f * i,
                                90f,
                                false,
                                EFFECT,
                                true,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0.25f,
                                2f,
                                1f,
                                true,
                                CombatEngineLayers.BELOW_SHIPS_LAYER);
                    }
                }

                SYSTEM = false;
            }
            if (state == ShipSystemStatsScript.State.OUT) {
                stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
            } else {
                stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS * effectLevel);
                stats.getAcceleration().modifyFlat(id, SPEED_BONUS * effectLevel);

            }
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        SYSTEM = true;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {

        if (index == 0) {
            return new StatusData(I18nUtil.getShipSystemString("FM_ByakurenSystemInfo"), false);
        }

        return null;
    }
}
