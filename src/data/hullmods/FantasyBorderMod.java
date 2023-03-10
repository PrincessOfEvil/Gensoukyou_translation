package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_LocalData;
import data.utils.I18nUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FantasyBorderMod extends BaseHullMod {

    public static final float EFFECT_TIME = 1f;
    public static final float D_FLUX_BONS = 0.85f;
    public static final float FLUX_LEVEL = 0.9f;
    public static final int NUM_OF_BOMBS = 3;

    public static final Color SHIP = new Color(151, 8, 53, 255);
    public static final Color EMP1 = new Color(213, 96, 96, 255);
    public static final Color EMP2 = new Color(123, 0, 0, 184);

    public static Object INFO2;

    FM_LocalData.FM_Data currdata = FM_LocalData.getCurrData();


    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getFluxCapacity().modifyMult(id, D_FLUX_BONS);

    }

//    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
//
//        if(ship == null)return;
//
//        currdata.numberOfBombs.put(ship,NUM_OF_BOMBS);
//    }

    public void advanceInCombat(ShipAPI ship, float amount) {


        if (ship == null) {
            return;
        }

        CombatEngineAPI engine = Global.getCombatEngine();
        if (!engine.getCustomData().containsKey("FantasyBorderMod")) {
            engine.getCustomData().put("FantasyBorderMod", new HashMap<>());
        }

        if (currdata == null)return;


        currdata.withFM_BorderMod.put(ship, true);

        if (ship.isAlive()) {
            if (currdata.withFM_BorderMod.get(ship)) {

                float fluxlevel = ship.getFluxTracker().getFluxLevel();

                Map<ShipAPI, ModState> currState = (Map) engine.getCustomData().get("FantasyBorderMod");

                if (!currState.containsKey(ship)) {
                    currState.put(ship, new ModState());
                }

                //检测幅能
                if ((fluxlevel > FLUX_LEVEL || ship.getFluxTracker().isOverloaded()) && currState.get(ship).num > 0) {

                    currState.get(ship).isActive = true;
                }

                //测试效果
                if (currState.get(ship).isActive) {
                    ship.getFluxTracker().setHardFlux(0.95f * ship.getFluxTracker().getHardFlux());
                    ship.getFluxTracker().setCurrFlux(0.95f * ship.getFluxTracker().getCurrFlux());
                    currState.get(ship).timer = currState.get(ship).timer + amount;

                    ship.setJitterUnder(ship, SHIP, 3f, 2, 3f);

                    ship.getShield().toggleOn();

                    Global.getSoundPlayer().playLoop("FM_bordermod_se", ship, 1f, 1f, ship.getLocation(), new Vector2f());

                    //消弹

                    float effect_range = FantasyBasicMod.magRANGE.get(ship.getHullSize());
                    List<DamagingProjectileAPI> projects = engine.getProjectiles();
                    for (DamagingProjectileAPI project : projects) {
                        if (project.getOwner() != ship.getOwner() && MathUtils.isWithinRange(project, ship, effect_range)) {
                            engine.spawnEmpArc(ship, ship.getLocation(), ship, project, DamageType.ENERGY, 0f, 0f, 100000f,
                                    "tachyon_lance_emp_impact", 30f, EMP2, EMP1);
                            //engine.spawnExplosion(project.getLocation(), new Vector2f(), SHIP, project.getCollisionRadius(), 1f);

                            NegativeExplosionVisual.NEParams neEffect = new NegativeExplosionVisual.NEParams();
                            neEffect.color = SHIP;
                            neEffect.thickness = 8f;
                            neEffect.radius = Math.min(project.getCollisionRadius() * 0.6f,14f);
                            neEffect.fadeOut = MathUtils.getRandomNumberInRange(0.25f,0.6f);
                            neEffect.underglow = EMP2;
                            neEffect.invertForDarkening = EMP1;

                            CombatEntityAPI visual = engine.addLayeredRenderingPlugin(new NegativeExplosionVisual(neEffect));
                            visual.getLocation().set(project.getLocation());


                            engine.removeEntity(project);
                        }
                    }

                }
                if (currState.get(ship).timer > EFFECT_TIME) {
                    currState.get(ship).timer = 0f;
                    currState.get(ship).isActive = false;
                    ship.getFluxTracker().stopOverload();


                    currState.get(ship).num = currState.get(ship).num - 1;
                }

                //debug
                //engine.addFloatingText(ship.getLocation(), String.valueOf(currState.get(ship).num),10, Color.WHITE,ship,1,1);
                //Global.getLogger(this.getClass()).info(currState.get(ship).num);

                if (ship == engine.getPlayerShip()) {
                    engine.maintainStatusForPlayerShip(INFO2, Global.getSettings().getSpriteName("ui", "icon_energy"), I18nUtil.getHullModString("FantasyBorderMod_Combat_0_T"), I18nUtil.getHullModString("FantasyBorderMod_Combat_0_D") +
                            currState.get(ship).num, false);
                }
            }
        }
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship.getVariant().hasHullMod(FantasyBasicMod.FANTASYBASICMOD) && ship.getShield() != null) return true;
        return false;
    }

    public String getUnapplicableReason(ShipAPI ship) {

        if (!ship.getVariant().hasHullMod(FantasyBasicMod.FANTASYBASICMOD)) {
            return I18nUtil.getHullModString("FM_HullModRequireBasicMod");
        }
        if (ship.getShield() == null) {
            return I18nUtil.getHullModString("FM_HullModRequireShield");
        }

        return null;
    }


    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int)(D_FLUX_BONS * 100) + "%";
        if (index == 1) return "" + (int)(FLUX_LEVEL * 100) + "%";
        if (index == 2) return "" + (int)EFFECT_TIME;
        if (index == 3) return "" + NUM_OF_BOMBS;
        return null;
    }


    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addSpacer(10f);
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_Instruction"), Alignment.TMID,4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyBorderMod_I_0"), Misc.getTextColor(), 4f);
        tooltip.addSpacer(10f);
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_DescriptionAndEvaluation"), Alignment.TMID,4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyBorderMod_DAE_0"), Misc.getTextColor(), 4f);
        tooltip.addSpacer(10f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyBorderMod_DAE_1"), Misc.getGrayColor(), 4f);
    }

    private final static class ModState {
        boolean isActive;
        float timer;
        int num;

        private ModState() {
            isActive = false;
            timer = 0f;
            num = 3;
        }
    }
}
