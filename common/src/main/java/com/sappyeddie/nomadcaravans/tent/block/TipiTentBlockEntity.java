package com.sappyeddie.nomadcaravans.tent.block;

import com.sappyeddie.nomadcaravans.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TipiTentBlockEntity extends TentBlockEntity {

    public TipiTentBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.TIPI_TENT_BE.get(), pos, state);
    }
}
