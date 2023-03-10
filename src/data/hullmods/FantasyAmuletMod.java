package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import data.scripts.util.MagicRender;
import data.utils.FM_ProjectEffect;
import data.utils.I18nUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FantasyAmuletMod extends BaseHullMod {

    public static final float TIME = 0.4f;


    public void advanceInCombat(ShipAPI ship, float amount) {

        if (ship == null) {
            return;
        }

        CombatEngineAPI engine = Global.getCombatEngine();

        if (engine == null)return;

        if (!engine.getCustomData().containsKey("FantasyAmuletMod")) {
            engine.getCustomData().put("FantasyAmuletMod", new HashMap<>());
        }

        Map<ShipAPI, FantasyAmuletMod.ModState> currState = (Map) engine.getCustomData().get("FantasyAmuletMod");

        if (!currState.containsKey(ship)) {
            currState.put(ship, new ModState());
        }

        if (!currState.get(ship).isActive) {
            currState.get(ship).weapons = ship.getAllWeapons();
            currState.get(ship).isActive = true;
        }

        if (currState.get(ship).isActive) {

            currState.get(ship).timer = currState.get(ship).timer + amount;

            if (currState.get(ship).timer >= TIME) {
                currState.get(ship).timer = TIME;
            }

            for (DamagingProjectileAPI proj : FM_ProjectEffect.ProjectsThisFrame) {
                //debug
//                if (proj.didDamage()){
//                       engine.addFloatingText(proj.getLocation(),proj.getSource().toString(),10f,Color.WHITE,proj,1f,1f);
//                }

                if (proj.getSource() == ship && proj.didDamage() && currState.get(ship).timer >= TIME && !(proj instanceof MissileAPI)) {

                    if (proj.getDamageTarget().getOwner() != ship.getOwner()
                            && proj.getDamageTarget() instanceof ShipAPI
                            && ((ShipAPI) proj.getDamageTarget()).isAlive()){

                        for (WeaponAPI weapon : currState.get(ship).weapons) {
                            if (weapon.getId().equals("FM_Amulet_B")) {
                                engine.spawnProjectile(ship, weapon, "FM_Amulet_B", weapon.getLocation(), weapon.getCurrAngle(), new Vector2f());
                                Global.getSoundPlayer().playSound("harpoon_fire", 10f, 0.5f, weapon.getLocation(), ship.getVelocity());

                                MagicRender.battlespace(Global.getSettings().getSprite("fx", "FM_modeffect_4"),
                                        weapon.getLocation(),
                                        MathUtils.getRandomPointInCircle(weapon.getShip().getVelocity(), 20f), new Vector2f(30f, 30f),
                                        new Vector2f(10f, 10f), MathUtils.getRandomNumberInRange(0, 360),
                                        10f, new Color(236, 56, 56, 221), true, 0.1f, 0.6f, 0.3f);
                            }
                        }

                        currState.get(ship).timer = 0f;
                    }

                }
            }

        }

    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + 8 + I18nUtil.getHullModString("FantasyAmuletMod_HL_0");
        if (index == 1) return "" + TIME + I18nUtil.getHullModString("FantasyAmuletMod_HL_1");
        if (index == 2) return "" + 70 + I18nUtil.getHullModString("FantasyAmuletMod_HL_2");

        return null;
    }

    private final static class ModState {
        boolean isActive;
        List<WeaponAPI> weapons;
        float timer;

        private ModState() {
            isActive = false;
            timer = 0f;

        }
    }

}
