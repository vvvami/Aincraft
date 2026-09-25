package net.vami.aincraft.util.slash;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.vami.aincraft.Aincraft;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class SlashActions {

    private static final HashMap<ResourceLocation, SlashAction> ACTIONS = new HashMap<>();

    public static final ResourceLocation NONE = register(
            "none",
            condition -> false,
            context -> {});

    public static final ResourceLocation LAUNCH = register(
            "launch",
            cond -> cond.entity() instanceof LivingEntity,
            ctx -> ctx.entity().push(0, 2.5, 0));

    private static ResourceLocation register(String name, Predicate<SlashAction.Context> condition, Consumer<SlashAction.Context> action) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Aincraft.MOD_ID, name);
        ACTIONS.put(id, new SlashAction(condition, action));
        return id;
    }

    public static void run(ResourceLocation id, SlashAction.Context context) {
        if (id.equals(NONE)) return;

        SlashAction action = ACTIONS.get(id);

        if (action != null) {
            action.tryRun(context);
        }
    }
}
