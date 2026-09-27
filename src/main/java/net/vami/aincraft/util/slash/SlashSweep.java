package net.vami.aincraft.util.slash;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.vami.aincraft.render.SlashEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class SlashSweep {

    public record Segment(Vec3 from, Vec3 to, double radius) {}

    private final Player player;
    private final SlashEffect effect;
    private final Vec3 origin;
    private final Vec3 forward;
    private final Vec3 vAxis;

    public SlashSweep(Player player, SlashEffect effect) {
        this.player = player;
        this.effect = effect;

        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        float pitch = player.getXRot() * Mth.DEG_TO_RAD;

        forward = new Vec3(
                -Mth.sin(yaw) * Mth.cos(pitch),
                -Mth.sin(pitch),
                Mth.cos(yaw) * Mth.cos(pitch))
                .normalize();

        Vec3 right = new Vec3(Mth.cos(yaw), 0, Mth.sin(yaw)).normalize();
        Vec3 up = forward.cross(right).normalize();

        double rotation = Math.toRadians(effect.rotation());
        double tilt = Math.toRadians(effect.tilt());

        Vec3 swingAxis = up.scale(Math.cos(rotation))
                .add(right.scale(Math.sin(rotation)))
                .normalize();

        vAxis = effect.shape() == SlashEffect.Shape.LINE
                ? swingAxis.scale(Math.cos(tilt))
                .add(forward.scale(Math.sin(tilt)))
                .normalize()
                : swingAxis;

        origin = player.getEyePosition()
                .add(right.scale(effect.sway()))
                .add(up.scale(effect.lift()))
                .add(effect.xOffset(), effect.yOffset(), effect.zOffset());
    }

    public Vec3 center(float progress) {
        return origin.add(forward.scale(effect.getDistance(progress)));
    }


    public List<Segment> getSegments(float prevProgress, float currentProgress) {
        List<Segment> segments = new ArrayList<>();

        float prevHead = getProgress(prevProgress);
        float currentHead = getProgress(currentProgress);

        switch (effect.shape()) {
            case ARC -> {
                if (effect.revealTime() <= 0) {
                    if (effect.isMoving()) {
                        calcMovingArc(segments, prevProgress, currentProgress);
                    } else if (prevProgress == 0) {
                        calcArc(
                                segments,
                                1,
                                currentProgress,
                                Math.max(effect.thickness(), 0.25)
                        );
                    }
                } else if (currentHead > prevHead) {
                    calcSweptArc(segments, prevHead, currentHead, prevProgress, currentProgress);
                } else if (effect.isMoving()) {
                    calcMovingArc(segments, prevProgress, currentProgress);
                }
            }
            case LINE -> {
                if (effect.revealTime() <= 0) {
                    if (effect.isMoving()) {
                        collectMovingLine(segments, prevProgress, currentProgress);
                    } else if (prevProgress == 0) {
                        collectFullLine(segments, currentProgress);
                    }
                } else if (currentHead > prevHead) {
                    collectSweptLine(segments, prevHead, currentHead, prevProgress, currentProgress);
                } else if (effect.isMoving()) {
                    collectMovingLine(segments, prevProgress, currentProgress);
                }
            }
        }

        return segments;
    }


    public List<Segment> getFullSweep() {
        List<Segment> segments = new ArrayList<>();
        int lifetime = effect.lifetime();

        for (int age = 0; age < lifetime; age++) {
            segments.addAll(getSegments(
                    age / (float) lifetime,
                    (age + 1) / (float) lifetime));
        }

        return segments;
    }

    private void calcSweptArc(List<Segment> segments, float prevHead, float currentHead, float prevProgress, float currentProgress) {
        double hitRadius = Math.max(effect.thickness(), 0.25);

        Vec3 previousHeadPoint = getArcPoint(
                Math.toRadians(Mth.lerp(prevHead, effect.startAngle(), effect.endAngle())),
                effect.radius(),
                prevProgress);

        Vec3 currentHeadPoint = getArcPoint(
                Math.toRadians(Mth.lerp(currentHead, effect.startAngle(), effect.endAngle())),
                effect.radius(),
                currentProgress);

        double maxTravel = Math.max(
                center(prevProgress).distanceTo(center(currentProgress)),
                previousHeadPoint.distanceTo(currentHeadPoint));

        int timeSamples = Math.max(1, (int) Math.ceil(maxTravel / (hitRadius * 0.75)));

        for (int i = 0; i <= timeSamples; i++) {
            float t = i / (float) timeSamples;

            calcArc(segments,
                    Mth.lerp(t, prevHead, currentHead),
                    Mth.lerp(t, prevProgress, currentProgress),
                    hitRadius);
        }
    }

    private void calcArc(List<Segment> segments, float headProgress, float moveProgress, double radius) {
        double startAngle = Math.toRadians(effect.startAngle());
        double headAngle = Math.toRadians(Mth.lerp(headProgress, effect.startAngle(), effect.endAngle()));
        int samples = Math.max(1, (int) Math.ceil(Math.abs(headAngle - startAngle) * effect.radius() / radius));

        Vec3 previous = null;

        for (int i = 0; i <= samples; i++) {
            double angle = Mth.lerp(i / (double) samples, startAngle, headAngle);
            Vec3 point = getArcPoint(angle, effect.radius(), moveProgress);

            segments.add(previous == null ? new Segment(point, point, radius)
                    : new Segment(previous, point, radius));

            previous = point;
        }
    }

    private void calcMovingArc(List<Segment> segments, float prevProgress, float currentProgress) {
        double startAngle = Math.toRadians(effect.startAngle());
        double endAngle = Math.toRadians(effect.endAngle());
        double hitRadius = Math.max(effect.thickness(), 0.25);

        int samples = Math.max(1,
                (int) Math.ceil(Math.abs(endAngle - startAngle) * effect.radius() / hitRadius));

        for (int i = 0; i <= samples; i++) {
            double angle = Mth.lerp(i / (double) samples, startAngle, endAngle);

            segments.add(new Segment(
                    getArcPoint(angle, effect.radius(), prevProgress),
                    getArcPoint(angle, effect.radius(), currentProgress),
                    hitRadius));
        }
    }

    private void collectFullLine(List<Segment> segments, float progress) {
        segments.add(new Segment(
                getLinePoint(0, progress),
                getLinePoint(1, progress),
                Math.max(effect.thickness(), 0.25)));
    }

    private void collectSweptLine(List<Segment> segments, float prevHead, float currentHead, float prevProgress, float currentProgress) {
        double hitRadius = Math.max(effect.thickness(), 0.25);

        double maxTravel = Math.max(
                getLinePoint(0, prevProgress).distanceTo(getLinePoint(0, currentProgress)),
                getLinePoint(prevHead, prevProgress).distanceTo(getLinePoint(currentHead, currentProgress)));

        int samples = Math.max(1, (int) Math.ceil(maxTravel / (hitRadius * 0.75)));

        for (int i = 0; i <= samples; i++) {
            float t = i / (float) samples;
            float moveProgress = Mth.lerp(t, prevProgress, currentProgress);

            segments.add(new Segment(
                    getLinePoint(0, moveProgress),
                    getLinePoint(Mth.lerp(t, prevHead, currentHead), moveProgress),
                    hitRadius));
        }
    }

    private void collectMovingLine(List<Segment> segments, float prevProgress, float currentProgress) {
        double hitRadius = Math.max(effect.thickness(), 0.25);
        int samples = Math.max(1, (int) Math.ceil(Math.abs(effect.length()) / hitRadius));

        for (int i = 0; i <= samples; i++) {
            float lineProgress = i / (float) samples;

            segments.add(new Segment(
                    getLinePoint(lineProgress, prevProgress),
                    getLinePoint(lineProgress, currentProgress),
                    hitRadius));
        }
    }

    private Vec3 getArcPoint(double angle, double radius, float progress) {
        return center(progress)
                .add(forward.scale(Math.cos(angle) * radius))
                .add(vAxis.scale(Math.sin(angle) * radius));
    }

    private Vec3 getLinePoint(float lineProgress, float moveProgress) {
        return center(moveProgress).add(vAxis.scale(Mth.lerp(
                lineProgress,
                -effect.length() / 2,
                effect.length() / 2)));
    }

    private float getProgress(float progress) {
        if (effect.revealTime() <= 0) return progress <= 0 ? 0 : 1;

        return easeOutCubic(Mth.clamp(progress / effect.revealTime(), 0, 1));
    }

    private static float easeOutCubic(float value) {
        return 1 - (float) Math.pow(1 - value, 3);
    }


    public List<Entity> findHits(List<Segment> segments, Set<Integer> excluded) {
        if (segments.isEmpty()) return List.of();

        List<Entity> hits = new ArrayList<>();

        outer:
        for (Entity entity : player.level().getEntitiesOfClass(
                Entity.class,
                getBounds(segments),
                entity -> entity != player
                        && !(entity instanceof ItemEntity)
                        && entity.isAlive()
                        && !excluded.contains(entity.getId()))) {

            for (Segment segment : segments) {
                if (intersects(entity.getBoundingBox(), segment)) {
                    hits.add(entity);
                    continue outer;
                }
            }
        }

        return hits;
    }

    public static boolean wouldHit(Player player, SlashEffect effect) {
        List<Segment> segments = new SlashSweep(player, effect).getFullSweep();
        if (segments.isEmpty()) return false;

        for (LivingEntity entity : player.level().getEntitiesOfClass(
                LivingEntity.class,
                getBounds(segments),
                entity -> entity != player && entity.isAlive())) {

            AABB bounds = entity.getBoundingBox();

            for (Segment segment : segments) {
                if (intersects(bounds, segment)) return true;
            }
        }

        return false;
    }

    private static boolean intersects(AABB entityBounds, Segment segment) {
        double distance = segment.from().distanceTo(segment.to());

        if (distance == 0) {
            return sampleHits(entityBounds, segment.from(), segment.radius());
        }

        int samples = Math.max(1, (int) Math.ceil(distance / segment.radius()));

        for (int i = 0; i <= samples; i++) {
            if (sampleHits(
                    entityBounds,
                    segment.from().lerp(segment.to(), i / (double) samples),
                    segment.radius())) {
                return true;
            }
        }

        return false;
    }

    private static boolean sampleHits(AABB box, Vec3 point, double radius) {
        return point.x + radius > box.minX
                && point.x - radius < box.maxX
                && point.y + radius > box.minY
                && point.y - radius < box.maxY
                && point.z + radius > box.minZ
                && point.z - radius < box.maxZ;
    }

    private static AABB getBounds(List<Segment> segments) {
        AABB bounds = segmentBounds(segments.getFirst());

        for (int i = 1; i < segments.size(); i++) {
            AABB next = segmentBounds(segments.get(i));

            bounds = new AABB(
                    Math.min(bounds.minX, next.minX),
                    Math.min(bounds.minY, next.minY),
                    Math.min(bounds.minZ, next.minZ),
                    Math.max(bounds.maxX, next.maxX),
                    Math.max(bounds.maxY, next.maxY),
                    Math.max(bounds.maxZ, next.maxZ));
        }

        return bounds;
    }

    private static AABB segmentBounds(Segment segment) {
        double r = segment.radius();

        return new AABB(
                Math.min(segment.from().x, segment.to().x) - r,
                Math.min(segment.from().y, segment.to().y) - r,
                Math.min(segment.from().z, segment.to().z) - r,
                Math.max(segment.from().x, segment.to().x) + r,
                Math.max(segment.from().y, segment.to().y) + r,
                Math.max(segment.from().z, segment.to().z) + r);
    }

    public static void forEachBlock(Segment segment, Consumer<BlockPos> consumer) {
        Vec3 from = segment.from();
        Vec3 to = segment.to();
        double radius = segment.radius() * 1.5;

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
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double t = lengthSqr == 0 ? 0 :
                            ((x + 0.5 - from.x) * dx
                             + (y + 0.5 - from.y) * dy
                             + (z + 0.5 - from.z) * dz) / lengthSqr;

                    t = Mth.clamp(t, 0, 1);

                    double cx = from.x + dx * t;
                    double cy = from.y + dy * t;
                    double cz = from.z + dz * t;

                    double ox = x + 0.5 - cx;
                    double oy = y + 0.5 - cy;
                    double oz = z + 0.5 - cz;

                    if (ox * ox + oy * oy + oz * oz <= radiusSqr) {
                        pos.set(x, y, z);
                        consumer.accept(pos.immutable());
                    }
                }
            }
        }
    }
}
