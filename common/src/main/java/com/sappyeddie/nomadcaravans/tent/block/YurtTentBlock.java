package com.sappyeddie.nomadcaravans.tent.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class YurtTentBlock extends TentBlock {

    public YurtTentBlock(Properties props) {
        super(props);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new YurtTentBlockEntity(pos, state);
    }

    @Override
    public net.minecraft.resources.Identifier getRolledUpModelId() {
        return com.sappyeddie.nomadcaravans.NomadCaravans.id("item/rolledupyurttent");
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        if (ctx instanceof EntityCollisionContext entityCtx && entityCtx.getEntity() == null) {
            return Shapes.empty();
        }
        return super.getCollisionShape(state, level, pos, ctx);
    }

    @Override
    protected List<BlockPos> templateDoorwayOffsets() {
        List<BlockPos> offsets = new ArrayList<>();
        offsets.add(new BlockPos(0, -3, -5));
        offsets.add(new BlockPos(0, -2, -5));
        return offsets;
    }

    @Override
    protected List<BlockPos> templateRoofOffsets() {
        List<BlockPos> offsets = new ArrayList<>();

        for (int x = -2; x <= 2; x++) {
            offsets.add(new BlockPos(x, 0, -3));
            offsets.add(new BlockPos(x, 0, 3));
        }
        for (int z = -2; z <= 2; z++) {
            for (int x = -3; x <= 3; x++) {
                if (x == 0 && z == 0) continue;
                offsets.add(new BlockPos(x, 0, z));
            }
        }

        for (int x = -2; x <= 2; x++) {
            offsets.add(new BlockPos(x, -1, -4));
            offsets.add(new BlockPos(x, -1, 4));
        }
        for (int z = -2; z <= 2; z++) {
            offsets.add(new BlockPos(-4, -1, z));
            offsets.add(new BlockPos(4, -1, z));
        }
        offsets.add(new BlockPos(-3, -1, -3));
        offsets.add(new BlockPos(3, -1, -3));
        offsets.add(new BlockPos(-3, -1, 3));
        offsets.add(new BlockPos(3, -1, 3));

        for (int y = -2; y >= -3; y--) {
            for (int x = -2; x <= 2; x++) {
                offsets.add(new BlockPos(x, y, -5));
                offsets.add(new BlockPos(x, y, 5));
            }
            for (int z = -2; z <= 2; z++) {
                offsets.add(new BlockPos(-5, y, z));
                offsets.add(new BlockPos(5, y, z));
            }
            offsets.add(new BlockPos(-3, y, -4));
            offsets.add(new BlockPos(3, y, -4));
            offsets.add(new BlockPos(-3, y, 4));
            offsets.add(new BlockPos(3, y, 4));
            offsets.add(new BlockPos(-4, y, -3));
            offsets.add(new BlockPos(4, y, -3));
            offsets.add(new BlockPos(-4, y, 3));
            offsets.add(new BlockPos(4, y, 3));
        }

        return offsets;
    }

    @Override
    protected List<BlockPos> templateInteriorOffsets() {
        List<BlockPos> offsets = new ArrayList<>();
        for (int y = -3; y <= -1; y++) {
            for (int x = -2; x <= 2; x++) {
                offsets.add(new BlockPos(x, y, -4));
                offsets.add(new BlockPos(x, y, 4));
            }
            for (int x = -3; x <= 3; x++) {
                offsets.add(new BlockPos(x, y, -3));
                offsets.add(new BlockPos(x, y, 3));
            }
            for (int x = -4; x <= 4; x++) {
                offsets.add(new BlockPos(x, y, -2));
                offsets.add(new BlockPos(x, y, 2));
            }
            for (int z = -1; z <= 1; z++) {
                for (int x = -4; x <= 4; x++) {
                    offsets.add(new BlockPos(x, y, z));
                }
            }
        }
        return offsets;
    }

    @Override
    protected BlockState refineDummyState(BlockState baseState, BlockPos offset, Direction facing) {
        BlockState state = baseState;
        int x = offset.getX();
        int y = offset.getY();
        int z = offset.getZ();

        if (y == 0 && state.hasProperty(TentRoofBlock.FLAT)) {
            return state.setValue(TentRoofBlock.FLAT, true);
        }

        if (state.hasProperty(TentRoofBlock.IS_WALL) && state.hasProperty(TentRoofBlock.FACING)) {
            Direction wallFacing = null;
            if (z <= -5) wallFacing = Direction.NORTH;
            else if (z >= 5) wallFacing = Direction.SOUTH;
            else if (x <= -5) wallFacing = Direction.WEST;
            else if (x >= 5) wallFacing = Direction.EAST;

            if (wallFacing != null) {
                state = state.setValue(TentRoofBlock.IS_WALL, true)
                        .setValue(TentRoofBlock.FACING, rotateDirection(wallFacing, facing));
            }
        }
        return state;
    }
}
