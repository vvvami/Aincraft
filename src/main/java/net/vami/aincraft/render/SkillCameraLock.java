package net.vami.aincraft.render;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.vami.aincraft.Aincraft;

@EventBusSubscriber(modid = Aincraft.MOD_ID, value = Dist.CLIENT)
public class SkillCameraLock {

    private static boolean locked;
    private static float yaw;
    private static float pitch;

    private SkillCameraLock() {}

    public static void lock(float yaw, float pitch) {
        SkillCameraLock.yaw = yaw;
        SkillCameraLock.pitch = pitch;
        locked = true;
    }

    public static void unlock() {
        locked = false;
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!locked) return;

        event.setYaw(yaw);
        event.setPitch(pitch);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!locked) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        minecraft.player.setYRot(yaw);
        minecraft.player.setXRot(pitch);
        minecraft.player.setYHeadRot(yaw);
    }
}
