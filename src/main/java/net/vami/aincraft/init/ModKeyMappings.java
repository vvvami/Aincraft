package net.vami.aincraft.init;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.network.packet.SkillAbilityKeyC2SPacket;
import net.vami.aincraft.network.packet.SkillKeyC2SPacket;
import net.vami.aincraft.skill.SkillKeyManager;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Aincraft.MOD_ID, value = Dist.CLIENT)
public class ModKeyMappings {
    private static boolean SKILL_ABILITY_USED = false;
    public static final KeyMapping SKILL = new KeyMapping("key.aincraft.skill",
            GLFW.GLFW_KEY_R, "key.categories.aincraft") {

        private boolean isDownOld = false;

        @Override
        public void setDown(boolean isDown) {
            super.setDown(isDown);

            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;

            if (isDownOld != isDown && isDown) {
                SKILL_LAST_PRESS = System.currentTimeMillis();
                SKILL_ABILITY_USED = false;
            } else if (isDownOld != isDown) {
                int dt = (int) (System.currentTimeMillis() - SKILL_LAST_PRESS);

                if (!SKILL_ABILITY_USED) {
                    PacketDistributor.sendToServer(new SkillKeyC2SPacket(1, dt));
                    SkillKeyC2SPacket.pressAction(player, 1, dt);
                }
            }
            isDownOld = isDown;
        }
    };

    private static long SKILL_LAST_PRESS = 0;

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(SKILL);
    }

    @EventBusSubscriber(value = Dist.CLIENT)
    public static class KeyEventListener {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            if (Minecraft.getInstance().screen == null) {
                SKILL.consumeClick();
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onKeyInput(InputEvent.Key event) {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.player == null) return;
            if (minecraft.screen != null) return;

            if (event.getAction() != GLFW.GLFW_PRESS) return;

            int ability = switch (event.getKey()) {
                case GLFW.GLFW_KEY_1 -> 1;
                case GLFW.GLFW_KEY_2 -> 2;
                case GLFW.GLFW_KEY_3 -> 3;
                case GLFW.GLFW_KEY_4 -> 4;
                case GLFW.GLFW_KEY_5 -> 5;
                case GLFW.GLFW_KEY_6 -> 6;
                case GLFW.GLFW_KEY_7 -> 7;
                case GLFW.GLFW_KEY_8 -> 8;
                case GLFW.GLFW_KEY_9 -> 9;
                default -> -1;
            };

            if (!SKILL.isDown()) return;

            if (ability == -1) return;

            for (KeyMapping hotbarKey : minecraft.options.keyHotbarSlots) {
                if (hotbarKey.matches(event.getKey(), event.getScanCode())) {
                    hotbarKey.consumeClick();
                }
            }

            SKILL_ABILITY_USED = true;

            if (event.getAction() == GLFW.GLFW_PRESS) {
                PacketDistributor.sendToServer(new SkillAbilityKeyC2SPacket(ability));
                SkillKeyManager.onAbilityPress(minecraft.player, ability);
            }
        }
    }
}
