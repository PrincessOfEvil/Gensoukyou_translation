package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.plugins.MagicTrailPlugin;
import data.scripts.util.MagicRender;
import data.utils.FM_ProjectEffect;
import data.utils.I18nUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FantasyKawausoMod extends BaseHullMod {


    public static final Color EFFECT_1 = new Color(31, 236, 157, 226);
    public static final Color EFFECT_2 = new Color(202, 246, 233, 226);
    public static final Color EFFECT_3 = new Color(102, 208, 250, 255);

    public static final Color EMP = new Color(22, 239, 239, 205);
    public static final Color EMP_CORE = new Color(31, 236, 157, 119);

    public static final float PD_MULT= 0.25f;
    public static final float FLUX_MULT = 0.85f;

    private static Map<ShipAPI.HullSize,Float> mag = new HashMap();

    static {
        mag.put(ShipAPI.HullSize.FIGHTER, 0f);
        mag.put(ShipAPI.HullSize.FRIGATE, 50f);
        mag.put(ShipAPI.HullSize.DESTROYER, 75f);
        mag.put(ShipAPI.HullSize.CRUISER, 100f);
        mag.put(ShipAPI.HullSize.CAPITAL_SHIP, 125f);
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize,
                                               MutableShipStatsAPI stats, String id) {

        stats.getBeamPDWeaponRangeBonus().modifyMult(id,PD_MULT);
        stats.getNonBeamPDWeaponRangeBonus().modifyMult(id,PD_MULT);
    }

    public void advanceInCombat(ShipAPI ship, float amount) {

        if (ship == null) {
            return;
        }

        CombatEngineAPI engine = Global.getCombatEngine();

        if (engine == null)return;

        if (!engine.getCustomData().containsKey("FantasyKawausoMod")) {
            engine.getCustomData().put("FantasyKawausoMod", new HashMap<>());
        }

        Map<ShipAPI, ModState> currState = (Map) engine.getCustomData().get("FantasyKawausoMod");

        if (!currState.containsKey(ship)) {
            currState.put(ship, new ModState());


        }

        if (!ship.getTravelDrive().isActive()){
            currState.get(ship).timer = currState.get(ship).timer + amount;

            if (currState.get(ship).timer >= 1f){
                currState.get(ship).timer = 1f;
            }

            currState.get(ship).isActive = true;

        }

        if (currState.get(ship).isActive && ship.isAlive()){

            List<DamagingProjectileAPI> projects = FM_ProjectEffect.ProjectsThisFrame;

            if (currState.get(ship).sprites.isEmpty()){
                for (int i = 0; i < 3; i = i + 1){
                    SpriteAPI sprite = Global.getSettings().getSprite("fx","FM_modeffect_5");

                    currState.get(ship).sprites.add(i,sprite);

                    currState.get(ship).sprite_angle_0.put(sprite,120f * i);

                    currState.get(ship).trail_id.put(sprite,MagicTrailPlugin.getUniqueID());


                }
            }

            currState.get(ship).angle = currState.get(ship).angle + amount * Math.max(1f - ship.getFluxTracker().getFluxLevel(),0.1f);

            if(currState.get(ship).angle == 360f){
                currState.get(ship).angle = 0f;
            }

            for (SpriteAPI sprite : currState.get(ship).sprites){
                float angel_s = currState.get(ship).angle * 50f + currState.get(ship).sprite_angle_0.get(sprite);
                sprite.setAlphaMult(currState.get(ship).timer);

                Vector2f loc = MathUtils.getPoint(ship.getLocation(),ship.getCollisionRadius() * 2f,angel_s);

                if (currState.get(ship).timer >= 1){

                    MagicTrailPlugin.AddTrailMemberAdvanced(
                            ship,
                            currState.get(ship).trail_id.get(sprite),
                            Global.getSettings().getSprite("fx","FM_trail_2"),
                            loc,
                            0,
                            0,
                            angel_s - 90,
                            0f,
                            0f,
                            60f,
                            180f,
                            EFFECT_1,
                            EFFECT_2,
                            1f,
                            0.2f,
                            0.3f,
                            1f,
                            GL11.GL_BLEND_SRC,
                            GL11.GL_ONE_MINUS_CONSTANT_ALPHA,
                            256f,
                            10,
                            10f,
                            null,
                            null,
                            CombatEngineLayers.BELOW_SHIPS_LAYER,
                            60f
                    );
                }
                //engine.addSmoothParticle(loc,new Vector2f(),10f,10f,-1f, 2f,EFFECT_1);

                MagicRender.singleframe(sprite,loc,new Vector2f(28,28),angel_s,
                        EFFECT_2,true, CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);

                if (!ship.getFluxTracker().isOverloadedOrVenting()){

                    for (DamagingProjectileAPI project : projects){
                        if (MathUtils.getDistance(project.getLocation(),loc) < mag.get(ship.getHullSize())){

                            if (project instanceof MissileAPI && project.getOwner() != ship.getOwner()){
                                ship.getFluxTracker().increaseFlux(project.getDamageAmount() * FLUX_MULT,false);
                                engine.removeEntity(project);

                                engine.spawnEmpArcVisual(loc,ship,project.getLocation(),project,6f,EMP,EMP_CORE);
                                engine.addNebulaParticle(project.getLocation(),(Vector2f) project.getVelocity().scale(0.3f),project.getCollisionRadius() * 3f,
                                        3f,-1f,2f,3f,
                                        EFFECT_3);
                                Global.getSoundPlayer().playSound("ui_drone_mode_deploy",2f,0.3f,project.getLocation(),new Vector2f());
                            }
                        }
                    }
                }


            }

            //engine.addFloatingText(ship.getLocation(),String.valueOf(currState.get(ship).sprites.size()),20f,Color.WHITE,ship,1f,5f);
            //engine.addFloatingText(ship.getLocation(),String.valueOf(currState.get(ship).angle),20f,Color.WHITE,ship,1f,1f);


        }
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {


        if (index == 0) return "" + (int)(PD_MULT * 100f) + "%";
        if (index == 1) return "" + (int)(FLUX_MULT * 100f) + "%";

        return null;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship.getVariant().hasHullMod(FantasyBasicMod.FANTASYBASICMOD)) return true;
        return false;
    }

    public String getUnapplicableReason(ShipAPI ship) {

        if (!ship.getVariant().hasHullMod(FantasyBasicMod.FANTASYBASICMOD)) {
            return I18nUtil.getHullModString("FM_HullModRequireBasicMod");
        }

        return null;
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {

        String[] data = {String.valueOf(mag.get(ShipAPI.HullSize.FRIGATE).intValue()),
                String.valueOf(mag.get(ShipAPI.HullSize.DESTROYER).intValue()),
                String.valueOf(mag.get(ShipAPI.HullSize.CRUISER).intValue()),
                String.valueOf(mag.get(ShipAPI.HullSize.CAPITAL_SHIP).intValue())};

        tooltip.addSpacer(10f);
        //说明
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_Instruction"), Alignment.TMID,4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyKawausoMod_I_0"), Misc.getHighlightColor(), 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyKawausoMod_I_1"),4f,Misc.getHighlightColor(), data);
        tooltip.addPara(I18nUtil.getHullModString("FantasyKawausoMod_I_2"), Misc.getGrayColor(), 4f);
        tooltip.addSpacer(10f);
        //描述与评价
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_DescriptionAndEvaluation"), Alignment.TMID,4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyKawausoMod_DAE_0"), Misc.getTextColor(), 4f);
        tooltip.addSpacer(10f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyKawausoMod_DAE_1"), Misc.getGrayColor(), 4f);

    }

    private final static class ModState {
        boolean isActive;
        List<SpriteAPI> sprites;
        float timer;
        float angle;
        Map<SpriteAPI,Float> sprite_angle_0;
        Map<SpriteAPI,Float> trail_id;

        private ModState() {
            sprites = new ArrayList<>();
            isActive = false;
            timer = 0f;
            angle = 0f;
            sprite_angle_0 = new HashMap<>();
            trail_id = new HashMap<>();
        }
    }

}
