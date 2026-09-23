package net.vami.aincraft.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.vami.aincraft.network.packet.SpawnSlashS2CPacket;
import net.vami.aincraft.render.SlashEffect;
import net.vami.aincraft.render.renderer.SlashRenderer;
import net.vami.aincraft.util.SlashAttack;

public final class SlashSpawner {

    public static void spawn(ServerPlayer player, SlashEffect slash, float damage) {
        spawn(player, slash, damage, false);
    }

    public static void spawn(ServerPlayer player, SlashEffect slash, float damage, boolean breakBlocks) {
        SpawnSlashS2CPacket packet = new SpawnSlashS2CPacket(player.getId(), slash);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, packet);

        SlashAttack.spawn(player, slash, damage, breakBlocks);
    }

    public static void spawnEffect(Player player, SlashEffect slash) {
        SpawnSlashS2CPacket packet = new SpawnSlashS2CPacket(player.getId(), slash);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, packet);
    }

    public static void spawnEffectSelf(Player player, SlashEffect slash) {
        SlashRenderer.spawn(player, slash);
    }
}
