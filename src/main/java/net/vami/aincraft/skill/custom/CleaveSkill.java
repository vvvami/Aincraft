package net.vami.aincraft.skill.custom;

import net.minecraft.server.level.ServerPlayer;
import net.vami.aincraft.init.SlashEffects;
import net.vami.aincraft.network.SlashSpawner;
import net.vami.aincraft.render.SlashEffect;
import net.vami.aincraft.skill.Skill;

import java.awt.*;
import java.util.Random;

public class CleaveSkill extends Skill {

    public CleaveSkill() {
        super(1, false);
    }

    private static final SlashEffect hCleave = SlashEffects.GREEN_HORIZONTAL.toBuilder()
            .thickness(0.45)
            .lifetime(10)
            .animation(0.3f, 0.8f)
            .segments(6)
            .colors(Color.black.getRGB(), Color.red.getRGB(), Color.white.getRGB())
            .line(30)
            .distances(-2, 60)
            .scaling(false)
            .build();

    private static final SlashEffect vCleave = hCleave.toBuilder().rotation(0).build();

    @Override
    protected void onTick(ServerPlayer player, int age) {
        Random random = new Random();

        int lines = random.nextInt(3, 9);
        if (lines % 2 != 0) lines++;

        float spacing = random.nextFloat(1, 7);

        float start = -(lines - 1) * spacing / 2f;
        float length = (lines + 1) * spacing;

        for (int i = 0; i < lines; i++) {
            float offset = start + i * spacing;

            SlashSpawner.spawn(player, hCleave.toBuilder()
                            .lift(offset)
                            .length(length)
                            .build(),
                    10f, true);

            SlashSpawner.spawn(player, vCleave.toBuilder()
                            .sway(offset)
                            .length(length)
                            .build(),
                    10f, true);
        }
    }
}
