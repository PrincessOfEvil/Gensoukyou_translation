package data.utils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MagicLensFlare;
import data.scripts.util.MagicRender;
import data.utils.guidedProject.FM_GuidedProjectSrc_Seal;
import data.utils.guidedProject.FM_GuidedProjectSrc_Star;
import data.utils.missileAI.FM_Nightbug_missile_ai;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * What I have learned from A111164 and FSF?
 * written by homejerry99
 */

public class FM_ProjectEffect extends BaseEveryFrameCombatPlugin {

    public static final Color EFFECT_1 = new Color(220, 255, 255, 255);
    public static final Color EFFECT_2 = new Color(255, 96, 96, 151);
    public static final Color EFFECT_3 = new Color(146, 248, 242, 226);
    public static final Color EFFECT_4 = new Color(169, 222, 241, 255);
    public static final Color EFFECT_5 = new Color(192, 255, 248, 221);
    public static final Color EFFECT_6 = new Color(43, 199, 168, 240);
    public static final Color EFFECT_6_X = new Color(176, 239, 255, 99);
    public static final Color EFFECT_7 = new Color(100, 170,255, 226);
    public static final Color CYAN_X = new Color(100, 225,255, 170);

    FM_LocalData.FM_Data currdata = FM_LocalData.getCurrData();


    CombatEngineAPI engine;
    public static List<DamagingProjectileAPI> ProjectsThisFrame = new ArrayList<>();

    //something important：战斗加速影响的注意
    //主要影响那些一直会起效的（对计时器和elapsed没有影响但是会影响每帧执行的内容，如每帧减速2f在战斗加速时会造成问题）
    //同理对高帧数注意
    //注意最小时间和帧数！！！！！！（会引起实际上变成每帧执行的问题！）

    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }


    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        super.advance(amount, events);


        if (this.engine == null) {
            engine = Global.getCombatEngine();

        }
        if (this.engine.isPaused()) return;

        ProjectsThisFrame = engine.getProjectiles();


        for (DamagingProjectileAPI project : ProjectsThisFrame) {
            if (project == null) continue;
            if (project.getProjectileSpecId() == null) continue;


            String proj_id = project.getProjectileSpecId();
            if (!proj_id.startsWith("FM_")) continue;


            switch (proj_id) {
                case "FM_ball" :{

                    //匀加速弹相关

                    Map<DamagingProjectileAPI, silverEffect> currEffect = currdata.silver;

                    if (!currEffect.containsKey(project)) {
                        currEffect.put(project, new silverEffect());
                    }

                    //currEffect.get(project).timer = currEffect.get(project).timer + amount;
                    //if (currEffect.get(project).timer >= 0.02f) {
                    //    currEffect.get(project).timer = 0;
                    //}

                    Vector2f vel = project.getVelocity();
                    VectorUtils.resize(vel, vel.length() + 2f * 60f * amount , vel);
                    project.getVelocity().set(vel);

                    if (project.getElapsed() >= 4) {
                        engine.removeEntity(project);
                        engine.spawnExplosion(project.getLocation(), new Vector2f(), CYAN_X, 1f, 0.4f);
                    }

                break;

                }


                case "FM_ball_dec" : {

                    //匀减速弹相关

                    Map<DamagingProjectileAPI, silverEffect> currEffect = currdata.silver;

                    if (!currEffect.containsKey(project)) {
                        currEffect.put(project, new silverEffect());
                    }

                    //currEffect.get(project).timer = currEffect.get(project).timer + amount;
                    //if (currEffect.get(project).timer >= 0.02f) {
                    //    currEffect.get(project).timer = 0;
                    //}

                    Vector2f vel = project.getVelocity();
                    VectorUtils.resize(vel, vel.length() - 1f * 60f * amount , vel);
                    project.getVelocity().set(vel);

                    if (project.getElapsed() >= 2.2f) {
                        engine.spawnProjectile(project.getSource(), null, "FM_PdMissile_builtin", project.getLocation(), project.getFacing()+30f, null);
                        engine.spawnProjectile(project.getSource(), null, "FM_PdMissile_builtin", project.getLocation(), project.getFacing()-30f, null);
                        engine.spawnExplosion(project.getLocation(), new Vector2f(), CYAN_X, 1f, 0.4f);

                        Global.getSoundPlayer().playSound("FM_Nightbugs_expand_2",1f,0.5f,project.getLocation(),new Vector2f());

                        engine.removeEntity(project);
                    }
                    break;

                }

                case "FM_IcicleFall_shot" : {

                    //FM_IcicleFall_shot
                    if (project.getWeapon() == null)continue;
                    if (project.getElapsed() > project.getWeapon().getRange()/project.getWeapon().getProjectileSpeed() * 0.9f){
                        float n = MathUtils.getRandomNumberInRange(-10f,10f);
                        for (int i = 0; i < 360f; i = i + 60) {
                            engine.spawnProjectile(project.getSource(), project.getWeapon(), "FM_ice_weapon_s", project.getLocation(), project.getFacing() + n + i, null);
                        }
                        engine.removeEntity(project);
                        Global.getSoundPlayer().playSound("FM_icebreak_se", 1, 1, project.getLocation(), new Vector2f());
                        engine.spawnExplosion(project.getLocation(), new Vector2f(), CYAN_X, 25f, 2f);
                    }

                    break;

                }

                case "FM_star_shot" : {

                    //FM_star_shot
                    if (project.getWeapon() == null)continue;

                    Map<DamagingProjectileAPI, silverEffect> currEffect = currdata.silver;

                    if (!currEffect.containsKey(project)) {
                        currEffect.put(project, new silverEffect());
                    }

                    if (project.getElapsed() * project.getWeapon().getProjectileSpeed() >= project.getWeapon().getRange() * 0.5f){
                        currEffect.get(project).timer = currEffect.get(project).timer + amount;

                        if (currEffect.get(project).timer * project.getWeapon().getProjectileSpeed() >= 100f && !project.isFading()){
                            currEffect.get(project).timer = 0;
                            engine.spawnProjectile(project.getSource(),project.getWeapon(),"FM_star_weapon_1",project.getLocation(),
                                    project.getFacing(),new Vector2f());
                            Global.getSoundPlayer().playSound("FM_Stardust_expand",1f,0.2f,project.getLocation(),project.getVelocity());

                        }
                    }
                    break;
                }

                case "FM_star_1" : {

                    //FM_star_1
                    if (project.getWeapon() == null)continue;
                    Map<DamagingProjectileAPI, silverEffect> currEffect = currdata.silver;
                    if (!currEffect.containsKey(project)) {
                        currEffect.put(project, new silverEffect());
                    }

                    ((MissileAPI) project).getSpriteAPI().setColor(Color.BLUE);

                    if (project.getElapsed() > 1f && !project.didDamage() && !project.isFading()){

                        ShipAPI enemy = AIUtils.getNearestEnemy(project);
                        DamagingProjectileAPI subProjectA = (DamagingProjectileAPI) engine.spawnProjectile(project.getSource(),project.getWeapon(),"FM_star_weapon_2",project.getLocation(),
                                project.getFacing() + 90f,new Vector2f());
                        DamagingProjectileAPI subProjectB = (DamagingProjectileAPI) engine.spawnProjectile(project.getSource(),project.getWeapon(),"FM_star_weapon_2",project.getLocation(),
                                project.getFacing() - 90f,new Vector2f());

                        ((MissileAPI) project).explode();

                        Global.getSoundPlayer().playSound("FM_Stardust_expand",2f,0.2f,project.getLocation(),project.getVelocity());
                        if (enemy != null && currEffect.get(project).beginning){
                            engine.addPlugin(new FM_GuidedProjectSrc_Star(subProjectA,enemy));
                            engine.addPlugin(new FM_GuidedProjectSrc_Star(subProjectB,enemy));
                            currEffect.get(project).beginning = false;
                            //debug
                            //engine.addFloatingText(project.getLocation(),"TEST",10f,Color.WHITE,project,0f,0f);

                        }

                    }


                    break;
                }

                case "FM_silver_shot" : {

                    //FM_silver_shot
                    Map<DamagingProjectileAPI, silverEffect> currEffect = currdata.silver;
                    if (!currEffect.containsKey(project)) {
                        currEffect.put(project, new silverEffect());
                    }
                    float totalTime = project.getWeapon().getRange()/project.getWeapon().getProjectileSpeed();
                    if (currEffect.get(project).beginning){
                        currEffect.get(project).angle = project.getWeapon().getCurrAngle();
                        currEffect.get(project).beginning = false;
                        float a = Misc.normalizeAngle(VectorUtils.getFacing(project.getVelocity()) - currEffect.get(project).angle);
                        float b = Misc.normalizeAngle(currEffect.get(project).angle - VectorUtils.getFacing(project.getVelocity()));
                        if (b > a){
                            currEffect.get(project).effect = true;
                        }
                        if (a > b){
                            currEffect.get(project).effect = false;
                        }
                    }

                    if (project.getElapsed() < 0.3f * totalTime){
                        VectorUtils.resize(project.getVelocity(),project.getWeapon().getProjectileSpeed() * (0.35f * totalTime - project.getElapsed()));
                    }else if (project.getElapsed() >= 0.3f * totalTime && project.getElapsed() <= 0.5f * totalTime){
                        VectorUtils.resize(project.getVelocity(),project.getWeapon().getProjectileSpeed() * 0.05f * totalTime);
                    }else if (project.getElapsed() > 0.5f * totalTime){

//                        float a = Misc.normalizeAngle(VectorUtils.getFacing(project.getVelocity()) - currEffect.get(project).angle);
//                        float b = Misc.normalizeAngle(currEffect.get(project).angle - VectorUtils.getFacing(project.getVelocity()));
//
//                        if (b > a){
//                            VectorUtils.rotate(project.getVelocity(),-20f * amount,project.getVelocity());
//                        }
//                        if (a > b){
//                            VectorUtils.rotate(project.getVelocity(),20f * amount,project.getVelocity());
//                        }

                        if (project.getElapsed() <= 0.75f * totalTime){
                            if (currEffect.get(project).effect){
                                VectorUtils.rotate(project.getVelocity(),-50f * amount/totalTime ,project.getVelocity());
                            }else {
                                VectorUtils.rotate(project.getVelocity(),50f * amount/totalTime ,project.getVelocity());
                            }
                        }
                        project.setFacing(VectorUtils.getFacing(project.getVelocity()));
                        VectorUtils.resize(project.getVelocity(),project.getWeapon().getProjectileSpeed() * (project.getElapsed() - 0.4f * totalTime) * 5.3f/totalTime);
                    }

                    if (project.isFading() && !project.didDamage()){
                        engine.removeEntity(project);
                        engine.spawnExplosion(project.getLocation(),(Vector2f) project.getVelocity().scale(0.01f),EFFECT_6_X,20f,1f);
                    }
                    break;
                }

                case "FM_Seal_shot" : {
                    //FM_Seal_shot
                    Map<DamagingProjectileAPI, silverEffect> currEffect = currdata.silver;
                    if (!currEffect.containsKey(project)) {
                        currEffect.put(project, new silverEffect());
                    }
                    Vector2f proj_loc = project.getLocation();
                    //Vector2f proj_vel = project.getVelocity();
                    MagicRender.singleframe(Global.getSettings().getSprite("fx", "FM_modeffect_4"), proj_loc, new Vector2f(70, 70),
                            MathUtils.getRandomNumberInRange(project.getFacing() - 10f, project.getFacing() + 10f),EFFECT_2
                            , true, CombatEngineLayers.ABOVE_SHIPS_LAYER);

                    ShipAPI enemy = AIUtils.getNearestEnemy(project);
                    if (enemy != null && currEffect.get(project).beginning){
                        engine.addPlugin(new FM_GuidedProjectSrc_Seal(project,enemy));
                        currEffect.get(project).beginning = false;
                    }
//                if (currEffect.get(project).effect){
//                    MagicRender.battlespace(Global.getSettings().getSprite("systemMap","map_star"),proj_loc,proj_vel,
//                            new Vector2f(70,70),new Vector2f(20,20),MathUtils.getRandomNumberInRange(0,360),5f,
//                            new Color(129, 248, 233, 200),true,1f,1f,0.5f);
//
//                    currEffect.get(project).effect = false;
//                }
                    break;
                }

                case "FM_Blade_ac_shell" : {
                    //FM_Blade_ac_shell
                    if (project.getWeapon() == null)continue;
                    Map<DamagingProjectileAPI, silverEffect> currEffect = currdata.silver;
                    if (!currEffect.containsKey(project)) {
                        currEffect.put(project, new silverEffect());

                    }
                    //engine.addFloatingText(project.getSource().getLocation(),String.valueOf(engine.getTimeMult().getModifiedValue()),10f,Color.WHITE,project.getSource(),1f,1f);
                    //engine.addFloatingText(project.getLocation(),String.valueOf(project.getDamageAmount()),10f,Color.WHITE,project,1f,1f);
                    //engine.addFloatingText(project.getLocation(),String.valueOf(project.getBaseDamageAmount()),10f,Color.GREEN,project,1f,1f);

                    if (project.getElapsed() > ( project.getWeapon().getRange() / project.getWeapon().getProjectileSpeed() ) * 4f/6f && !project.didDamage()) {

                        float angle;
                        angle = project.getFacing();
                        for (int i = -15 ; i < 15 ;i = i+1){

                            Vector2f loc_x = Vector2f.add(project.getLocation(),(Vector2f) Misc.getUnitVectorAtDegreeAngle(angle).scale(i * 3.4f),new Vector2f());

                            DamagingProjectileAPI subproject = (DamagingProjectileAPI) engine.spawnProjectile(project.getSource(), project.getWeapon(), "FM_Blade_weapon",
                                    loc_x, angle,
                                    new Vector2f()
                            );
                            subproject.setDamageAmount(project.getDamageAmount()/30f);

                            engine.addNebulaParticle(
                                    MathUtils.getRandomPointInCircle(loc_x,20f),
                                    MathUtils.getRandomPointInCone((Vector2f) project.getVelocity().scale(0.9f),20f,project.getFacing()-15f,project.getFacing()+15f),
                                    MathUtils.getRandomNumberInRange(27f,32f),
                                    1f,
                                    -2f,
                                    10f,
                                    2f,
                                    EFFECT_7
                            );
                        }
                        engine.addNegativeNebulaParticle(
                                MathUtils.getRandomPointInCircle(project.getLocation(),30f),
                                MathUtils.getRandomPointInCone((Vector2f) project.getVelocity().scale(1f),10f,project.getFacing()-15f,project.getFacing()+15f),
                                30f,
                                0.5f,
                                -1f,
                                2f,
                                1f,
                                EFFECT_1
                        );
                        engine.removeEntity(project);
                        Global.getSoundPlayer().playSound("FM_Nightbugs_expand_2",2f,0.4f,project.getLocation(),new Vector2f());
                        MagicLensFlare.createSharpFlare(engine, project.getSource(), project.getLocation(), 4, 220, angle + 90f,
                                EFFECT_3,EFFECT_4
                        );
                    }
                    break;
                }
                case "FM_Nightbugs_proj" : {

                    //FM_Nightbugs_proj
                    if (project.getWeapon() == null)continue;

                    Map<DamagingProjectileAPI, silverEffect> currEffect = currdata.silver;

                    if (!currEffect.containsKey(project)) {
                        currEffect.put(project, new silverEffect());
                    }
                    float range = project.getWeapon().getRange();
                    float speed = project.getWeapon().getProjectileSpeed();

                    if (project.getElapsed() >= 0.5f * range / speed) {

                        //debug
                        //engine.addFloatingText(project.getLocation(),"TEST",10f,Color.WHITE,project,0f,0f);

//                        Vector2f vel = project.getVelocity();
//                        //something important：战斗加速影响的注意
//                        VectorUtils.resize(vel, vel.length() - 360f * engine.getElapsedInLastFrame(), vel);
//                        project.getVelocity().set(vel);

                        if (project instanceof MissileAPI){
                            //减速！
                            ((MissileAPI) project).setMissileAI(new FM_Nightbug_missile_ai((MissileAPI) project));
                        }
                        //弹幕生成之类的东西（）
                        if (project.getVelocity().length() < 60) {
                            currEffect.get(project).timer = currEffect.get(project).timer + amount;

                            if (currEffect.get(project).beginning) {
                                currEffect.get(project).beginning = false;
                                engine.spawnExplosion(project.getLocation(), new Vector2f(), CYAN_X, 60f, 3f);
                            }

                            if (currEffect.get(project).timer >= 0.2f) {
                                currEffect.get(project).effect = !currEffect.get(project).effect;
                                currEffect.get(project).timer = 0;
                                currEffect.get(project).angle = currEffect.get(project).angle + 5f;
                            }

                            if (currEffect.get(project).effect) {
                                MagicLensFlare.createSharpFlare(engine, project.getSource(), project.getLocation(), 6f, 270f, 0f,
                                        EFFECT_5,EFFECT_6
                                );
                                for (float i = 0; i < 360; i = i + 60f) {
                                    Vector2f point = MathUtils.getPoint(project.getLocation(), project.getCollisionRadius(), i + currEffect.get(project).angle);
                                    engine.spawnProjectile(project.getSource(), project.getWeapon(), "FM_decball", point, i + currEffect.get(project).angle, null);
                                    Global.getSoundPlayer().playSound("FM_Nightbugs_expand_1",1f,0.7f,project.getLocation(),new Vector2f());
                                }
                                currEffect.get(project).times = currEffect.get(project).times + 1;
                                currEffect.get(project).effect = !currEffect.get(project).effect;
                            }
                        }
                    } else {
                        currEffect.get(project).effect = false;
                    }
                    if (currEffect.get(project).times > 5) {
                        engine.removeEntity(project);
                    }
                    break;

                }

//                case "FM_ClearanceMunitions_project" : {
//
//                    //FM_ClearanceMunitions_project
//                    if (project.getWeapon() == null){
//                        continue;
//                    }
//                    Map<DamagingProjectileAPI, silverEffect> currEffect = currdata.silver;
//
//                    currEffect.get(project).timer = currEffect.get(project).timer + engine.getElapsedInLastFrame();
//
//                    if (currEffect.get(project).timer >= 0.5f){
//
//                    }
//                    break;
//                }

                case "FM_Hailstorm_shot" : {

                    //FM_Hailstorm_shot
                    if (project.getWeapon() == null){
                        continue;
                    }

                    WeaponAPI weapon = project.getWeapon();

                    if (project.getElapsed() > weapon.getRange() / project.getVelocity().length() * 3f/7f) {

                        for (int i = 0; i < 10; i = i + 1) {
                            engine.spawnProjectile(weapon.getShip(), weapon, "FM_ice_weapon_m", project.getLocation(), MathUtils.getRandomNumberInRange(project.getFacing() - 10, project.getFacing() + 10), null);
                        }
                        engine.spawnExplosion(project.getLocation(), new Vector2f(), CYAN_X, 20f, 0.5f);
                        Global.getSoundPlayer().playSound("FM_icebreak_se", 1, 1, project.getLocation(), new Vector2f());
                        engine.removeEntity(project);

                    }
                    break;

                }


//                case "FM_MoonedInsect_shot" : {
//                    //FM_MoonedInsect_shot
//                    if (project.getWeapon() == null)continue;
//                    if (project.getSource() == null)continue;
//                    if (project.getElapsed() > project.getWeapon().getRange()/project.getWeapon().getProjectileSpeed() * 0.8f){
//
//
//                        engine.spawnProjectile(
//                                project.getSource(),
//                                null,
//                                "flarelauncher1",
//                                project.getLocation(),
//                                project.getFacing(),
//                                new Vector2f()
//                        );
//                        engine.spawnExplosion(project.getLocation(),(Vector2f) project.getVelocity().scale(0.1f),EFFECT_6_X,50f,1.5f);
//                        RippleDistortion ripple = new RippleDistortion(project.getLocation(),new Vector2f());
//                        ripple.setArc(0,360);
//                        DistortionShader.addDistortion(ripple);
//                        ripple.fadeInIntensity(0.5f);
//                       ripple.fadeOutSize(1f);
//
//                        engine.removeEntity(project);
//
//                    }
//                    break;
//                }
//                case "FM_PdMissile_project" : {
//
//                    //FM_PdMissile_project
//
//                    float speed = project.getVelocity().length();
//                    float time = project.getElapsed();
//
//                    if (!engine.isPaused()){
//                        engine.addFloatingText(project.getLocation(),""+speed,10f,Color.WHITE,project,10f,10f);
//                        //engine.addFloatingText(project.getLocation(),""+time,10f,Color.WHITE,project,10f,10f);
//                        Global.getLogger(this.getClass()).info(time);
//                    }
//                    break;
//                }
                default : {
                    break;
                }



            }


/*
            //正弦轨迹弹头A
            if (proj_id.equals("FM_Sine_shell_A")) {

                Map<DamagingProjectileAPI, silverEffect> currEffect = currdata.silver;

                if (!currEffect.containsKey(project)) {
                    currEffect.put(project, new silverEffect());
                }

                if (currEffect.get(project).beginning) {
                    //x轴方向速度(不变)
                    currEffect.get(project).dir = new Vector2f(project.getVelocity());
                    currEffect.get(project).beginning = false;

                }

                //engine.addFloatingText(project.getLocation(),String.valueOf(currEffect.get(project).dir),20f, Color.WHITE,project,1f,1f);

                sineEffect(project,project.getElapsed(),currEffect.get(project).dir,-0.5f,1f);



            }

            //正弦轨迹弹头B
            if (proj_id.equals("FM_Sine_shell_B")) {

                Map<DamagingProjectileAPI, silverEffect> currEffect = currdata.silver;

                if (!currEffect.containsKey(project)) {
                    currEffect.put(project, new silverEffect());
                }

                if (currEffect.get(project).beginning) {
                    //x轴方向速度(不变)
                    currEffect.get(project).dir = new Vector2f(project.getVelocity());
                    currEffect.get(project).beginning = false;

                }

                sineEffect(project,project.getElapsed(),currEffect.get(project).dir,0.5f,1f);



            }
*/


        }

    }

    //弹头通用参数
    public final static class silverEffect {
        //计时用
        float timer;
        //额外计时器
        //float timer_2;
        //角度用
        float angle;
        //效果开关
        boolean effect;
        //初始状态？
        boolean beginning;
        //方向相关向量
        Vector2f dir;
        //作用次数相关
        int times;



        private silverEffect() {
            timer = 0f;
            //timer_2 = 0f;
            angle = 0f;
            effect = true;
            beginning = true;
            dir = new Vector2f();
            times = 0;

        }
    }




}
