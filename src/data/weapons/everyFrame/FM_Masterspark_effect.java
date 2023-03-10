package data.weapons.everyFrame;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.util.MagicLensFlare;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_Masterspark_effect implements EveryFrameWeaponEffectPlugin {
    private float NO_CHARGE = 0f;
    private boolean FULL_CHARGE = false;
    private static final Color PARTICLES_COLOR = new Color(25, 207, 239, 208);
    private static final Color FLARE_COLOR = new Color(169, 239, 239, 181);
    private static final Color EXPLOSION_COLOR = new Color(202, 233, 255, 255);

    private float TIMER = 0f;
    private static final float TURN_RATE_DEBUFF = 0.5f;

    private WaveDistortion wave = null;


    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        float chargelevel = weapon.getChargeLevel();
        ShipAPI ship = weapon.getShip();
        Vector2f shipvel = ship.getVelocity();

        if (chargelevel > NO_CHARGE && !FULL_CHARGE) {

            int PARTICLES = (int) (30f * (1f + 2f * chargelevel));
            Vector2f particles_vel;
            particles_vel = MathUtils.getRandomPointOnCircumference(shipvel, 100f * (1 + 0.75f * chargelevel));
            Vector2f particles_loc;
            particles_loc = ship.getLocation();

            ship.getMutableStats().getMaxTurnRate().modifyMult(ship.getFleetMemberId() + "_MastersparkBeam",TURN_RATE_DEBUFF);

            TIMER = TIMER + amount;
            if (TIMER >= 0.2f) {
                MagicLensFlare.createSharpFlare(engine, ship, weapon.getLocation(), 3f, 400f, 0f, FLARE_COLOR, EXPLOSION_COLOR);
                TIMER = 0f;
            }

            for (int x = 0; x < PARTICLES; x++) {
                engine.addSmoothParticle(particles_loc, particles_vel, MathUtils.getRandomNumberInRange(2, 4), 0.75f,-1f,1f, PARTICLES_COLOR);
            }

            if (chargelevel >= 1f) {
                FULL_CHARGE = true;
                for (int i = 0 ; i < 5; i++){
                    engine.addNebulaParticle(
                            particles_loc,
                            MathUtils.getPoint(new Vector2f(),70f,72f * i),
                            50f,
                            1.5f,
                            -1f,
                            1.2f,
                            2f,
                            PARTICLES_COLOR,
                            true
                            );
                }

                ship.setAngularVelocity(ship.getAngularVelocity() * (0));


                if (wave == null){
                    wave = new WaveDistortion();
                    wave.setLocation(weapon.getLocation());
                    wave.setArc(0,360);
                    wave.setSize(70f);
                    wave.setIntensity(50f);
                }

                if (wave != null){
                    DistortionShader.addDistortion(wave);
                    wave.fadeInIntensity(0.6f);
                    wave.fadeOutSize(0.6f);
                    wave.flip(true);


                }

                engine.spawnExplosion(weapon.getLocation(), shipvel, EXPLOSION_COLOR, 60f, 3f);
                MagicLensFlare.createSharpFlare(engine, ship, weapon.getLocation(), 8f, 700f, 0f, FLARE_COLOR, EXPLOSION_COLOR);


            }


        }

        if (chargelevel <= 0f) {
            FULL_CHARGE = false;
            ship.getMutableStats().getMaxTurnRate().unmodifyMult(ship.getFleetMemberId() + "_MastersparkBeam");
            if (wave != null){
                DistortionShader.removeDistortion(wave);
                wave = null;
            }


        }


    }
}
