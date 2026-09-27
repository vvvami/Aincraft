package net.vami.aincraft.skill.custom;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.vami.aincraft.init.SlashEffects;
import net.vami.aincraft.network.SlashSpawner;
import net.vami.aincraft.render.SlashEffect;
import net.vami.aincraft.skill.Skill;
import net.vami.aincraft.util.SlashUtil;

import java.awt.*;
import java.util.Random;

public class PixieSkill extends Skill {
    private Vec3 origin;
    private Vec3 pos;
    private Vec3 area;
    private float damage;

    public PixieSkill(Vec3 origin, Vec3 pos, Vec3 area, float damage) {
        super(1, false);
        this.origin = origin;
        this.pos = pos;
        this.area = area;
        this.damage = damage;
    }

    private static final int[] colors = new int[]{Color.magenta.getRGB(), Color.red.darker().getRGB()};

    @Override
    protected void onTick(ServerPlayer player, int age) {
        SlashEffect pixieSlash = SlashEffects.PIXIE.edit()
                .distance(0)
                .animation(0f, 0.2f)
                .line(new Random().nextDouble(0.25, 1))
                .thickness(0.1)
                .lift(-player.getEyeHeight())
                .build();

        Vec3 targetPos = origin.add(area);

        Vec3 offset = targetPos.subtract(pos);

        SlashSpawner.spawn(player, pixieSlash.edit()
                .rotation(90)
                .colors(colors)
                .xOffset(offset.x)
                .yOffset(offset.y)
                .zOffset(offset.z)
                .build(), damage, false, true);

        SlashSpawner.spawn(player, pixieSlash.edit()
                .rotation(0)
                .colors(colors)
                .xOffset(offset.x)
                .yOffset(offset.y)
                .zOffset(offset.z)
                .build(), damage, false, false);

        SlashSpawner.spawn(player, pixieSlash.edit()
                .rotation(0)
                .tilt(90)
                .colors(colors)
                .xOffset(offset.x)
                .yOffset(offset.y)
                .zOffset(offset.z)
                .build(), damage, false, false);

    }
}
