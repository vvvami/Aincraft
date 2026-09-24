package net.vami.aincraft.skill.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.vami.aincraft.init.SlashEffects;
import net.vami.aincraft.network.SlashSpawner;
import net.vami.aincraft.skill.Skill;

public class WorldSlashSkill extends Skill {

    public WorldSlashSkill() {
        super(61, true);
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

                player.connection.send(new ClientboundSetTitlesAnimationPacket(
                        10, 20, 20));
                player.connection.send(new ClientboundSetTitleTextPacket(
                        Component.literal("DISMANTLE!")
                                .withStyle(ChatFormatting.BOLD, ChatFormatting.RED)));

                SlashSpawner.spawn(player, SlashEffects.RED_VERTICAL.toBuilder()
                        .thickness(5.5)
                        .radius(30)
                        .lifetime(40)
                        .animation(0.2f, 0.8f)
                        .distance(-25, 150)
                        .build(), 100f, true);
            }
        }
    }
}
