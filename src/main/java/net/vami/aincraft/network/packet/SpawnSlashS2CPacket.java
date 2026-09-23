package net.vami.aincraft.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.render.SlashEffect;
import net.vami.aincraft.render.renderer.SlashRenderer;

public record SpawnSlashS2CPacket(int entityId, SlashEffect slash) implements CustomPacketPayload {

    public static final Type<SpawnSlashS2CPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            Aincraft.MOD_ID, "spawn_slash"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpawnSlashS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    SpawnSlashS2CPacket::entityId,

                    SlashEffect.STREAM_CODEC,
                    SpawnSlashS2CPacket::slash,

                    SpawnSlashS2CPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SpawnSlashS2CPacket packet, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        Entity entity = minecraft.level.getEntity(packet.entityId());

        if (entity instanceof Player player) {
            SlashRenderer.spawn(player, packet.slash());
        }
    }
}
