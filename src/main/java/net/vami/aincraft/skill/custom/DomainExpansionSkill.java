package net.vami.aincraft.skill.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.init.SlashEffects;
import net.vami.aincraft.network.SlashSpawner;
import net.vami.aincraft.skill.Skill;

import java.util.Random;

public class DomainExpansionSkill extends Skill {

    public DomainExpansionSkill() {
        super(240, false);
    }

    @Override
    protected void onTick(ServerPlayer player, int age) {
        if (age == 1) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(
                    10, 100, 20));

            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.literal("DOMAIN EXPANSION")
                            .withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED)));
        }

        if (age == 30) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(
                    10, 100, 20));
            player.connection.send(new ClientboundSetSubtitleTextPacket(
                    Component.literal("Malevolent Shrine")
                            .withStyle(ChatFormatting.RED)));
        }

        if (age < 40) return;

        for (int i = 0; i < 300; i++) {
            SlashSpawner.spawn(player, SlashEffects.RED_VERTICAL.toBuilder()
                    .rotation(SlashEffects.RED_VERTICAL.rotation() + new Random().nextInt(-180, 180))
                    .radius(2 + new Random().nextFloat(1, 2))
                    .angles(SlashEffects.RED_VERTICAL.startAngle() +
                                    new Random().nextInt(-45, 45),
                            SlashEffects.RED_VERTICAL.endAngle() +
                                    new Random().nextInt(-45, 45))
                    .distance(new Random().nextDouble(-100, 100))
                    .xOffset(new Random().nextDouble(-100, 100))
                    .yOffset(new Random().nextDouble(-4, 25))
                    .zOffset(new Random().nextDouble(-100, 100))
                    .thickness(1)
                    .lifetime(5)
                    .animation(0.5f, 0.5f)
                    .build(), 10f);
        }
    }
}
