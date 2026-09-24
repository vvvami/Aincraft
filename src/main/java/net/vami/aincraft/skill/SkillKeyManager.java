package net.vami.aincraft.skill;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class SkillKeyManager {

    public static void onPress(Player player, int pressedMs) {

    }

    public static void onAbilityPress(Player player, int skill) {
        if (player.level().isClientSide()) System.out.println("pressed on client! skill: " + skill);
        if (!player.level().isClientSide()) {
            System.out.println("pressed on server! skill: " + skill);


            ServerPlayer serverPlayer = (ServerPlayer) player;

            switch (skill) {
                case 1 -> Skill.activate(serverPlayer, Skills.TRIPLE_SLASH);
                case 2 -> Skill.activate(serverPlayer, Skills.INFINITE_RUSH);
                case 3 -> Skill.activate(serverPlayer, Skills.DISMANTLE);
                case 4 -> Skill.activate(serverPlayer, Skills.CLEAVE);
                case 5 -> Skill.activate(serverPlayer, Skills.WORLD_SLASH);
                case 6 -> Skill.activate(serverPlayer, Skills.DOMAIN_EXPANSION);
            }

        }
    }
}
