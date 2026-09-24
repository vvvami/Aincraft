package net.vami.aincraft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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
import net.vami.aincraft.init.ModSounds;
import net.vami.aincraft.render.SlashEffect;

import java.util.*;

@EventBusSubscriber(modid = Aincraft.MOD_ID)
public class SlashAttack {

    private static final ArrayList<SlashAttack> ATTACKS = new ArrayList<>();

    private final HashSet<Long> processedBlocks = new HashSet<>();

    private final ServerPlayer player;
    private final SlashEffect effect;
    private final float damage;
    private final boolean breakBlocks;

    private final Vec3 origin;
    private final Vec3 forward;
    private final Vec3 hAxis;
    private final Vec3 vAxis;

    private final HashSet<Integer> hitEntities = new HashSet<>();

    private int age;

    public SlashAttack(ServerPlayer player, SlashEffect effect, float damage, boolean breakBlocks, boolean hasSound) {
        this.player = player;
        this.effect = effect;
        this.damage = damage;
        this.breakBlocks = breakBlocks;

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

        if (!hasSound) return;

        Vec3 pos = getCenter();
        player.level().playSound(null,
                pos.x, pos.y, pos.z,
                ModSounds.SLASH.get(), SoundSource.PLAYERS,
                1.0f, new Random().nextFloat(0.5f, 2));
    }

    public static void spawn(ServerPlayer player, SlashEffect effect, float damage, boolean breakBlocks, boolean hasSound) {
        ATTACKS.add(new SlashAttack(player, effect, damage, breakBlocks, hasSound));
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
        return age >= effect.lifetime() || !player.isAlive();
    }

    private void checkHits(float prevProgress, float currentProgress) {
        float prevHead = getProgress(prevProgress);
        float currentHead = getProgress(currentProgress);

        switch (effect.shape()) {
            case ARC -> {
                if (currentHead > prevHead) {
                    checkSweptArc(prevHead, currentHead, prevProgress, currentProgress);
                    return;
                }

                if (effect.distance() != effect.endDistance()) {
                    checkMovingArc(prevProgress, currentProgress);
                }
            }

            case LINE -> {
                if (currentHead > prevHead) {
                    checkSweptLine(prevHead, currentHead, prevProgress, currentProgress);
                    return;
                }

                if (effect.distance() != effect.endDistance()) {
                    checkMovingLine(prevProgress, currentProgress);
                }
            }
        }
    }

    private void checkSweptArc(float prevHead, float currentHead, float prevProgress, float currentProgress) {
        double hitRadius = Math.max(effect.thickness(), 0.25);

        double previousHeadAngle = Math.toRadians(Mth.lerp(prevHead, effect.startAngle(), effect.endAngle()));

        double currentHeadAngle = Math.toRadians(Mth.lerp(currentHead, effect.startAngle(), effect.endAngle()));

        Vec3 previousHeadPoint = getArcPoint(previousHeadAngle, effect.radius(), prevProgress);
        Vec3 currentHeadPoint = getArcPoint(currentHeadAngle, effect.radius(), currentProgress);

        double centerTravel = getCenter(prevProgress).distanceTo(getCenter(currentProgress));
        double headTravel = previousHeadPoint.distanceTo(currentHeadPoint);

        double maxTravel = Math.max(centerTravel, headTravel);
        double spacing = hitRadius * 0.75;
        int timeSamples = Math.max(1, (int) Math.ceil(maxTravel / spacing));

        for (int i = 0; i <= timeSamples; i++) {
            float t = i / (float) timeSamples;

            float moveProgress = Mth.lerp(t, prevProgress, currentProgress);
            float headProgress = Mth.lerp(t, prevHead, currentHead);

            checkArcPoint(headProgress, moveProgress, hitRadius);
        }
    }

    private void checkArcPoint(float headProgress, float moveProgress, double hitRadius) {
        double startAngle = Math.toRadians(effect.startAngle());

        double headAngle = Math.toRadians(Mth.lerp(headProgress, effect.startAngle(), effect.endAngle()));

        double arcLength = Math.abs(headAngle - startAngle) * effect.radius();

        int samples = Math.max(1, (int) Math.ceil(arcLength / hitRadius));

        Vec3 previousPoint = null;

        for (int i = 0; i <= samples; i++) {
            double t = i / (double) samples;

            double angle = Mth.lerp(t, startAngle, headAngle);

            Vec3 point = getArcPoint(angle, effect.radius(), moveProgress);

            if (previousPoint == null) {
                hitAt(point, hitRadius);

                if (breakBlocks) {
                    breakBlocksAlongLine(point, point, hitRadius * 1.5);
                }
            } else {
                checkLine(previousPoint, point, hitRadius);
            }

            previousPoint = point;
        }
    }

    private void checkMovingArc(float prevProgress, float currentProgress) {
        double startAngle = Math.toRadians(effect.startAngle());
        double endAngle = Math.toRadians(effect.endAngle());

        double hitRadius = Math.max(effect.thickness(), 0.25);

        double arcLength = Math.abs(endAngle - startAngle) * effect.radius();
        int arcSamples = Math.max(1, (int) Math.ceil(arcLength / hitRadius));

        for (int i = 0; i <= arcSamples; i++) {
            double t = i / (double) arcSamples;
            double angle = Mth.lerp(t, startAngle, endAngle);

            Vec3 prevPoint = getArcPoint(angle, effect.radius(), prevProgress);
            Vec3 currentPoint = getArcPoint(angle, effect.radius(), currentProgress);

            checkLine(prevPoint, currentPoint, hitRadius);
        }
    }

    private void checkSweptLine(float prevHead, float currentHead, float prevProgress, float currentProgress) {
        double hitRadius = Math.max(effect.thickness() / 2, 0.25);

        Vec3 prevStart = getLinePoint(0, prevProgress);
        Vec3 currentStart = getLinePoint(0, currentProgress);

        Vec3 prevHeadPoint = getLinePoint(prevHead, prevProgress);
        Vec3 currentHeadPoint = getLinePoint(currentHead, currentProgress);

        double maxTravel = Math.max(prevStart.distanceTo(currentStart), prevHeadPoint.distanceTo(currentHeadPoint));

        double sampleSpacing = hitRadius * 0.75;

        int samples = Math.max(1, (int) Math.ceil(maxTravel / sampleSpacing));

        for (int i = 0; i <= samples; i++) {
            float t = i / (float) samples;

            float moveProgress = Mth.lerp(t, prevProgress, currentProgress);
            float headProgress = Mth.lerp(t, prevHead, currentHead);

            Vec3 start = getLinePoint(0, moveProgress);
            Vec3 head = getLinePoint(headProgress, moveProgress);

            checkLine(start, head, hitRadius);
        }
    }

    private void checkMovingLine(float prevProgress, float currentProgress) {
        double hitRadius = Math.max(effect.thickness(), 0.25);

        int samples = Math.max(1, (int) Math.ceil(Math.abs(effect.length()) / hitRadius));

        for (int i = 0; i <= samples; i++) {
            float lineProgress = i / (float) samples;

            Vec3 prevPoint = getLinePoint(lineProgress, prevProgress);
            Vec3 currentPoint = getLinePoint(lineProgress, currentProgress);

            checkLine(prevPoint, currentPoint, hitRadius);
        }
    }

    private void checkLine(Vec3 from, Vec3 to, double radius) {
        if (breakBlocks) breakBlocksAlongLine(from, to, radius * 1.5);

        double distance = from.distanceTo(to);

        int samples = Math.max(1, (int) Math.ceil(distance / radius));

        for (int i = 0; i <= samples; i++) {
            double t = i / (double) samples;
            hitAt(from.lerp(to, t), radius);
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
    }

    private void breakBlocksAlongLine(Vec3 from, Vec3 to, double radius) {
        ServerLevel level = player.serverLevel();

        int minX = Mth.floor(Math.min(from.x, to.x) - radius);
        int minY = Mth.floor(Math.min(from.y, to.y) - radius);
        int minZ = Mth.floor(Math.min(from.z, to.z) - radius);

        int maxX = Mth.floor(Math.max(from.x, to.x) + radius);
        int maxY = Mth.floor(Math.max(from.y, to.y) + radius);
        int maxZ = Mth.floor(Math.max(from.z, to.z) + radius);

        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double lengthSqr = dx * dx + dy * dy + dz * dz;
        double radiusSqr = radius * radius;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            double bx = x + 0.5;

            for (int y = minY; y <= maxY; y++) {
                double by = y + 0.5;

                for (int z = minZ; z <= maxZ; z++) {
                    double bz = z + 0.5;

                    double t = lengthSqr == 0
                            ? 0
                            : ((bx - from.x) * dx + (by - from.y) * dy + (bz - from.z) * dz) / lengthSqr;

                    t = Mth.clamp(t, 0.0, 1.0);

                    double closestX = from.x + dx * t;
                    double closestY = from.y + dy * t;
                    double closestZ = from.z + dz * t;

                    double distanceX = bx - closestX;
                    double distanceY = by - closestY;
                    double distanceZ = bz - closestZ;

                    if (distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ > radiusSqr) continue;

                    long packedPos = BlockPos.asLong(x, y, z);
                    if (!processedBlocks.add(packedPos)) continue;

                    pos.set(x, y, z);

                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;

                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2 | 32);
                }
            }
        }
    }

    private Vec3 getCenter() {
        return getCenter((float) age / effect.lifetime());
    }

    private Vec3 getCenter(float progress) {
        double distance = Mth.lerp(progress, effect.distance(), effect.endDistance());
        return origin.add(forward.scale(distance));
    }

    private Vec3 getArcPoint(double angle, double radius, float progress) {
        return getCenter(progress)
                .add(hAxis.scale(Math.cos(angle) * radius))
                .add(vAxis.scale(Math.sin(angle) * radius));
    }

    private Vec3 getLinePoint(float lineProgress, float moveProgress) {
        double offset = Mth.lerp(lineProgress, -effect.length() / 2.0, effect.length() / 2.0);
        return getCenter(moveProgress).add(vAxis.scale(offset));
    }

    private float getProgress(float progress) {
        float reveal = Mth.clamp(progress / effect.revealTime(), 0, 1);
        return easeOutCubic(reveal);
    }

    private static float easeOutCubic(float value) {
        return 1 - (float) Math.pow(1 - value, 3);
    }
}
