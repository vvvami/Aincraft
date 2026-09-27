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
import net.vami.aincraft.init.SlashActions;
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

    public static SlashAttack spawn(ServerPlayer player, SlashEffect effect, float damage, boolean breakBlocks, boolean hasSound) {

        SlashAttack attack = new SlashAttack(player, effect, damage, breakBlocks, hasSound);

        ATTACKS.add(attack);

        SlashActions.run(effect.onSpawn(),
                new SlashAction.Context(player, attack, null, null, null, damage, 0));

        return attack;
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
                        attack, null, null, null, attack.damage, 1));
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

        hitEntities(segments);

        if (breakBlocks) {
            breakBlocks(segments);
        }
    }

    public boolean isFinished() {
        return age >= effect.lifetime() || !player.isAlive();
    }

    private void hitEntities(List<SlashSweep.Segment> segments) {
        float progress = age / (float) effect.lifetime();
        float dealtDamage = effect.scaling()
                ? Math.max(damage / 2, damage * (1 - progress))
                : damage;

        for (Entity entity : sweep.findHits(segments, hitEntities)) {
            if (!hitEntities.add(entity.getId())) continue;

            entity.invulnerableTime = 0;

            if (entity.hurt(player.damageSources().playerAttack(player), dealtDamage)) {
                SlashActions.run(
                        effect.onHitEntity(), new SlashAction.Context(
                                player, this, entity, null, null, dealtDamage, progress));
            }
        }
    }

    private void breakBlocks(List<SlashSweep.Segment> segments) {
        ServerLevel level = player.serverLevel();
        float progress = age / (float) effect.lifetime();

        for (SlashSweep.Segment segment : segments) {
            SlashSweep.forEachBlock(segment, pos -> {
                if (!processedBlocks.add(pos.asLong())) return;

                BlockState state = level.getBlockState(pos);
                if (state.isAir() || state.getDestroySpeed(level, pos) < 0) return;

                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2 | 16 | 32);

                if (!effect.onBreakBlock().equals(SlashActions.NONE)) {
                    SlashActions.run(effect.onBreakBlock(), new SlashAction.Context(
                                    player, this, null, pos, state, damage, progress));
                }
            });
        }
    }
}
