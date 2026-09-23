package net.vami.aincraft.network.packet;

import io.netty.buffer.ByteBuf;
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
import net.vami.aincraft.render.SkillCameraLock;
import net.vami.aincraft.render.SlashEffect;
import net.vami.aincraft.render.renderer.SlashRenderer;

public record SkillCameraLockS2CPacket(float yaw, float pitch, boolean locked) implements CustomPacketPayload {

    public static final Type<SkillCameraLockS2CPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            Aincraft.MOD_ID, "skill_camera_lock"));

    public static final StreamCodec<ByteBuf, SkillCameraLockS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT,
                    SkillCameraLockS2CPacket::yaw,

                    ByteBufCodecs.FLOAT,
                    SkillCameraLockS2CPacket::pitch,

                    ByteBufCodecs.BOOL,
                    SkillCameraLockS2CPacket::locked,

                    SkillCameraLockS2CPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SkillCameraLockS2CPacket packet, IPayloadContext context) {
        if (packet.locked()) {
            SkillCameraLock.lock(packet.yaw(), packet.pitch());
        } else {
            SkillCameraLock.unlock();
        }
    }
}
