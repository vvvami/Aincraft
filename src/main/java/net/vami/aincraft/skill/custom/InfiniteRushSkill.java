package net.vami.aincraft.skill.custom;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.init.SlashEffects;
import net.vami.aincraft.network.SlashSpawner;
import net.vami.aincraft.skill.Skill;

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
    protected void onStart(ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speed != null) speed.addOrUpdateTransientModifier(SPEED_MODIFIER);
    }

    @Override
    protected void onEnd(ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speed != null) speed.removeModifier(SPEED_MODIFIER_ID);
    }

    @Override
    protected void onTick(ServerPlayer player, int age) {
        if (!(age % 4 == 0)) return;
        SlashSpawner.spawn(player, SlashEffects.GREEN_HORIZONTAL.toBuilder()
                .rotation(SlashEffects.GREEN_HORIZONTAL.rotation() + new Random().nextInt(-25, 25))
                .radius(2 + new Random().nextFloat(0, 1))
                .angles(SlashEffects.GREEN_HORIZONTAL.startAngle() +
                                new Random().nextInt(-25, 25),
                        SlashEffects.GREEN_HORIZONTAL.endAngle() +
                                new Random().nextInt(-25, 25))
                .thickness(0.8)
                .lifetime(20)
                .animation(0.15f, 0.2f)
                .build(), 4f);
    }
}
