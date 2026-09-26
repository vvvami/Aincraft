package net.vami.aincraft.render.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
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

            RenderType renderType = NeoForgeRenderTypes.getUnlitTranslucent(effect.texture(), true);

            usedRenderTypes.add(renderType);
            renderSlash(poseStack, buffers.getBuffer(renderType), slash, partialTick, cameraPos);
        }

        poseStack.popPose();

        for (RenderType renderType : usedRenderTypes) buffers.endBatch(renderType);
    }

    private static void renderSlash(PoseStack poseStack, VertexConsumer consumer, ActiveSlash slash, float partialTick, Vec3 cameraPos) {
        SlashEffect effect = slash.effect;

        float progress = slash.getProgress(partialTick);
        float reveal = easeOutCubic(Mth.clamp(progress / effect.revealTime(), 0, 1));

        float fade = progress < effect.fadeStart() ? 1
                : 1 - (progress - effect.fadeStart()) / (1 - effect.fadeStart());

        fade = Mth.clamp(fade, 0, 1);

        if (reveal <= 0) return;

        int renderSegments;

        if (effect.shape() == SlashEffect.Shape.ARC) {
            double arcLength = Math.toRadians(Math.abs(effect.endAngle() - effect.startAngle())) * effect.radius();
            renderSegments = Mth.clamp((int) Math.ceil(arcLength * 8), 12, 96);
        } else {
            renderSegments = 1;
        }

        Vec3[] centers = new Vec3[renderSegments + 1];
        Vec3[] left = new Vec3[renderSegments + 1];
        Vec3[] right = new Vec3[renderSegments + 1];

        for (int i = 0; i <= renderSegments; i++) {
            float t = reveal * i / (float) renderSegments;
            centers[i] = getVisualPoint(slash, t, partialTick);
        }

        for (int i = 0; i <= renderSegments; i++) {
            Vec3 tangent;

            if (i == 0) {
                tangent = centers[1].subtract(centers[0]).normalize();
            } else if (i == renderSegments) {
                tangent = centers[i].subtract(centers[i - 1]).normalize();
            } else {
                tangent = centers[i + 1].subtract(centers[i - 1]).normalize();
            }

            Vec3 view = cameraPos.subtract(centers[i]).normalize();
            Vec3 widthAxis = view.cross(tangent).normalize();

            Vec3 widthOffset = widthAxis.scale(effect.thickness());

            left[i] = centers[i].subtract(widthOffset);
            right[i] = centers[i].add(widthOffset);
        }

        int color = interpolateColors(effect.colors(), slash.getProgress(partialTick), fade);

        for (int i = 0; i < renderSegments; i++) {
            float t1 = reveal * i / (float) renderSegments;
            float t2 = reveal * (i + 1) / (float) renderSegments;

            renderQuad(poseStack, consumer, left[i], right[i], right[i + 1], left[i + 1], t1, t2, color, color, slash.getNormal());
        }
    }

    private static Vec3 getVisualPoint(ActiveSlash slash, float progress, float partialTick) {
        SlashEffect effect = slash.effect;

        if (effect.shape() == SlashEffect.Shape.LINE) {
            return slash.getLinePoint(progress, partialTick);
        }

        double angle = Math.toRadians(Mth.lerp(progress, effect.startAngle(), effect.endAngle()));

        return slash.getArcPoint(angle, effect.radius(), partialTick);
    }

    private static void renderQuad(PoseStack poseStack, VertexConsumer consumer, Vec3 inner0, Vec3 outer0, Vec3 outer1, Vec3 inner1, float u0, float u1, int color0, int color1, Vec3 normal) {
        PoseStack.Pose pose = poseStack.last();

        vertex(consumer, pose, inner0, u0, 1, color0, LightTexture.FULL_BRIGHT, normal);
        vertex(consumer, pose, outer0, u0, 0, color0, LightTexture.FULL_BRIGHT, normal);
        vertex(consumer, pose, outer1, u1, 0, color1, LightTexture.FULL_BRIGHT, normal);
        vertex(consumer, pose, inner1, u1, 1, color1, LightTexture.FULL_BRIGHT, normal);
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

    private static int interpolateColors(int[] colors, float progress, float alphaMultiplier) {
        if (colors.length == 0) return 0;
        if (colors.length == 1) return applyAlpha(colors[0], alphaMultiplier);

        progress = Mth.clamp(progress, 0, 1);

        float scaled = progress * (colors.length - 1);
        int index = Math.min((int) scaled, colors.length - 2);
        float localProgress = scaled - index;

        return interpolateColor(
                colors[index],
                colors[index + 1],
                localProgress,
                alphaMultiplier);
    }

    private static int interpolateColor(int from, int to, float progress, float alphaMultiplier) {
        int a = (int) Mth.lerp(progress, from >>> 24 & 0xFF, to >>> 24 & 0xFF);
        int r = (int) Mth.lerp(progress, from >>> 16 & 0xFF, to >>> 16 & 0xFF);
        int g = (int) Mth.lerp(progress, from >>> 8 & 0xFF, to >>> 8 & 0xFF);
        int b = (int) Mth.lerp(progress, from & 0xFF, to & 0xFF);

        a = (int) (a * alphaMultiplier);

        return a << 24 | r << 16 | g << 8 | b;
    }

    private static int applyAlpha(int color, float alphaMultiplier) {
        int a = (int) ((color >>> 24 & 0xFF) * alphaMultiplier);
        return color & 0x00FFFFFF | a << 24;
    }

    private static class ActiveSlash {

        private final SlashEffect effect;

        private final Vec3 origin;
        private final Vec3 forward;
        private final Vec3 hAxis;
        private final Vec3 vAxis;

        private int age;

        private ActiveSlash(Player player, SlashEffect effect) {
            this.effect = effect;

            float yaw = player.getYRot() * Mth.DEG_TO_RAD;
            float pitch = player.getXRot() * Mth.DEG_TO_RAD;

            forward = new Vec3(
                    -Mth.sin(yaw) * Mth.cos(pitch), -Mth.sin(pitch), Mth.cos(yaw) * Mth.cos(pitch))
                    .normalize();

            Vec3 right = new Vec3(Mth.cos(yaw), 0, Mth.sin(yaw)).normalize();
            Vec3 up = forward.cross(right).normalize();

            double rotation = Math.toRadians(effect.rotation());

            Vec3 swingAxis = up.scale(Math.cos(rotation))
                    .add(right.scale(Math.sin(rotation)))
                    .normalize();

            hAxis = forward;
            vAxis = swingAxis;

            origin = player.getEyePosition()
                    .add(right.scale(effect.sway()))
                    .add(up.scale(effect.lift()))
                    .add(effect.xOffset(), effect.yOffset(), effect.zOffset());
        }

        private void tick() {
            age++;
        }

        private boolean isFinished() {
            return age > effect.lifetime();
        }

        private float getProgress(float partialTick) {
            return Mth.clamp((age + partialTick) / effect.lifetime(), 0, 1);
        }

        private Vec3 getCenter(float partialTick) {
            float progress = Mth.clamp((age + partialTick) / effect.lifetime(), 0, 1);
            return origin.add(forward.scale(effect.getDistance(progress)));
        }


        private Vec3 getNormal() {
            return hAxis.cross(vAxis).normalize();
        }

        private Vec3 getArcPoint(double angle, double radius, float partialTick) {
            return getCenter(partialTick)
                    .add(hAxis.scale(Math.cos(angle) * radius))
                    .add(vAxis.scale(Math.sin(angle) * radius));
        }

        private Vec3 getLinePoint(float progress, float partialTick) {
            double offset = Mth.lerp(progress, -effect.length() / 2, effect.length() / 2);
            return getCenter(partialTick).add(vAxis.scale(offset));
        }

        public Vec3 getHAxis() {
            return hAxis;
        }

        public Vec3 getvAxis() {
            return vAxis;
        }
    }
}
