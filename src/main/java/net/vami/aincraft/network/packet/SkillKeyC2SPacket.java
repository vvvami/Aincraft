package net.vami.aincraft.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.skill.SkillKeyManager;

public record SkillKeyC2SPacket(int pressType, int pressedMs) implements CustomPacketPayload {

    public static final Type<SkillKeyC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            Aincraft.MOD_ID, "skill_key"));

    public static final StreamCodec<ByteBuf, SkillKeyC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    SkillKeyC2SPacket::pressType,

                    ByteBufCodecs.INT,
                    SkillKeyC2SPacket::pressedMs,

                    SkillKeyC2SPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SkillKeyC2SPacket message, IPayloadContext context) {
        pressAction(context.player(), message.pressType, message.pressedMs);
    }

    public static void pressAction(Player player, int type, int pressedMs) {
        if (type == 1) {
            SkillKeyManager.onPress(player, pressedMs);
        }
    }
}
