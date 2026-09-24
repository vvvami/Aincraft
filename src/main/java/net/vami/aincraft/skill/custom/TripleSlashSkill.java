package net.vami.aincraft.skill.custom;

import net.minecraft.server.level.ServerPlayer;
import net.vami.aincraft.init.SlashEffects;
import net.vami.aincraft.network.SlashSpawner;
import net.vami.aincraft.skill.Skill;

import java.awt.*;

public class TripleSlashSkill extends Skill {

    public TripleSlashSkill() {
        super(14, false);
    }

    @Override
    protected void onTick(ServerPlayer player, int age) {
        switch (age) {
            case 0 -> SlashSpawner.spawn(player, SlashEffects.RED_VERTICAL.toBuilder()
                    .rotation(-45)
                    .furthen(1)
                    .colors(Color.magenta.getRGB(), Color.red.darker().darker().getRGB())
                    .build(), 5f, false);

            case 4 -> SlashSpawner.spawn(player, SlashEffects.RED_VERTICAL.toBuilder()
                    .rotation(45)
                    .colors(Color.magenta.getRGB(), Color.red.darker().darker().getRGB())
                    .build(), 5f, false);

            case 8 -> SlashSpawner.spawn(player, SlashEffects.RED_VERTICAL.toBuilder()
                    .rotation(90)
                    .radius(2)
                    .furthen(0.5)
                    .thickness(0.8)
                    .angles(180, -180)
                    .colors(Color.magenta.getRGB(), Color.red.darker().darker().getRGB())
                    .build(), 8f, false);
        }
    }
}
