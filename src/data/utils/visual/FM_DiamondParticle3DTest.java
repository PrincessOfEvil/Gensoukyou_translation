package data.utils.visual;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.EnumSet;

public class FM_DiamondParticle3DTest extends BaseCombatLayeredRenderingPlugin {

    public static class FM_DP3DParams implements Cloneable{
        public float fadeIn = 0.1f;
        public float fadeOut = 0.5f;
        public float radius = 20f;
        public float spin = 1f;
        public float spinZ = 1f;
        public float thickness = 6f;
        public Color color = new Color(100,100,255);
        public Vector2f vel = new Vector2f();
        public Vector2f loc = new Vector2f();

        public FM_DP3DParams() {
        }

        @Override
        protected FM_DP3DParams clone() {
            try {
                return (FM_DP3DParams) super.clone();
            } catch (CloneNotSupportedException e) {
                return null; // should never happen
            }
        }
    }

    protected FaderUtil fader;
    protected SpriteAPI tex;

    protected FM_DP3DParams params;

    protected int segments;
    protected float facing;
    protected float zAngle;
    protected boolean spinDirection;

    public FM_DiamondParticle3DTest(FM_DP3DParams params){
        this.params = params;
    }

    public float getRenderRadius() {
        return params.radius + 500f;
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(CombatEngineLayers.CONTRAILS_LAYER);
    }

    public void advance(float amount){
        if (Global.getCombatEngine().isPaused()) return;

        fader.advance(amount);
        if (spinDirection){
            facing = facing + amount * params.spin;
            zAngle = zAngle + amount * params.spinZ;
        }else {
            facing = facing - amount * params.spin;
            zAngle = zAngle - amount * params.spinZ;
        }




    }

    public void init(CombatEntityAPI entity) {
        super.init(entity);

        fader = new FaderUtil(0f, params.fadeIn, params.fadeOut);
        fader.setBounceDown(true);
        fader.fadeIn();

        tex = Global.getSettings().getSprite("fx", "FM_DiamondParticle3DTest_ring");

        entity.getLocation().set(params.loc);
        entity.getVelocity().set(params.vel);

        if (MathUtils.getRandomNumberInRange(0f,1f) > 0.5f){
            spinDirection = false;
        }else {
            spinDirection = true;
        }

        segments = 4;

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
        if (layer == CombatEngineLayers.CONTRAILS_LAYER) {
            float circleAlpha = 1f;
            if (alphaMult < 0.5f) {
                circleAlpha = alphaMult * 2f;
            }
            renderAtmosphere(x,y,r,facing,zAngle,params.thickness,circleAlpha,segments,tex,params.color,true);
        }
        //GL14.glBlendEquation(GL14.GL_FUNC_ADD);

//		GL11.glPopMatrix();
    }


    private void renderAtmosphere(float x, float y, float radius, float facing, float zAngle, float thickness, float alphaMult, int segments, SpriteAPI tex, Color color, boolean additive) {
        //角度与节点处理
        float radius2 = 0.5f * radius;

        Vector2f rotateAxis = Misc.getUnitVectorAtDegreeAngle(facing + 90f);

        GL11.glPushMatrix();
        //平移函数，矩阵相关操作
        GL11.glTranslatef(x, y, 0);
        //旋转函数，矩阵相关操作
        GL11.glRotatef(zAngle, rotateAxis.x, rotateAxis.y, 0);
        //纹理等
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        tex.bindTexture();

        //混合颜色的方式.aya
        GL11.glEnable(GL11.GL_BLEND);
        if (additive) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        } else {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
        GL11.glColor4ub((byte)color.getRed(),
                (byte)color.getGreen(),
                (byte)color.getBlue(),
                (byte)((float) color.getAlpha() * alphaMult));

        //一种特殊好用的四边形的绘制方法.png
        //重点系列
        //非常惊喜的独特顺序（）
        //例如如果用此法绘制一个矩形，顺序是 先一条边 再对角线连接至起点同侧 再另一条边
        //而不是一笔画下来的正常顺序
        //STRIP的原因（？
        //有他自己的好处
        //比如给两个同样方向的平行向量（）

        GL11.glBegin(GL11.GL_QUAD_STRIP);

        for (float i = 0; i < segments + 1; i++) {
            boolean last = i == segments;
            if (last) i = 0;
            float theta = (float) Math.toRadians(90f * i + facing);

            float cos = (float) Math.cos(theta);
            float sin = (float) Math.sin(theta);

            //噪点和尖锐？
            float m1 = 1f;

            //同向平行.aya
            float x1;
            float y1;
            float x2;
            float y2;
            if (i % 2 != 0){
                x1 = cos * radius * m1;
                y1 = sin * radius * m1;
                x2 = cos * (radius + thickness);
                y2 = sin * (radius + thickness);
            }else {
                x1 = cos * radius2 * m1;
                y1 = sin * radius2 * m1;
                x2 = cos * (radius2 + thickness);
                y2 = sin * (radius2 + thickness);

            }

//            x1 = cos * radius * m1;
//            y1 = sin * radius * m1;
//            x2 = cos * (radius + thickness);
//            y2 = sin * (radius + thickness);

            //其实肯定的还可以整点更劲的

            //正确的在四边形上表示纹理的方式：
            //图的各个顶点（整个的肯定是矩形啦）
            //与四边形的定点对应
            //因为是循环绘制所以起点对起点的意思（）
            //s与t也是对应x与y轴的
            GL11.glTexCoord2f(0.5f, 0.05f);
            GL11.glVertex2f(x1, y1);
            GL11.glTexCoord2f(0.5f, 0.95f);
            GL11.glVertex2f(x2, y2);

            if (last) break;
        }


        GL11.glEnd();
        GL11.glPopMatrix();
    }


}
