package com.sappyeddie.nomadcaravans.tent.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class TentRoofBlock extends Block {

    public static final BooleanProperty FLAT = BooleanProperty.create("flat");
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final BooleanProperty IS_WALL = BooleanProperty.create("is_wall");

    public static final BooleanProperty DOORWAY = BooleanProperty.create("doorway");
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape FULL_BLOCK = Shapes.block();
    private static final VoxelShape FLAT_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
    private static final VoxelShape NORTH_WALL = Block.box(0.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SOUTH_WALL = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);
    private static final VoxelShape WEST_WALL = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape EAST_WALL = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 16.0D);

    public TentRoofBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FLAT, false)
                .setValue(OPEN, false)
                .setValue(IS_WALL, false)
                .setValue(DOORWAY, false)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FLAT, OPEN, IS_WALL, DOORWAY, FACING);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {

        if (type == PathComputationType.LAND && state.getValue(DOORWAY)) {
            return true;
        }

        return false;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Nullable
    private BlockPos findCore(Level level, BlockPos pos) {
        for (int y = 0; y <= 6; y++) {
            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    BlockPos check = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(check);
                    if (state.getBlock() instanceof TentBlock coreBlock
                            && coreBlock.computeRoofPositions(check, state.getValue(TentBlock.FACING)).contains(pos)) {
                        return check;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                       Player player, InteractionHand hand, BlockHitResult hit) {
        BlockPos corePos = findCore(level, pos);
        if (corePos != null && level.getBlockState(corePos).getBlock() instanceof TentBlock coreBlock) {

            return coreBlock.useItemOn(stack, level.getBlockState(corePos), level, corePos, player, hand, hit);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockPos corePos = findCore(level, pos);
            if (corePos != null && level.getBlockState(corePos).getBlock() instanceof TentBlock coreBlock) {
                Direction facing = level.getBlockState(corePos).getValue(TentBlock.FACING);
                for (BlockPos p : coreBlock.computeRoofPositions(corePos, facing)) {
                    if (p.equals(pos)) continue;
                    if (level.getBlockState(p).is(this)) {
                        level.removeBlock(p, false);
                    }
                }
                level.destroyBlock(corePos, !player.isCreative());
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        if (state.getValue(OPEN)) return Shapes.empty();
        if (state.getValue(FLAT)) return FLAT_SHAPE;
        if (state.getValue(IS_WALL)) {
            return switch (state.getValue(FACING)) {
                case NORTH -> NORTH_WALL;
                case SOUTH -> SOUTH_WALL;
                case WEST -> WEST_WALL;
                case EAST -> EAST_WALL;
                default -> FULL_BLOCK;
            };
        }
        return FULL_BLOCK;
    }
}
