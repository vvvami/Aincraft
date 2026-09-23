package net.vami.aincraft.skill.custom;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.init.SlashEffects;
import net.vami.aincraft.network.SlashSpawner;
import net.vami.aincraft.skill.Skill;

import java.util.Random;

public class WindCutterSkill extends Skill {

    public WindCutterSkill() {
        super(1, true);
    }

    @Override
    protected void onTick(ServerPlayer player, int age) {
        SlashSpawner.spawn(player, SlashEffects.GREEN_HORIZONTAL.toBuilder()
                .thickness(0.8)
                .lifetime(10)
                .animation(0.3f, 0.8f)
                .distance(1.4, 10)
                .build(), 4f, true);
    }
}
