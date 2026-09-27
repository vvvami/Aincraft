package net.vami.aincraft.skill;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
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

    private static final ArrayList<Active> ACTIVE = new ArrayList<>();
    private static final ArrayList<Active> PENDING = new ArrayList<>();

    private final int lifetime;
    private final boolean lockView;

    protected Skill(int lifetime, boolean lockView) {
        this.lifetime = lifetime;
        this.lockView = lockView;
    }

    public static void activate(LivingEntity entity, Skill skill) {
        Active active = new Active(entity, skill);
        PENDING.add(active);
        skill.onStart(entity);
    }

    protected void onStart(LivingEntity entity) {}

    protected void onTick(LivingEntity entity, int age) {}

    protected void onEnd(LivingEntity entity) {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<Active> iterator = ACTIVE.iterator();

        while (iterator.hasNext()) {
            Active active = iterator.next();
            active.tick();

            if (active.isFinished()) {
                active.finish();
                iterator.remove();
            }
        }

        ACTIVE.addAll(PENDING);
        PENDING.clear();
    }

    public int getLifetime() {
        return lifetime;
    }

    private static class Active {

        private final LivingEntity entity;
        private final Skill skill;

        private final float lockedYaw;
        private final float lockedPitch;

        private int age;

        private Active(LivingEntity entity, Skill skill) {
            this.entity = entity;
            this.skill = skill;

            lockedYaw = entity.getYRot();
            lockedPitch = entity.getXRot();

            if (skill.lockView && entity instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player,
                        new SkillCameraLockS2CPacket(lockedYaw, lockedPitch, true));

            }
        }

        private void tick() {
            if (isFinished()) return;

            if (skill.lockView) {
                entity.setYRot(lockedYaw);
                entity.setXRot(lockedPitch);
                entity.setYHeadRot(lockedYaw);
                entity.yBodyRot = lockedYaw;

                entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
                entity.hurtMarked = true;
            }

            skill.onTick(entity, age);
            age++;
        }

        private void finish() {
            skill.onEnd(entity);

            if (skill.lockView && entity instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player,
                        new SkillCameraLockS2CPacket(0, 0, false));
            }
        }

        private boolean isFinished() {
            return age >= skill.lifetime || !entity.isAlive();
        }
    }
}