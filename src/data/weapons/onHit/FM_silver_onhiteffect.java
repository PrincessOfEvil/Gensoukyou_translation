package data.weapons.onHit;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.utils.FM_ProjectEffect;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class FM_silver_onhiteffect implements OnHitEffectPlugin {

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        for (int i = 0; i < 7; i++) {
            engine.addNebulaParticle(
                    point,
                    MathUtils.getRandomPointInCircle(new Vector2f(),50f),
                    16,
                    0.8f,
                    -0.5f,
                    0.8f,
                    1f,
                    FM_ProjectEffect.EFFECT_5,
                    true
            );
        }
        engine.spawnExplosion(projectile.getLocation(), new Vector2f(), FM_ProjectEffect.EFFECT_1, 30f, 0.3f);


    }
}
