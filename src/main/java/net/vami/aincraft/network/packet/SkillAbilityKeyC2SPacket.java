package net.vami.aincraft.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.skill.SkillKeyManager;

public record SkillAbilityKeyC2SPacket(int skill) implements CustomPacketPayload {

    public static final Type<SkillAbilityKeyC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            Aincraft.MOD_ID, "skill"));

    public static final StreamCodec<ByteBuf, SkillAbilityKeyC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    SkillAbilityKeyC2SPacket::skill,

                    SkillAbilityKeyC2SPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SkillAbilityKeyC2SPacket message, IPayloadContext context) {

        ServerPlayer player = (ServerPlayer) context.player();
        SkillKeyManager.onAbilityPress(player, message.skill);
    }
}
