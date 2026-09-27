package net.vami.aincraft.event;

import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.skill.Skill;
import net.vami.aincraft.init.Skills;

@EventBusSubscriber(modid = Aincraft.MOD_ID)
public class ModCombatEvents {

    @SubscribeEvent
    public static void serverTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Zombie zombie)) return;
        if (zombie.level().isClientSide()) return;

        if (zombie.getTarget() == null) return;

        if (zombie.tickCount % 100 != 0) return;

        Skill.activate(zombie, Skills.TRIPLE_SLASH);
    }
}
