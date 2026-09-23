package net.vami.aincraft.init;

import net.minecraft.resources.ResourceLocation;
import net.vami.aincraft.Aincraft;
import net.vami.aincraft.render.SlashEffect;

public final class SlashEffects {

    public static final ResourceLocation SLASH_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Aincraft.MOD_ID, "textures/effect/slash.png");

    public static final SlashEffect RED_VERTICAL = SlashEffect.builder(SLASH_TEXTURE)
            .radius(1.6)
            .thickness(0.5)
            .angles(90, -90)
            .lifetime(7)
            .rotation(0)
            .color(0xFFFF3030)
            .fullBright(true)
            .build();

    public static final SlashEffect GREEN_HORIZONTAL = SlashEffect.builder(SLASH_TEXTURE)
            .radius(1.5)
            .thickness(0.5)
            .angles(-120, 70)
            .lifetime(5)
            .rotation(90)
            .color(0xFF30FF50)
            .fullBright(true)
            .build();

    public static final SlashEffect HEAVY_RED_DIAGONAL = SlashEffect.builder(SLASH_TEXTURE)
            .radius(1.9)
            .thickness(0.8)
            .angles(-135, 80)
            .lifetime(10)
            .rotation(-45)
            .color(0xFFFF2020)
            .build();

}
