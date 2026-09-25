package net.vami.aincraft.skill.custom;

import net.minecraft.server.level.ServerPlayer;
import net.vami.aincraft.network.SlashSpawner;
import net.vami.aincraft.render.SlashEffect;
import net.vami.aincraft.skill.Skill;
import net.vami.aincraft.util.SlashUtil;
import net.vami.aincraft.util.slash.SlashActions;

import java.awt.*;

public class TripleSlashSkill extends Skill {

    public TripleSlashSkill() {
        super(14, false);
    }

    private static final int[] colors = new int[]{Color.magenta.getRGB(), Color.red.darker().getRGB(), Color.red.darker().darker().getRGB()};

    @Override
    protected void onTick(ServerPlayer player, int age) {
        SlashEffect slash = SlashUtil.getWeaponSlash(player).edit()
                .distance(SlashUtil.getStartDist(player))
                .fading(0.5f)
                .build();

        switch (age) {
            case 0 -> SlashSpawner.spawn(player, slash.edit()
                    .rotation(-45)
                    .colors(colors)
                    .build(), 5f, false);

            case 4 -> SlashSpawner.spawn(player, slash.edit()
                    .rotation(45)
                    .furthen(1)
                    .colors(colors)
                    .build(), 5f, false);

            case 8 -> SlashSpawner.spawn(player, slash.edit()
                    .rotation(90)
                    .inflate(1)
                    .furthen(2)
                    .thicken(0.2)
                    .angles(180, -180)
                    .colors(colors)
                    .build(), 8f, false);
        }
    }
}
