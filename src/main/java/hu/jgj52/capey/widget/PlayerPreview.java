package hu.jgj52.capey.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tr7zw.transition.mc.EntityUtil;
import dev.tr7zw.transition.mc.LightingUtil;
import dev.tr7zw.transition.mc.MathUtil;
import dev.tr7zw.trender.gui.client.RenderContext;
import dev.tr7zw.trender.gui.widget.WPlayerPreview;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * stolen code from <a href="https://github.com/tr7zw/TRender/blob/master/src/main/java/dev/tr7zw/trender/gui/widget/WPlayerPreview.java">trender</a>
 * because i dont want it to specifically be the user's skin
 */
public class PlayerPreview extends WPlayerPreview {
    private final Player player;

    public PlayerPreview(Player player) {
        this.player = player;
    }
    
    @Override
    public void paint(RenderContext context, int x, int y, int mouseX, int mouseY) {
        if (isShowBackground()) {
            context.fill(x, y, x + getWidth(), y + getHeight(), 0xFF000000);
        }
        drawEntity(context, x + getWidth() / 2, y + getHeight() / 2, this.getWidth(), this.getHeight(), 40,
                getRotationX(), getRotationY(), player, 0);
    }

    private static void drawEntity(RenderContext context, int x, int y, int width, int height, int size, float lookX,
                                   float lookY, LivingEntity livingEntity, float delta) {
        float rotationModifier = 3;
        prepareViewMatrix(x, y);
        PoseStack matrixStack = new PoseStack();
        matrixStack.translate(x, y, 500.0D);
        matrixStack.scale((float) size, (float) size, (float) size);
        matrixStack.scale(1.0F, 1.0F, -1.0F);
        var quaternion = MathUtil.ZP.rotationDegrees(180.0F);
        var quaternion2 = MathUtil.XP.rotationDegrees(lookY * rotationModifier);
        quaternion.mul(quaternion2);
        matrixStack.mulPose(quaternion);
        matrixStack.translate(0.0D, -1, 0D);
        float yBodyRot = livingEntity.yBodyRot;
        float yRot = EntityUtil.getYRot(livingEntity);
        float yRotO = livingEntity.yRotO;
        float yBodyRotO = livingEntity.yBodyRotO;
        float xRot = EntityUtil.getXRot(livingEntity);
        float xRotO = livingEntity.xRotO;
        float yHeadRotO = livingEntity.yHeadRotO;
        float yHeadRot = livingEntity.yHeadRot;
        Vec3 vel = livingEntity.getDeltaMovement();
        livingEntity.yBodyRot = (180.0F + lookX * rotationModifier);
        EntityUtil.setYRot(livingEntity, (180.0F + lookX * rotationModifier));
        livingEntity.yBodyRotO = livingEntity.yBodyRot;
        livingEntity.yRotO = EntityUtil.getYRot(livingEntity);
        livingEntity.setDeltaMovement(Vec3.ZERO);
        EntityUtil.setXRot(livingEntity, 0);
        livingEntity.xRotO = EntityUtil.getXRot(livingEntity);
        livingEntity.yHeadRot = EntityUtil.getYRot(livingEntity);
        livingEntity.yHeadRotO = EntityUtil.getYRot(livingEntity);
        LightingUtil.prepareLightingEntity();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        MathUtil.conjugate(quaternion2);
        //? if < 1.21.9 {

        /*entityRenderDispatcher.overrideCameraOrientation(quaternion2);
        entityRenderDispatcher.setRenderShadow(false);
        *///? }
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        //? if >= 1.21.6 {

        var entityRenderer = entityRenderDispatcher.getRenderer(livingEntity);
        var entityRenderState = entityRenderer.createRenderState(livingEntity, 1.0F);
        //? if >= 1.21.11 {

        entityRenderState.boundingBoxHeight = 0;
        entityRenderState.boundingBoxWidth = 0;
        //? } else {

        /*entityRenderState.hitboxesRenderState = null;
         *///? }
        float o = livingEntity.getScale();
        var vector3f = new org.joml.Vector3f(0.0F, livingEntity.getBbHeight() / 2.0F + 0 * o, 0.0F);
        float p = (float) size / o;
        //? if >= 26.0 {

        context.getGuiGraphics().entity(entityRenderState, p, vector3f, quaternion, quaternion2, x - width, y - height,
                x + width, y + height);
        //? } else {

        /*context.getGuiGraphics().submitEntityRenderState(entityRenderState, p, vector3f, quaternion, quaternion2,
                x - width, y - height, x + width, y + height);
         *///? }
        //? } else if >= 1.21.2 {
            /*
             entityRenderDispatcher.render(livingEntity, 0.0D, 0.0D, 0.0D, delta, matrixStack, bufferSource, 15728880);
            *///? } else if >= 1.17.0 {

        /*entityRenderDispatcher.render(livingEntity, 0.0D, 0.0D, 0.0D, 0.0F, delta, matrixStack, bufferSource, 15728880);
         *///? } else {
        /*
         RenderSystem.runAsFancy(() -> {
            entityRenderDispatcher.render(livingEntity, 0.0D, 0.0D, 0.0D, 0.0F, delta, matrixStack, bufferSource, 15728880);
         });
        *///? }
        bufferSource.endBatch();
        //? if < 1.21.9 {

        /*entityRenderDispatcher.setRenderShadow(true);
         *///? }
        livingEntity.yBodyRot = yBodyRot;
        livingEntity.yBodyRotO = yBodyRotO;
        EntityUtil.setYRot(livingEntity, yRot);
        livingEntity.yRotO = yRotO;
        EntityUtil.setXRot(livingEntity, xRot);
        livingEntity.xRotO = xRotO;
        livingEntity.yHeadRotO = yHeadRotO;
        livingEntity.yHeadRot = yHeadRot;
        livingEntity.setDeltaMovement(vel);
        resetViewMatrix();
    }

    private static void resetViewMatrix() {
        //?  if >= 1.17.0 && < 1.21.2 {

        /*RenderSystem.applyModelViewMatrix();
         *///? } else if < 1.17.0 {
        /*
         RenderSystem.popMatrix();
        *///? }
    }

    private static void prepareViewMatrix(double xpos, double ypos) {
        //? if >= 1.17.0 && < 1.21.2 {

        /*RenderSystem.applyModelViewMatrix();
         *///? } else if < 1.17.0 {
        /*
         RenderSystem.pushMatrix();
         RenderSystem.translatef((float)xpos, (float)ypos, 1050.0F);
         RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        *///? }
    }
}
