package net.vami.aincraft.util;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.vami.aincraft.init.SlashEffects;
import net.vami.aincraft.render.SlashEffect;

public class SlashUtil {

    public static boolean isSword() {
        return true;
    }

    public static SlashEffect getWeaponSlash(Player player) {
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed == null) return SlashEffects.GREEN_HORIZONTAL;

        AttributeInstance attackReach = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (attackReach == null) return SlashEffects.GREEN_HORIZONTAL;

        double speed = attackSpeed.getValue();
        double reach = attackReach.getValue();

        double startDist = 1 - (reach / 4);
        int lifetime = Math.max(2, (int) ((int) reach * 5 / speed));

        float fadeTicks = Math.min(2, lifetime);
        float fadeStart = 1 - fadeTicks / lifetime;

        return SlashEffects.WEAPON.edit()
                .distances(startDist, Math.max(startDist, reach - 2))
                .radius(reach / 3)
                .thickness(reach / 12)
                .animation((float) (0.75 / speed), fadeStart)
                .lifetime(lifetime)
                .build();
    }

    public static double getStartDist(Player player) {
        AttributeInstance attackReach = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (attackReach == null) return 0;

        return 1 - (attackReach.getValue() / 4);
    }
}
