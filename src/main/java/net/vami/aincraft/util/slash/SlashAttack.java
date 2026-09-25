package net.vami.aincraft.util.slash;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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

    private final SlashSweep sweep;

    private final float damage;
    private final boolean breakBlocks;

    private final HashSet<Integer> hitEntities = new HashSet<>();

    private int age;

    public SlashAttack(ServerPlayer player, SlashEffect effect, float damage, boolean breakBlocks, boolean hasSound) {
        this.player = player;
        this.effect = effect;
        this.damage = damage;
        this.breakBlocks = breakBlocks;
        this.sweep = new SlashSweep(player, effect);

        if (!hasSound) return;

        Vec3 pos = sweep.center(0);

        player.level().playSound(null,
                pos.x, pos.y, pos.z,
                ModSounds.SLASH.get(), SoundSource.PLAYERS,
                1.0F, new Random().nextFloat(0.5F, 2.0F));
    }

    public static void spawn(ServerPlayer player, SlashEffect effect, float damage, boolean breakBlocks, boolean hasSound) {
        SlashAttack attack = new SlashAttack(player, effect, damage, breakBlocks, hasSound);

        ATTACKS.add(attack);
        SlashActions.run(effect.onExpire(),
                new SlashAction.Context(player,
                        attack, null, null, null, damage, 0));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<SlashAttack> iterator = ATTACKS.iterator();

        while (iterator.hasNext()) {
            SlashAttack attack = iterator.next();

            attack.tick();

            if (attack.isFinished())  {
                SlashActions.run(attack.effect.onExpire(),
                        new SlashAction.Context(attack.player,
                        attack, null, null, null, attack.damage, attack.effect.lifetime()));
                iterator.remove();
            }
        }
    }

    public void tick() {
        if (isFinished()) return;

        float prevProgress = age / (float) effect.lifetime();
        age++;
        float currentProgress = age / (float) effect.lifetime();

        List<SlashSweep.Segment> segments = sweep.getSegments(prevProgress, currentProgress);
        checkEntities(segments);

        if (breakBlocks) {
            for (SlashSweep.Segment segment : segments) {
                breakBlocks(segment.from(), segment.to(), segment.radius() * 1.5);
            }
        }
    }

    public boolean isFinished() {
        return age > effect.lifetime() || !player.isAlive();
    }

    private void checkEntities(List<SlashSweep.Segment> segments) {
        List<Entity> entities = SlashSweep.findHits(player, segments, hitEntities);

        for (Entity entity : entities) {
            if (!hitEntities.add(entity.getId()))
                continue;

            entity.invulnerableTime = 0;

            float progress = age / (float) effect.lifetime();
            float distDamage = effect.scaling() ? Math.max(damage / 2, damage - (damage * progress)) : damage;

            if (entity.hurt(player.damageSources().playerAttack(player), distDamage)) {
                SlashActions.run(
                        effect.onHitEntity(),
                        new SlashAction.Context(player, this, entity, null, null, damage, progress));
            }
        }
    }

    private void breakBlocks(Vec3 from, Vec3 to, double radius) {
        ServerLevel level = player.serverLevel();

        float progress = (float) this.age / this.effect.lifetime();

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

                    double t = lengthSqr == 0 ? 0 : ((bx - from.x) * dx + (by - from.y) * dy + (bz - from.z) * dz) / lengthSqr;

                    t = Mth.clamp(t, 0.0, 1.0);

                    double closestX = from.x + dx * t;
                    double closestY = from.y + dy * t;
                    double closestZ = from.z + dz * t;

                    double distanceX = bx - closestX;
                    double distanceY = by - closestY;
                    double distanceZ = bz - closestZ;

                    if (distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ > radiusSqr) continue;

                    long packedPos = BlockPos.asLong(x, y, z);

                    pos.set(x, y, z);

                    BlockState state = level.getBlockState(pos);

                    if (state.isAir() || state.getDestroySpeed(level, pos) < 0) continue;

                    if (!processedBlocks.add(packedPos)) continue;

                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | 16 | 32);

                    if (!effect.onBreakBlock().equals(SlashActions.NONE)) {
                        SlashActions.run(effect.onBreakBlock(),
                                new SlashAction.Context(player,
                                        this, null, pos.immutable(), state, damage, progress));
                    }
                }
            }
        }
    }
}
