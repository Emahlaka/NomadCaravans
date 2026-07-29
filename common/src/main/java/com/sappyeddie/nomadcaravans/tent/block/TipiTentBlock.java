package com.sappyeddie.nomadcaravans.tent.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TipiTentBlock extends TentBlock {

    public TipiTentBlock(Properties props) {
        super(props);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TipiTentBlockEntity(pos, state);
    }

    @Override
    public net.minecraft.resources.Identifier getRolledUpModelId() {
        return com.sappyeddie.nomadcaravans.NomadCaravans.id("item/rolleduptipitent");
    }

    @Override
    protected List<BlockPos> templateDoorwayOffsets() {
        List<BlockPos> offsets = new ArrayList<>();
        offsets.add(new BlockPos(0, -3, -2));
        offsets.add(new BlockPos(0, -2, -2));
        return offsets;
    }

    @Override
    protected List<BlockPos> templateRoofOffsets() {
        List<BlockPos> offsets = new ArrayList<>();

        for (int y = -3; y <= -2; y++) {
            offsets.add(new BlockPos(-1, y, -2));
            offsets.add(new BlockPos(1, y, -2));
            for (int z = -1; z <= 1; z++) {
                offsets.add(new BlockPos(-2, y, z));
                offsets.add(new BlockPos(2, y, z));
            }
            offsets.add(new BlockPos(-1, y, 2));
            offsets.add(new BlockPos(0, y, 2));
            offsets.add(new BlockPos(1, y, 2));
        }
        offsets.addAll(templateDoorwayOffsets());

        for (int z = -1; z <= 1; z++) {
            offsets.add(new BlockPos(-1, -1, z));
            offsets.add(new BlockPos(1, -1, z));
        }
        offsets.add(new BlockPos(0, -1, -1));
        offsets.add(new BlockPos(0, -1, 1));

        return offsets;
    }

    @Override
    protected List<BlockPos> templateInteriorOffsets() {
        List<BlockPos> offsets = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -3; y <= -1; y++) {
                for (int z = -1; z <= 1; z++) {
                    offsets.add(new BlockPos(x, y, z));
                }
            }
        }
        return offsets;
    }
}
