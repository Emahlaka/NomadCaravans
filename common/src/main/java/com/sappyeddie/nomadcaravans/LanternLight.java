package com.sappyeddie.nomadcaravans;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class LanternLight {

    private LanternLight() {
    }

    private static final int LIGHT_LEVEL = 14;

    @Nullable
    public static BlockPos follow(ServerLevel level, Entity carrier, @Nullable BlockPos current) {
        BlockPos target = BlockPos.containing(carrier.getX(), carrier.getY() + 1.2, carrier.getZ());
        if (target.equals(current)) {
            return current;
        }
        clear(level, current);
        if (canOccupy(level, target)) {
            level.setBlock(target,
                    Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, LIGHT_LEVEL),
                    Block.UPDATE_CLIENTS);
            return target;
        }
        return null;
    }

    public static void clear(ServerLevel level, @Nullable BlockPos pos) {
        if (pos != null && level.getBlockState(pos).is(Blocks.LIGHT)) {
            level.removeBlock(pos, false);
        }
    }

    private static boolean canOccupy(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(Blocks.LIGHT);
    }
}
