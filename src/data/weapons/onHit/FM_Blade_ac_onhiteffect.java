package data.weapons.onHit;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_Blade_ac_onhiteffect implements OnHitEffectPlugin {

    private static final int NUM_PARTICLES = 10;

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        float speed = projectile.getMoveSpeed();
        float facing = projectile.getFacing();

        if (shieldHit) {
            for (int x = 0; x < NUM_PARTICLES; x++) {
                engine.addSmoothParticle(point, MathUtils.getRandomPointOnCircumference(null, speed * 0.02f), MathUtils.getRandomNumberInRange(10, 20), 200f, 2, Color.CYAN);
            }
        }

    }
}
