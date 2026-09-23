package net.vami.aincraft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.render.SlashEffect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = Aincraft.MOD_ID)
public class SlashAttack {

    private static final ArrayList<SlashAttack> ATTACKS = new ArrayList<>();

    private final ServerPlayer player;
    private final SlashEffect effect;
    private final float damage;
    private final boolean breakBlocks;

    private final Vec3 center;
    private final Vec3 hAxis;
    private final Vec3 vAxis;

    private final HashSet<Integer> hitEntities = new HashSet<>();

    private int age;

    public SlashAttack(ServerPlayer player, SlashEffect effect, float damage, boolean breakBlocks) {
        this.player = player;
        this.effect = effect;
        this.damage = damage;
        this.breakBlocks = breakBlocks;

        float yaw = player.getYRot() * Mth.DEG_TO_RAD;

        Vec3 forward = new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw)).normalize();
        Vec3 right = new Vec3(forward.z, 0, -forward.x);
        Vec3 up = new Vec3(0, 1, 0);

        double rotation = Math.toRadians(effect.rotation());

        Vec3 swingAxis = up.scale(Math.cos(rotation))
                .add(right.scale(Math.sin(rotation)))
                .normalize();

        hAxis = forward;
        vAxis = swingAxis;

        center = player.getEyePosition()
                .add(effect.xOffset(), effect.yOffset(), effect.zOffset())
                .add(forward.scale(effect.distance()));
    }

    public static void spawn(ServerPlayer player, SlashEffect effect, float damage, boolean breakBlocks) {
        ATTACKS.add(new SlashAttack(player, effect, damage, breakBlocks));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<SlashAttack> iterator = ATTACKS.iterator();

        while (iterator.hasNext()) {
            SlashAttack attack = iterator.next();

            attack.tick();

            if (attack.isFinished()) iterator.remove();
        }
    }

    public void tick() {
        if (isFinished()) return;

        float prevProgress = age / (float) effect.lifetime();

        age++;

        float currentProgress = age / (float) effect.lifetime();

        checkHits(prevProgress, currentProgress);
    }

    public boolean isFinished() {
        return age >= effect.lifetime();
    }

    private void checkHits(float prevProgress, float currentProgress) {
        float prevHead = getProgress(prevProgress);
        float currentHead = getProgress(currentProgress);

        if (currentHead <= prevHead) return;

        double startAngle = Math.toRadians(Mth.lerp(prevHead, effect.startAngle(), effect.endAngle()));
        double endAngle = Math.toRadians(Mth.lerp(currentHead, effect.startAngle(), effect.endAngle()));

        double hitRadius = Math.max(effect.thickness(), 0.25);

        double arcLength = Math.abs(endAngle - startAngle) * effect.radius();
        int samples = Math.max(1, (int) Math.ceil(arcLength / (hitRadius * 0.5)));

        for (int i = 0; i <= samples; i++) {
            double t = i / (double) samples;
            double angle = Mth.lerp(t, startAngle, endAngle);

            Vec3 point = getArcPoint(angle, effect.radius());
            hitAt(point, hitRadius);
        }
    }

    private void hitAt(Vec3 point, double radius) {
        AABB bounds = new AABB(
                point.x - radius, point.y - radius, point.z - radius,
                point.x + radius, point.y + radius, point.z + radius);

        List<LivingEntity> entities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                bounds,
                entity -> entity != player && entity.isAlive());

        for (LivingEntity entity : entities) {
            if (!hitEntities.add(entity.getId())) continue;

            entity.invulnerableTime = 0;
            entity.hurt(player.damageSources().playerAttack(player), damage);
        }

        if (breakBlocks) breakBlocksAt(point, radius * 1.5);
    }

    private void breakBlocksAt(Vec3 point, double radius) {
        ServerLevel level = player.serverLevel();

        int minX = Mth.floor(point.x - radius);
        int minY = Mth.floor(point.y - radius);
        int minZ = Mth.floor(point.z - radius);

        int maxX = Mth.floor(point.x + radius);
        int maxY = Mth.floor(point.y + radius);
        int maxZ = Mth.floor(point.z + radius);

        double radiusSqr = radius * radius;

        for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            Vec3 blockCenter = Vec3.atCenterOf(pos);

            if (blockCenter.distanceToSqr(point) > radiusSqr) continue;

            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;

            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2 | 32);
        }
    }

    private Vec3 getArcPoint(double angle, double radius) {
        return center
                .add(hAxis.scale(Math.cos(angle) * radius))
                .add(vAxis.scale(Math.sin(angle) * radius));
    }

    private float getProgress(float progress) {
        float reveal = Mth.clamp(progress / effect.revealTime(), 0, 1);
        return easeOutCubic(reveal);
    }

    private static float easeOutCubic(float value) {
        return 1 - (float) Math.pow(1 - value, 3);
    }
}
