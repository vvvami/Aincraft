package net.vami.aincraft.skill.custom;

import net.minecraft.server.level.ServerPlayer;
import net.vami.aincraft.init.SlashEffects;
import net.vami.aincraft.network.SlashSpawner;
import net.vami.aincraft.skill.Skill;

import java.awt.*;

public class WindCutterSkill extends Skill {

    public WindCutterSkill() {
        super(1, false);
    }

    @Override
    protected void onTick(ServerPlayer player, int age) {
        SlashSpawner.spawn(player, SlashEffects.GREEN_HORIZONTAL.toBuilder()
                .thickness(0.4)
                .lifetime(10)
                .animation(0.3f, 0.8f)
                .segments(3)
                .distances(1.4, 10)
                .inflate(1)
                .splash(Color.yellow.getRGB())
                .build(), 4f, true);
    }
}
