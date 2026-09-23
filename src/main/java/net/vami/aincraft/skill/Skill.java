package net.vami.aincraft.skill;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.network.packet.SkillCameraLockS2CPacket;

import java.util.ArrayList;
import java.util.Iterator;

@EventBusSubscriber(modid = Aincraft.MOD_ID)
public abstract class Skill {

    private static final ArrayList<Active> ACTIVE_SKILLS = new ArrayList<>();

    private final int lifetime;
    private final boolean lockView;

    protected Skill(int lifetime, boolean lockView) {
        this.lifetime = lifetime;
        this.lockView = lockView;
    }

    public static void activate(ServerPlayer player, Skill skill) {
        Active active = new Active(player, skill);
        ACTIVE_SKILLS.add(active);
        skill.onStart(player);
    }

    protected void onStart(ServerPlayer player) {}

    protected abstract void onTick(ServerPlayer player, int age);

    protected void onEnd(ServerPlayer player) {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<Active> iterator = ACTIVE_SKILLS.iterator();

        while (iterator.hasNext()) {
            Active active = iterator.next();
            active.tick();

            if (active.isFinished()) {
                active.finish();
                iterator.remove();
            }
        }
    }

    public int getLifetime() {
        return lifetime;
    }

    private static class Active {

        private final ServerPlayer player;
        private final Skill skill;

        private final float lockedYaw;
        private final float lockedPitch;

        private int age;

        private Active(ServerPlayer player, Skill skill) {
            this.player = player;
            this.skill = skill;

            lockedYaw = player.getYRot();
            lockedPitch = player.getXRot();

            if (skill.lockView) {
                PacketDistributor.sendToPlayer(player,
                        new SkillCameraLockS2CPacket(lockedYaw, lockedPitch, true));

            }
        }

        private void tick() {
            if (isFinished()) return;

            if (skill.lockView) {
                player.setYRot(lockedYaw);
                player.setXRot(lockedPitch);
                player.setYHeadRot(lockedYaw);
                player.yBodyRot = lockedYaw;

                player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
                player.hurtMarked = true;
            }

            skill.onTick(player, age);
            age++;
        }

        private void finish() {
            skill.onEnd(player);

            if (skill.lockView) {
                PacketDistributor.sendToPlayer(player,
                        new SkillCameraLockS2CPacket(0, 0, false));
            }
        }

        private boolean isFinished() {
            return age >= skill.lifetime || !player.isAlive();
        }
    }
}