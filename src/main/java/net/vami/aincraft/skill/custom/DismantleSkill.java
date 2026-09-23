package net.vami.aincraft.skill.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.vami.aincraft.init.SlashEffects;
import net.vami.aincraft.network.SlashSpawner;
import net.vami.aincraft.skill.Skill;

public class DismantleSkill extends Skill {

    public DismantleSkill() {
        super(70, true);
    }

    @Override
    protected void onTick(ServerPlayer player, int age) {
        switch (age) {
            case  (0) -> {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(
                        10, 20, 20));

                player.connection.send(new ClientboundSetTitleTextPacket(
                        Component.literal("Scale of the Dragon")
                                .withStyle(ChatFormatting.BOLD, ChatFormatting.RED)));
            }

            case (20) -> {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(
                        10, 20, 20));
                player.connection.send(new ClientboundSetTitleTextPacket(
                        Component.literal("Repulsion")
                                .withStyle(ChatFormatting.BOLD, ChatFormatting.RED)));
            }

            case (40) -> {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(
                        10, 20, 20));
                player.connection.send(new ClientboundSetTitleTextPacket(
                        Component.literal("Twin Meteors")
                                .withStyle(ChatFormatting.BOLD, ChatFormatting.RED)));
            }

            default -> {
                if (age < 60) return;

                float progress = (age - 60) / (float) (getLifetime() - 60);
                double distance = Mth.lerp(progress, -10, 150);

                SlashSpawner.spawn(player, SlashEffects.RED_VERTICAL.toBuilder()
                        .thickness(5.5)
                        .radius(30)
                        .lifetime(5)
                        .animation(0.5f, 0.5f)
                        .distance(distance)
                        .build(), 100f);
            }
        }
    }
}
