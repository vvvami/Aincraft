package net.vami.aincraft.skill.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.vami.aincraft.init.SlashEffects;
import net.vami.aincraft.network.SlashSpawner;
import net.vami.aincraft.skill.Skill;

import java.awt.*;

public class WorldSlashSkill extends Skill {

    public WorldSlashSkill() {
        super(1, false);
    }

    private static final double[] distances = new double[]
            {-65, -65, -65, -65, -65, -65, -65, -65, -50, -20, 22.5, 60, 120, 200};

    private static final int[] colors = new int[]{
            Color.black.getRGB(),
            Color.red.darker().darker().getRGB(),
            Color.red.darker().getRGB(), Color.red.getRGB(), Color.red.getRGB(), Color.white.getRGB()
    };

    @Override
    protected void onTick(LivingEntity entity, int age) {

        SlashSpawner.spawn(entity, SlashEffects.RED_VERTICAL.edit()
                .thickness(3.5)
                .radius(75)
                .scaling(false)
                .lifetime(150)
                .animation(0.9f, 0.9f)
                .distances(distances)
                .colors(colors)
                .build(), 100f, true);
    }
}
