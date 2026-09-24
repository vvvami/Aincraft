package net.vami.aincraft.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.network.SlashSpawner;
import net.vami.aincraft.network.packet.WeaponSlashC2SPacket;

@EventBusSubscriber(modid = Aincraft.MOD_ID)
public class ModCombatEvents {

    @SubscribeEvent
    public static void onAttackInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

//        HitResult hit = mc.hitResult;
//        if (hit != null && hit.getType() == HitResult.Type.BLOCK) return;

        if (player.getAttackStrengthScale(0.5F) <= 0.9F) return;
        if (!(player.getMainHandItem().getItem() instanceof TieredItem)) return;

        event.setCanceled(true);

        PacketDistributor.sendToServer(new WeaponSlashC2SPacket());
        player.resetAttackStrengthTicker();
    }
}
