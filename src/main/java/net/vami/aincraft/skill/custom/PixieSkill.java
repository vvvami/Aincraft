package net.vami.aincraft.skill.custom;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
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
    private Vec3 area;
    private float damage;

    public PixieSkill(Vec3 origin, Vec3 area, float damage) {
        super(1, false);
        this.origin = origin;
        this.area = area;
        this.damage = damage;
    }

    private static final int[] colors = new int[]{Color.magenta.getRGB(), Color.red.darker().getRGB()};

    @Override
    protected void onTick(LivingEntity entity, int age) {

        double length = new Random().nextDouble(0.12, 0.5);

        SlashEffect pixieSlash = SlashEffects.PIXIE.edit()
                .distance(0)
                .animation(0f, 0.2f)
                .line()
                .lengths(length, length * 4, length * 2, length * 2, length * 2, length * 2, length * 2)
                .thickness(0.1)
                .lift(-entity.getEyeHeight())
                .build();

        Vec3 targetPos = origin.add(area);

        Vec3 offset = targetPos.subtract(entity.position());

        SlashSpawner.spawn(entity, pixieSlash.edit()
                .rotation(90)
                .tilt(0)
                .colors(colors)
                .xOffset(offset.x)
                .yOffset(offset.y)
                .zOffset(offset.z)
                .build(), damage, false, true);

        SlashSpawner.spawn(entity, pixieSlash.edit()
                .rotation(0)
                .tilt(-entity.getXRot())
                .colors(colors)
                .xOffset(offset.x)
                .yOffset(offset.y)
                .zOffset(offset.z)
                .build(), damage, false, false);

        SlashSpawner.spawn(entity, pixieSlash.edit()
                .rotation(0)
                .tilt(90 - entity.getXRot())
                .colors(colors)
                .xOffset(offset.x)
                .yOffset(offset.y)
                .zOffset(offset.z)
                .build(), damage, false, false);

    }
}
