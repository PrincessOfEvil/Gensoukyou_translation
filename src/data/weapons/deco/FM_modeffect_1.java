package data.weapons.deco;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import data.scripts.util.MagicRender;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_modeffect_1 implements EveryFrameWeaponEffectPlugin {
    private float TIMER = 0f;
    boolean ON = false;

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();

        if (engine.isPaused()) return;
        TIMER += amount;
        if (TIMER > 0.6f) {
            ON = !ON;
            TIMER = 0f;
        }



        Vector2f effect_size = new Vector2f();
        effect_size.set(ship.getShieldRadiusEvenIfNoShield() * 2f, ship.getShieldRadiusEvenIfNoShield() * 2f);

        if (ON) {
            MagicRender.objectspace(Global.getSettings().getSprite("fx", "FM_modeffect_1"), ship, ship.getRenderOffset(), ship.getVelocity(), effect_size, effect_size, 0, 120f, true, Color.WHITE, false, 0f, 0.1f, 0f, true, CombatEngineLayers.BELOW_SHIPS_LAYER);
        }
    }
}
