package net.vami.aincraft.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.vami.aincraft.Aincraft;

public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Aincraft.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> SLASH =
            SOUND_EVENTS.register("slash",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(
                                    Aincraft.MOD_ID, "slash")));

}
