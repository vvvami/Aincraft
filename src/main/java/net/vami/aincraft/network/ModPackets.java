package net.vami.aincraft.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.network.packet.*;

@EventBusSubscriber(modid = Aincraft.MOD_ID)
public class ModPackets {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                SkillAbilityKeyC2SPacket.TYPE,
                SkillAbilityKeyC2SPacket.STREAM_CODEC,
                SkillAbilityKeyC2SPacket::handle);

        registrar.playToServer(
                SkillKeyC2SPacket.TYPE,
                SkillKeyC2SPacket.STREAM_CODEC,
                SkillKeyC2SPacket::handle);

        registrar.playToClient(
                SpawnSlashS2CPacket.TYPE,
                SpawnSlashS2CPacket.STREAM_CODEC,
                SpawnSlashS2CPacket::handle);

        registrar.playToClient(
                SkillCameraLockS2CPacket.TYPE,
                SkillCameraLockS2CPacket.STREAM_CODEC,
                SkillCameraLockS2CPacket::handle);

        registrar.playToServer(
                WeaponSlashC2SPacket.TYPE,
                WeaponSlashC2SPacket.STREAM_CODEC,
                WeaponSlashC2SPacket::handle);
    }
}
