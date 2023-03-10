package data.weapons.everyFrame;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_PersuasionEveryFrame implements EveryFrameWeaponEffectPlugin {

    public int HIT_NUM = 0;

    private boolean FACING_CHANGE = true;

    public static final Color R = new Color(255, 53, 53,255);
    public static final Color G = new Color(37, 255, 37,255);
    public static final Color B = new Color(60, 60, 255,255);

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (weapon == null)return;
        if (weapon.getShip() == null)return;

        if (HIT_NUM >= 3){
            if (FACING_CHANGE){
                Color c = randomColor();
                spawnMissile(engine,weapon,weapon.getCurrAngle() + 90f,c);
                engine.addNebulaParticle(weapon.getLocation(),MathUtils.getPoint(new Vector2f(),40f,weapon.getCurrAngle() + 90f),30f,0.6f,
                        -0.4f,0.4f,1f,Misc.interpolateColor(Color.WHITE,c,0.5f),true);
            }else {
                Color c = randomColor();
                spawnMissile(engine,weapon,weapon.getCurrAngle() - 90f,c);
                engine.addNebulaParticle(weapon.getLocation(),MathUtils.getPoint(new Vector2f(),40f,weapon.getCurrAngle() - 90f),30f,0.6f,
                        -0.4f,0.4f,1f, Misc.interpolateColor(Color.WHITE,c,0.5f),true);
            }
            Global.getSoundPlayer().playSound("hit_glancing_energy",1.1f,0.4f,weapon.getLocation(),new Vector2f());
            FACING_CHANGE = !FACING_CHANGE;
            HIT_NUM = 0;
        }

    }

    private void spawnMissile(CombatEngineAPI engine, WeaponAPI weapon, float facing, Color color){

        MissileAPI missile = (MissileAPI) engine.spawnProjectile(weapon.getShip(),weapon,"FM_Persuasion_extra",weapon.getLocation(),facing,new Vector2f());
        missile.getSpriteAPI().setColor(color);

    }
    private Color randomColor(){
        Color color = R;
        float prob = MathUtils.getRandomNumberInRange(0f,1f);
        if (prob < 0.33f){
            color = B;
        }
        if (prob > 0.66f){
            color = G;
        }

        return color;
    }
}
