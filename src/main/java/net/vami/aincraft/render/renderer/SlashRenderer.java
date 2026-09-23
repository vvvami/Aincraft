package net.vami.aincraft.render.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.render.SlashEffect;

import java.util.*;

@EventBusSubscriber(modid = Aincraft.MOD_ID, value = Dist.CLIENT)
public final class SlashRenderer {

    private static final ArrayList<ActiveSlash> ACTIVE = new ArrayList<>();

    public static void spawn(Player player, SlashEffect effect) {
        ACTIVE.add(new ActiveSlash(player, effect));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Iterator<ActiveSlash> iterator = ACTIVE.iterator();

        while (iterator.hasNext()) {
            ActiveSlash slash = iterator.next();
            slash.tick();

            if (slash.isFinished()) iterator.remove();
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) return;

        Minecraft minecraft = Minecraft.getInstance();
        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        Set<RenderType> usedRenderTypes = new HashSet<>();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (ActiveSlash slash : ACTIVE) {
            SlashEffect effect = slash.effect;

            RenderType renderType = NeoForgeRenderTypes.getUnlitTranslucent(effect.texture(), false);

            usedRenderTypes.add(renderType);
            renderSlash(minecraft, poseStack, buffers.getBuffer(renderType), slash, partialTick);
        }

        poseStack.popPose();

        for (RenderType renderType : usedRenderTypes) buffers.endBatch(renderType);
    }

    private static void renderSlash(Minecraft minecraft, PoseStack poseStack, VertexConsumer consumer, ActiveSlash slash, float partialTick) {
        SlashEffect effect = slash.effect;

        float progress = slash.getProgress(partialTick);
        float reveal = easeOutCubic(Mth.clamp(progress / effect.revealTime(), 0, 1));

        float fade = progress < effect.fadeStart()
                ? 1
                : 1 - (progress - effect.fadeStart()) / (1 - effect.fadeStart());

        fade = Mth.clamp(fade, 0, 1);

        double innerRadius = effect.radius() - effect.thickness() / 2;
        double outerRadius = effect.radius() + effect.thickness() / 2;

        Vec3 normal = slash.getNormal();

        for (int i = 0; i < effect.segments(); i++) {
            float t0 = i / (float) effect.segments();
            if (t0 >= reveal) break;

            float t1 = Math.min((i + 1) / (float) effect.segments(), reveal);

            double epsilon = 0.0005;

            double angle0 = Math.toRadians(Mth.lerp(t0, effect.startAngle(), effect.endAngle())) - epsilon;
            double angle1 = Math.toRadians(Mth.lerp(t1, effect.startAngle(), effect.endAngle())) + epsilon;

            Vec3 inner0 = slash.getArcPoint(angle0, innerRadius, partialTick);
            Vec3 outer0 = slash.getArcPoint(angle0, outerRadius, partialTick);
            Vec3 inner1 = slash.getArcPoint(angle1, innerRadius, partialTick);
            Vec3 outer1 = slash.getArcPoint(angle1, outerRadius, partialTick);

            int color0 = interpolateColor(effect.startColor(), effect.endColor(), t0, fade);
            int color1 = interpolateColor(effect.startColor(), effect.endColor(), t1, fade);

            int light0 = getLight(minecraft, effect, inner0);
            int light1 = getLight(minecraft, effect, inner1);

            Vec3 depthOffset = normal.scale(effect.thickness() / 2);

            Vec3 inner0Front = inner0.add(depthOffset);
            Vec3 outer0Front = outer0.add(depthOffset);
            Vec3 inner1Front = inner1.add(depthOffset);
            Vec3 outer1Front = outer1.add(depthOffset);

            Vec3 inner0Back = inner0.subtract(depthOffset);
            Vec3 outer0Back = outer0.subtract(depthOffset);
            Vec3 inner1Back = inner1.subtract(depthOffset);
            Vec3 outer1Back = outer1.subtract(depthOffset);

            Vec3 radialNormal = slash.getHorizontalAxis().scale(Math.cos((angle0 + angle1) / 2.0))
                    .add(slash.getVerticalAxis().scale(Math.sin((angle0 + angle1) / 2.0)))
                    .normalize();

            renderQuad(poseStack, consumer, inner0Front, outer0Front, outer1Front, inner1Front,
                    t0, t1, color0, color1, light0, light1, normal);

            renderQuad(poseStack, consumer, outer0Back, outer0Front, outer1Front, outer1Back,
                    t0, t1, color0, color1, light0, light1, radialNormal);

            renderQuad(poseStack, consumer, inner0Front, inner0Back, inner1Back, inner1Front,
                    t0, t1, color0, color1, light0, light1, radialNormal.scale(-1));}
    }

    private static void renderQuad(PoseStack poseStack, VertexConsumer consumer, Vec3 inner0, Vec3 outer0, Vec3 outer1, Vec3 inner1,
                                   float u0, float u1, int color0, int color1, int light0, int light1, Vec3 normal) {
        PoseStack.Pose pose = poseStack.last();

        vertex(consumer, pose, inner0, u0, 1, color0, light0, normal);
        vertex(consumer, pose, outer0, u0, 0, color0, light0, normal);
        vertex(consumer, pose, outer1, u1, 0, color1, light1, normal);
        vertex(consumer, pose, inner1, u1, 1, color1, light1, normal);

        Vec3 reverseNormal = normal.scale(-1);

        vertex(consumer, pose, inner1, u1, 1, color1, light1, reverseNormal);
        vertex(consumer, pose, outer1, u1, 0, color1, light1, reverseNormal);
        vertex(consumer, pose, outer0, u0, 0, color0, light0, reverseNormal);
        vertex(consumer, pose, inner0, u0, 1, color0, light0, reverseNormal);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Vec3 position, float u, float v, int color, int light, Vec3 normal) {
        int a = color >>> 24 & 0xFF;
        int r = color >>> 16 & 0xFF;
        int g = color >>> 8 & 0xFF;
        int b = color & 0xFF;

        consumer.addVertex(pose, (float) position.x, (float) position.y, (float) position.z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    private static int getLight(Minecraft minecraft, SlashEffect effect, Vec3 position) {
        return LightTexture.FULL_BRIGHT;
    }

    private static float easeOutCubic(float value) {
        return 1.0f - (float) Math.pow(1.0f - value, 3.0);
    }

    private static int interpolateColor(int from, int to, float progress, float alphaMultiplier) {
        int a = (int) Mth.lerp(progress, from >>> 24 & 0xFF, to >>> 24 & 0xFF);
        int r = (int) Mth.lerp(progress, from >>> 16 & 0xFF, to >>> 16 & 0xFF);
        int g = (int) Mth.lerp(progress, from >>> 8 & 0xFF, to >>> 8 & 0xFF);
        int b = (int) Mth.lerp(progress, from & 0xFF, to & 0xFF);

        a = (int) (a * alphaMultiplier);

        return a << 24 | r << 16 | g << 8 | b;
    }

    private static class ActiveSlash {

        private final SlashEffect effect;

        private final Vec3 origin;
        private final Vec3 forward;
        private final Vec3 horizontalAxis;
        private final Vec3 verticalAxis;

        private int age;

        private ActiveSlash(Player player, SlashEffect effect) {
            this.effect = effect;

            float yaw = player.getYRot() * Mth.DEG_TO_RAD;

            forward = new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw)).normalize();

            Vec3 right = new Vec3(forward.z, 0, -forward.x);
            Vec3 up = new Vec3(0, 1, 0);

            double rotation = Math.toRadians(effect.rotation());

            Vec3 swingAxis = up.scale(Math.cos(rotation))
                    .add(right.scale(Math.sin(rotation)))
                    .normalize();

            horizontalAxis = forward;
            verticalAxis = swingAxis;

            origin = player.getEyePosition().add(effect.xOffset(), effect.yOffset(), effect.zOffset());
        }

        private void tick() {
            age++;
        }

        private boolean isFinished() {
            return age >= effect.lifetime();
        }

        private float getProgress(float partialTick) {
            return Mth.clamp((age + partialTick) / effect.lifetime(), 0, 1);
        }

        private Vec3 getCenter(float partialTick) {
            float progress = Mth.clamp((age + partialTick) / effect.lifetime(), 0, 1);
            double distance = Mth.lerp(progress, effect.distance(), effect.endDistance());

            return origin.add(forward.scale(distance));
        }

        private Vec3 getNormal() {
            return horizontalAxis.cross(verticalAxis).normalize();
        }

        private Vec3 getArcPoint(double angle, double radius, float partialTick) {
            return getCenter(partialTick)
                    .add(horizontalAxis.scale(Math.cos(angle) * radius))
                    .add(verticalAxis.scale(Math.sin(angle) * radius));
        }

        public Vec3 getHorizontalAxis() {
            return horizontalAxis;
        }

        public Vec3 getVerticalAxis() {
            return verticalAxis;
        }
    }
}
