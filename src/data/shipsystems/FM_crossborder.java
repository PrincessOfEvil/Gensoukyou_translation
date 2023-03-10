package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.util.MagicLensFlare;
import data.utils.I18nUtil;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_crossborder extends BaseShipSystemScript {
    //public static final Color EFFECT = new Color(255, 106, 106, 218);
    public static final Color FLARE_1 = new Color(255, 60, 60, 176);
    public static final Color FLARE_2 = new Color(255, 146, 146, 255);

    //public static final float CANCEL_RANGE = 500f;

    private WaveDistortion wave = null;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI the_ship = (ShipAPI) stats.getEntity();
        Vector2f ship_loc = the_ship.getLocation();
        //int owner = the_ship.getOwner();

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null)return;
/*
        List<CombatEntityAPI> projects = CombatUtils.getEntitiesWithinRange(ship_loc, CANCEL_RANGE);
        for (CombatEntityAPI project : projects) {
            if (project.getOwner() != owner && project instanceof DamagingProjectileAPI) {
                engine.removeEntity(project);
                engine.spawnExplosion(project.getLocation(), new Vector2f(), EFFECT, project.getCollisionRadius(), 1f);

            }
        }

 */

        MagicLensFlare.createSharpFlare(engine, the_ship, ship_loc, 9f, the_ship.getCollisionRadius() * 2, the_ship.getFacing() + 90f, FLARE_1, FLARE_2);
        the_ship.setExtraAlphaMult(1f - effectLevel);

        if (state == State.IN) {

            if (wave == null){

                wave = new WaveDistortion(ship_loc, new Vector2f());
                wave.setSize(the_ship.getCollisionRadius());
                wave.setIntensity(10f);
                wave.setArc(0, 360);
                wave.flip(true);

                DistortionShader.addDistortion(wave);

            }else {
                float intensity = (float) (Math.sqrt(effectLevel) * 60f);
                wave.setLocation(ship_loc);
                wave.setSize(the_ship.getCollisionRadius() - effectLevel * 40f);
                wave.setIntensity(intensity + 10);
            }

        }

        if (state == State.OUT && wave != null) {

            wave.fadeOutSize(0.3f);
            wave = null;
        }


    }

    public void unapply(MutableShipStatsAPI stats, String id) {


    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(I18nUtil.getShipSystemString("FM_CrossBorderInfo"), false);
        }
        return null;
    }
}
