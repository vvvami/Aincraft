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
            .lifetime(3)
            .animation(0f, 0.8f)
            .segments(6)
            .colors(Color.red.getRGB(),Color.black.getRGB())
            .line(15)
            .distances(-3, 120)
            .scaling(false)
            .build();


    @Override
    protected void onTick(ServerPlayer player, int age) {
        Random random = new Random();
        double size = random.nextDouble(-5, 5);
        double lift = random.nextDouble(-2, 2);
        double sway = random.nextDouble(-2, 2);

        SlashSpawner.spawn(player, hCleave.edit()
                        .thicken(size / 18)
                        .rotate(new Random().nextInt(-90, 90))
                        .lift(lift)
                        .sway(sway)
                        .lengthen(size)
                        .build(),
                20f, true);

    }
}
