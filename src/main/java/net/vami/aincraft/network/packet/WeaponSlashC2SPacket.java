package net.vami.aincraft.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.TieredItem;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.network.SlashSpawner;
import net.vami.aincraft.render.SlashEffect;
import net.vami.aincraft.util.SlashUtil;

public record WeaponSlashC2SPacket(int rotation) implements CustomPacketPayload {

    public static final Type<WeaponSlashC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            Aincraft.MOD_ID, "weapon_slash"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponSlashC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    WeaponSlashC2SPacket::rotation,

                    WeaponSlashC2SPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WeaponSlashC2SPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        if (player.getAttackStrengthScale(0.5F) <= 0.9F) return;
        if (!(player.getMainHandItem().getItem() instanceof TieredItem)) return;

        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null) return;

        int rotation = packet.rotation();
        double[] angles;

        if (rotation < 0) {
            angles = new double[]{135, -80};
        } else {
            angles = new double[]{-135, 80};
        }

        SlashEffect slash = SlashUtil.getWeaponSlash(player).edit()
                .rotate(rotation)
                .angles(angles[0], angles[1])
                .build();

        SlashSpawner.spawn(player, slash, (float) attackDamage.getValue());
    }
}
