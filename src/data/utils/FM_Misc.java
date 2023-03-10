package data.utils;

import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Scripted by AnyIDElse and homejerry99,just for convenient.
 */
public class FM_Misc {

    //方便的get舰载机的方法

    public static List<ShipAPI> getFighters(ShipAPI carrier) {
        return getFighters(carrier, true);
    }

    public static List<ShipAPI> getFighters(ShipAPI carrier, boolean includeReturn) {
        Set<ShipAPI> result = new HashSet<>();
        for (FighterWingAPI wing : carrier.getAllWings()){
            result.addAll(wing.getWingMembers());

            if (includeReturn) {
                for (FighterWingAPI.ReturningFighter ret : wing.getReturning()) {
                    result.add(ret.fighter);
                }
            }
        }


        return new ArrayList<>(result);
    }
    //一种贝塞尔曲线，t在(0,1)区间
    public static Vector2f BezierCurvePoint (float t, Vector2f begin, Vector2f end, Vector2f medium){

        Vector2f point;

        Vector2f p0 = (Vector2f) new Vector2f(0,0).scale(1-t*t);
        Vector2f p1 = new Vector2f((ReadableVector2f) Vector2f.sub(medium,begin,new Vector2f()).scale(2*t*(1-t)));
        Vector2f p2 = new Vector2f((ReadableVector2f) Vector2f.sub(end,begin,new Vector2f()).scale(t*t));


        point = new Vector2f(p0.x + p1.x + p2.x,p0.y + p1.y + p2.y);

        return Vector2f.add(point,begin,point);

    }

    //正弦轨迹
    //amount为弹头自身时间(Elapsed即可)，dir为初速(重点)，K为振幅相关(可为负数)，L为波长相关
    public static void sineEffect(DamagingProjectileAPI project, float amount, Vector2f dir, float K, float L){

        if (project.getWeapon() == null)return;

        float T = project.getWeapon().getRange()/dir.length();
        float a = (float) (2 * Math.PI/T);

        float y = (float) (K * FastTrig.cos(amount * a * L));


        Vector2f dir_y = new Vector2f();
        VectorUtils.rotate(dir,90f,dir_y);

        Vector2f.add((Vector2f) dir_y.scale(y), dir, project.getVelocity());

        project.setFacing(VectorUtils.getFacing(project.getVelocity()));

    }

}
