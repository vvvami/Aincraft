package net.vami.aincraft.skill.custom;

import net.minecraft.server.level.ServerPlayer;
import net.vami.aincraft.init.SlashEffects;
import net.vami.aincraft.network.SlashSpawner;
import net.vami.aincraft.render.SlashEffect;
import net.vami.aincraft.skill.Skill;

import java.awt.*;
import java.util.Random;

public class DismantleSkill extends Skill {

    public DismantleSkill() {
        super(1, false);
    }

    private static final SlashEffect hCleave = SlashEffects.GREEN_HORIZONTAL.edit()
            .thickness(0.25)
            .lifetime(20)
            .animation(0.3f, 0.8f)
            .segments(6)
            .colors(Color.black.getRGB(), Color.red.getRGB(), Color.white.getRGB())
            .line(40)
            .distances(-2, 120)
            .scaling(false)
            .build();


    @Override
    protected void onTick(ServerPlayer player, int age) {
        double rand = new Random().nextDouble(0, 15);
        SlashSpawner.spawn(player, hCleave.edit()
                        .thicken(rand / 20)
                        .lengthen(rand * 2)
                        .build(),
                10f, true);

    }
}
