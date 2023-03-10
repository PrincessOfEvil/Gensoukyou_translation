package data.weapons.beam;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_SparkBeamEffect implements BeamEffectPlugin {

    private static final Color PARTICLES_COLOR = new Color(143, 227, 229, 255);

    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        CombatEntityAPI target = beam.getDamageTarget();
        Vector2f hit_loc = beam.getTo();
        float R = beam.getWidth() * 6f;
        float PARTICLE_NUMBER = beam.getWidth() * 0.5f;


        if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {

            for (int i = 0; i < PARTICLE_NUMBER; i++) {
                engine.addHitParticle(hit_loc, MathUtils.getRandomPointOnCircumference(null, R), MathUtils.getRandomNumberInRange(2, 5), 30f, 1f, PARTICLES_COLOR);
            }

        }
    }
}
