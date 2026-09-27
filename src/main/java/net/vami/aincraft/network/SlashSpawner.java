package net.vami.aincraft.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.vami.aincraft.network.packet.SpawnSlashS2CPacket;
import net.vami.aincraft.render.SlashEffect;
import net.vami.aincraft.render.renderer.SlashRenderer;
import net.vami.aincraft.util.slash.SlashAttack;

public final class SlashSpawner {

    public static SlashAttack spawn(ServerPlayer player, SlashEffect slash, float damage) {
        return spawn(player, slash, damage, false);
    }

    public static SlashAttack spawn(ServerPlayer player, SlashEffect slash, float damage, boolean breakBlocks) {
        return spawn(player, slash, damage, breakBlocks, true);
    }

    public static SlashAttack spawn(ServerPlayer player, SlashEffect slash, float damage, boolean breakBlocks, boolean hasSound) {
        SpawnSlashS2CPacket packet = new SpawnSlashS2CPacket(player.getId(), slash);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, packet);

        return SlashAttack.spawn(player, slash, damage, breakBlocks, hasSound);
    }

    public static void spawnEffect(Player player, SlashEffect slash) {
        SpawnSlashS2CPacket packet = new SpawnSlashS2CPacket(player.getId(), slash);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, packet);
    }

    public static void spawnEffectSelf(Player player, SlashEffect slash) {
        SlashRenderer.spawn(player, slash);
    }
}
