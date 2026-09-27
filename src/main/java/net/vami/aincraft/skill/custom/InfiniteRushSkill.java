package net.vami.aincraft.skill.custom;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.init.SlashEffects;
import net.vami.aincraft.network.SlashSpawner;
import net.vami.aincraft.render.SlashEffect;
import net.vami.aincraft.skill.Skill;
import net.vami.aincraft.util.SlashUtil;

import java.awt.*;
import java.util.Random;

public class InfiniteRushSkill extends Skill {

    public InfiniteRushSkill() {
        super(20, false);
    }

    private static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
            Aincraft.MOD_ID, "infinite_rush_skill");

    private static final AttributeModifier SPEED_MODIFIER =
            new AttributeModifier(
                    SPEED_MODIFIER_ID,
                    0.5,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    @Override
    protected void onStart(LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speed != null) speed.addOrUpdateTransientModifier(SPEED_MODIFIER);
    }

    @Override
    protected void onEnd(LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speed != null) speed.removeModifier(SPEED_MODIFIER_ID);
    }

    @Override
    protected void onTick(LivingEntity entity, int age) {
        if (!(age % 4 == 0)) return;

        SlashEffect slash = SlashUtil.getWeaponSlash(entity).edit()
                .rotate(new Random().nextInt(-25, 25))
                .inflate(new Random().nextFloat(0, 1))
                .lifetime(20)
                .distance(SlashUtil.getStartDist(entity))
                .furthen(new Random().nextDouble(-0.5, 0.5))
                .colors(Color.yellow.getRGB(), Color.green.getRGB(), Color.green.getRGB())
                .fading(0.2f)
                .build();

        entity.swing(InteractionHand.MAIN_HAND, true);

        SlashSpawner.spawn(entity, slash.edit()
                .angles(slash.startAngle() + new Random().nextInt(-25, 25),
                        slash.endAngle() + new Random().nextInt(-25, 25))
                .build(), 4f, false);
    }
}
