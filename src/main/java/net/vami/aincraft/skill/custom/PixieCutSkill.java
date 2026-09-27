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

public class PixieCutSkill extends Skill {

    public PixieCutSkill() {
        super(30, false);
    }

    private static final int[] colors = new int[]{Color.magenta.getRGB(), Color.red.darker().getRGB()};

    private Vec3 originPos;

    @Override
    protected void onStart(LivingEntity entity) {
        originPos = entity.position();
    }

    @Override
    protected void onTick(LivingEntity entity, int age) {
        SlashEffect slash = SlashUtil.getWeaponSlash(entity).edit()
                .distance(SlashUtil.getStartDist(entity))
                .rotation(90)
                .fading(0.5f)
                .build();

        switch (age) {
            case 0 -> {
                entity.swing(InteractionHand.MAIN_HAND, true);
                SlashSpawner.spawn(entity, slash.edit()
                        .colors(colors)
                        .build(), 5f, false);
            }

            case 1 -> {
                entity.swing(InteractionHand.MAIN_HAND, true);
                SlashSpawner.spawn(entity, slash.edit()
                    .rotation(0)
                    .furthen(1)
                    .colors(colors)
                    .build(), 5f, false);
            }

            default -> {
                if (age < 12) return;

                Random random = new Random();

                for (int i = 0; i < 5; i++) {
                    Skill.activate(entity, new PixieSkill(this.originPos, new Vec3(
                            random.nextDouble(-5, 5),
                            random.nextDouble(-1, 4),
                            random.nextDouble(-5, 5)),
                            3f));
                }
            }
        }
    }
}
