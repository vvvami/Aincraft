package net.vami.aincraft.skill.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.init.ModSounds;
import net.vami.aincraft.init.SlashEffects;
import net.vami.aincraft.network.SlashSpawner;
import net.vami.aincraft.skill.Skill;

import java.awt.*;
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

        if (age < 60) return;

        double length = 15;
        double thickness = 1;

        for (int i = 0; i < 100; i++) {
            int color = new Random().nextInt(1, 10);
            switch (color) {
                case 1 -> color = Color.red.getRGB();
                case 2 -> color = Color.white.getRGB();
                default -> color = Color.black.getRGB();
            }

            SlashSpawner.spawn(player, SlashEffects.RED_VERTICAL.toBuilder()
                    .rotation(SlashEffects.RED_VERTICAL.rotation() + new Random().nextInt(-180, 180))
                    .distance(new Random().nextDouble(-50, 50))
                    .xOffset(new Random().nextDouble(-50, 50))
                    .yOffset(new Random().nextDouble(-4, 25))
                    .zOffset(new Random().nextDouble(-50, 50))
                    .thickness(thickness)
                    .lifetime(6)
                    .line(length)
                    .color(color)
                    .animation(0.5f, 0.7f)
                    .build(), 10f, true);
        }
    }
}
