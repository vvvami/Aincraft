package net.vami.aincraft.util.slash;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.vami.aincraft.render.SlashEffect;

import java.util.function.Consumer;
import java.util.function.Predicate;

public record SlashAction(Predicate<Context> condition, Consumer<Context> action) {

    public record Context(
            ServerPlayer player,
            SlashAttack attack,
            Entity entity,
            BlockPos blockPos,
            BlockState blockState,
            float damage,
            float progress) {}

    public void tryRun(Context context) {
        if (condition.test(context)) action.accept(context);
    }
}
