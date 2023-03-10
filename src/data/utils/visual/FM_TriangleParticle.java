package data.utils.visual;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect;
import com.fs.starfarer.api.util.FaderUtil;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.EnumSet;

/**
 * Just try to using OpenGL............
 */
public class FM_TriangleParticle extends BaseCombatLayeredRenderingPlugin {

    public static class FM_TPParams implements Cloneable{
        public float fadeIn = 0.1f;
        public float fadeOut = 0.5f;
        public float spawnHitGlowAt = 0f;
        public float hitGlowSizeMult = 0.75f;
        public float radius = 20f;
        public float thickness = 25f;
        public boolean withHitGlow = true;
        public Color color = new Color(100,100,255);
        public Color underglow = RiftCascadeEffect.EXPLOSION_UNDERCOLOR;
        public Color invertForDarkening = null;

        public FM_TPParams() {
        }

        public FM_TPParams(float radius, float thickness, Color color) {
            super();
            this.radius = radius;
            this.thickness = thickness;
            this.color = color;
        }

        @Override
        protected FM_TPParams clone() {
            try {
                return (FM_TPParams) super.clone();
            } catch (CloneNotSupportedException e) {
                return null; // should never happen
            }
        }
    }

    protected FaderUtil fader;
    protected SpriteAPI tex;

    protected FM_TPParams params;

    protected int segments;

    protected boolean spawnedHitGlow = false;

    public FM_TriangleParticle(FM_TPParams params){
        this.params = params;
    }

    public float getRenderRadius() {
        return params.radius + 500f;
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES_LOWER);
    }

    public void advance(float amount){
        if (Global.getCombatEngine().isPaused()) return;

        fader.advance(amount);

        if (!params.withHitGlow) return;

    }

    public void init(CombatEntityAPI entity) {
        super.init(entity);

        fader = new FaderUtil(0f, params.fadeIn, params.fadeOut);
        fader.setBounceDown(true);
        fader.fadeIn();

        tex = Global.getSettings().getSprite("combat", "corona_hard");

        segments = 6;

    }

    public boolean isExpired() {
        return fader.isFadedOut();
    }


    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        float x = entity.getLocation().x;
        float y = entity.getLocation().y;

        float f = fader.getBrightness();
        float alphaMult = viewport.getAlphaMult();
        if (f < 0.5f) {
            alphaMult *= f * 2f;
        }

        float r = params.radius;

        if (fader.isFadingIn()) {
            r *= 0.75f + Math.sqrt(f) * 0.25f;
        } else {
            r *= 0.1f + 0.9f * f;
        }

//		GL11.glPushMatrix();
//		GL11.glTranslatef(x, y, 0);
//		GL11.glScalef(6f, 6f, 1f);
//		x = y = 0;

        //GL14.glBlendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
        if (layer == CombatEngineLayers.ABOVE_PARTICLES_LOWER) {
            float circleAlpha = 1f;
            if (alphaMult < 0.5f) {
                circleAlpha = alphaMult * 2f;
            }
            renderGraph(x, y, r, circleAlpha, segments, params.color);
        }
        //GL14.glBlendEquation(GL14.GL_FUNC_ADD);

//		GL11.glPopMatrix();
    }


    private void renderGraph(float x, float y, float radius, float alphaMult, int segments, Color color) {
        if (fader.isFadingIn()) alphaMult = 1f;


        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glRotatef(0, 0, 0, 1);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);


        GL11.glColor4ub((byte)color.getRed(),
                (byte)color.getGreen(),
                (byte)color.getBlue(),
                (byte)((float) color.getAlpha() * alphaMult));

        //空心和实心多边形（大雾

        GL11.glBegin(GL11.GL_LINE_STRIP);
//        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glVertex2f(radius, 0);
//        GL11.glVertex2f(0, 0);


        for (float i = 0; i < segments + 1; i++) {
            //记得从0开始算.aya
            boolean last = i == segments;

            if (last) i = 0;//在回到起点

            float theta =(float) Math.toRadians(60f * i);
            float x1 = ((float) Math.cos(theta)) * radius;
            float y1 = ((float) Math.sin(theta)) * radius;

            GL11.glVertex2f(x1, y1);

            if (last) break;
        }


        GL11.glEnd();
        GL11.glPopMatrix();

    }

}
