package net.vami.aincraft.skill.custom;

import net.minecraft.server.level.ServerPlayer;
import net.vami.aincraft.init.SlashEffects;
import net.vami.aincraft.network.SlashSpawner;
import net.vami.aincraft.render.SlashEffect;
import net.vami.aincraft.skill.Skill;

import java.awt.*;

public class CleaveSkill extends Skill {

    public CleaveSkill() {
        super(1, false);
    }

    private static final SlashEffect hCleave = SlashEffects.GREEN_HORIZONTAL.toBuilder()
            .thickness(0.5)
            .lifetime(10)
            .animation(0.3f, 0.8f)
            .segments(6)
            .color(Color.red.getRGB())
            .line(30)
            .distance(-2, 60)
            .build();

    private static final SlashEffect vCleave = hCleave.toBuilder().rotation(0).build();

    @Override
    protected void onTick(ServerPlayer player, int age) {

        for (int i = 0; i < 24; i += 3) {
            SlashSpawner.spawn(player, hCleave.toBuilder()
                            .lift(-10 + i)
                            .build(),
                    10f, true);

            SlashSpawner.spawn(player, vCleave.toBuilder()
                            .sway(-10 + i)
                            .build(),
                    10f, true);

        }
    }
}
