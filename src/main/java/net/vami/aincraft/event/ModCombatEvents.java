package net.vami.aincraft.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.network.packet.WeaponSlashC2SPacket;
import net.vami.aincraft.render.SlashEffect;
import net.vami.aincraft.util.SlashSweep;

import java.util.Random;

@EventBusSubscriber(modid = Aincraft.MOD_ID, value = Dist.CLIENT)
public class ModCombatEvents {

    @SubscribeEvent
    public static void onAttackInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        int rotation = new Random().nextInt(-35, 25);

        SlashEffect slash = SlashEffect.getWeaponSlash(player).edit()
                .rotate(rotation)
                .build();

        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            boolean wouldHit = SlashSweep.wouldHit(player, slash);
            if (!wouldHit) return;
        }

        if (player.getAttackStrengthScale(0.5f) <= 0.9) return;
        if (!(player.getMainHandItem().getItem() instanceof TieredItem)) return;

        event.setCanceled(true);

        PacketDistributor.sendToServer(new WeaponSlashC2SPacket(rotation));
        player.resetAttackStrengthTicker();
    }
}
