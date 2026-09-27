package net.vami.aincraft.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.vami.aincraft.network.packet.SpawnSlashS2CPacket;
import net.vami.aincraft.render.SlashEffect;
import net.vami.aincraft.render.renderer.SlashRenderer;
import net.vami.aincraft.util.slash.SlashAttack;

public final class SlashSpawner {

    public static SlashAttack spawn(LivingEntity entity, SlashEffect slash, float damage) {
        return spawn(entity, slash, damage, false);
    }

    public static SlashAttack spawn(LivingEntity entity, SlashEffect slash, float damage, boolean breakBlocks) {
        return spawn(entity, slash, damage, breakBlocks, true);
    }

    public static SlashAttack spawn(LivingEntity entity, SlashEffect slash, float damage, boolean breakBlocks, boolean hasSound) {
        if (!(entity.level() instanceof ServerLevel)) {
            throw new IllegalStateException("SlashSpawner.spawn must only be called server-side");
        }
        SpawnSlashS2CPacket packet = new SpawnSlashS2CPacket(entity.getId(), slash);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);

        return SlashAttack.spawn(entity, slash, damage, breakBlocks, hasSound);
    }

    public static void spawnEffect(LivingEntity entity, SlashEffect slash) {
        SpawnSlashS2CPacket packet = new SpawnSlashS2CPacket(entity.getId(), slash);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);
    }

    public static void spawnEffectSelf(LivingEntity entity, SlashEffect slash) {
        SlashRenderer.spawn(entity, slash);
    }
}
