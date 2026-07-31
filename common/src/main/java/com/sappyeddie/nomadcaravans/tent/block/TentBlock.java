package com.sappyeddie.nomadcaravans.tent.block;

import com.sappyeddie.nomadcaravans.ModRegistries;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.entity.CaravanGuardEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class TentBlock extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);

    protected static final TagKey<Block> DO_NOT_PACK =
            TagKey.create(Registries.BLOCK, com.sappyeddie.nomadcaravans.NomadCaravans.id("do_not_pack"));

    protected TentBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    protected int getCoreHeightOffset() {
        return 3;
    }

    public net.minecraft.resources.Identifier getRolledUpModelId() {
        return NomadCaravans.id("item/rolleduptent");
    }

    protected abstract List<BlockPos> templateRoofOffsets();

    protected abstract List<BlockPos> templateInteriorOffsets();

    protected List<BlockPos> templateDoorwayOffsets() {
        return List.of();
    }

    protected static BlockPos rotate(BlockPos base, BlockPos offset, Direction facing) {
        int dx = offset.getX();
        int dy = offset.getY();
        int dz = offset.getZ();
        return switch (facing) {
            case SOUTH -> base.offset(-dx, dy, -dz);
            case EAST -> base.offset(-dz, dy, dx);
            case WEST -> base.offset(dz, dy, -dx);
            default -> base.offset(dx, dy, dz);
        };
    }

    protected static Direction rotateDirection(Direction dir, Direction tentFacing) {
        if (dir.getAxis() == Direction.Axis.Y) return dir;
        return switch (tentFacing) {
            case SOUTH -> dir.getOpposite();
            case EAST -> dir.getClockWise();
            case WEST -> dir.getCounterClockWise();
            default -> dir;
        };
    }

    public List<BlockPos> computeRoofPositions(BlockPos base, Direction facing) {
        List<BlockPos> result = new ArrayList<>();
        for (BlockPos off : templateRoofOffsets()) {
            result.add(rotate(base, off, facing));
        }
        return result;
    }

    public List<BlockPos> getDoorwayPositions(BlockPos corePos, Direction facing) {
        List<BlockPos> result = new ArrayList<>();
        for (BlockPos off : templateDoorwayOffsets()) {
            result.add(rotate(corePos, off, facing));
        }
        return result;
    }

    private static final int DOOR_CHECK_INTERVAL = 5;

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (st.getBlock() instanceof TentBlock tent && be instanceof TentBlockEntity tentBE) {
                tent.tickDoor(lvl, pos, st, tentBE);
            }
        };
    }

    private void tickDoor(Level level, BlockPos corePos, BlockState state, TentBlockEntity tentBE) {
        if (++tentBE.doorCheckTimer < DOOR_CHECK_INTERVAL) {
            return;
        }
        tentBE.doorCheckTimer = 0;

        Direction facing = state.getValue(FACING);
        List<BlockPos> doorways = getDoorwayPositions(corePos, facing);

        if (!tentBE.doorwaysMarked) {

            for (BlockPos doorway : doorways) {
                BlockState d = level.getBlockState(doorway);
                if (d.getBlock() instanceof TentRoofBlock && d.hasProperty(TentRoofBlock.DOORWAY)
                        && !d.getValue(TentRoofBlock.DOORWAY)) {
                    level.setBlock(doorway, d.setValue(TentRoofBlock.DOORWAY, true), Block.UPDATE_CLIENTS);
                }
            }
            tentBE.doorwaysMarked = true;
        }

        boolean nomadAtDoor = false;
        for (BlockPos doorway : doorways) {
            AABB box = new AABB(doorway).inflate(1.0);
            if (!level.getEntitiesOfClass(LivingEntity.class, box, CaravanGuardEntity::isCaravanMember).isEmpty()) {
                nomadAtDoor = true;
                break;
            }
        }

        boolean isOpen = state.getValue(OPEN);
        if (nomadAtDoor) {
            if (!isOpen) {
                setDoorOpen(level, corePos, true);
                tentBE.autoOpenedDoor = true;
            }
        } else {

            if (isOpen && tentBE.autoOpenedDoor) {
                setDoorOpen(level, corePos, false);
            }
            tentBE.autoOpenedDoor = false;
        }
    }

    public void setDoorOpen(Level level, BlockPos corePos, boolean open) {
        BlockState coreState = level.getBlockState(corePos);
        if (!(coreState.getBlock() instanceof TentBlock)) {
            return;
        }
        if (coreState.getValue(OPEN) != open) {
            level.setBlock(corePos, coreState.setValue(OPEN, open), Block.UPDATE_ALL);
        }
        for (BlockPos worldPos : getDoorwayPositions(corePos, coreState.getValue(FACING))) {
            BlockState doorState = level.getBlockState(worldPos);
            if (doorState.getBlock() instanceof TentRoofBlock && doorState.hasProperty(TentRoofBlock.OPEN)
                    && doorState.getValue(TentRoofBlock.OPEN) != open) {
                level.setBlockAndUpdate(worldPos, doorState.setValue(TentRoofBlock.OPEN, open));
            }
        }
    }

    protected Direction getPlacementFacing(@Nullable LivingEntity placer) {
        return placer != null ? placer.getDirection().getOpposite() : Direction.NORTH;
    }

    public boolean isTentSpaceClear(Level level, BlockPos anchorPos, Direction desired) {
        BlockPos corePos = anchorPos.above(getCoreHeightOffset());
        List<BlockPos> roofBlocks = computeRoofPositions(corePos, desired);

        List<BlockPos> interiorBlocks = new ArrayList<>();
        for (BlockPos offset : templateInteriorOffsets()) {
            interiorBlocks.add(rotate(corePos, offset, desired));
        }

        return roofBlocks.stream().allMatch(p -> level.getBlockState(p).canBeReplaced())
                && level.getBlockState(corePos).canBeReplaced()
                && interiorBlocks.stream().allMatch(p -> p.equals(anchorPos) || level.getBlockState(p).canBeReplaced());
    }

    protected BlockState getDummyRoofState() {
        return ModRegistries.TENT_ROOF_BLOCK.get().defaultBlockState();
    }

    protected BlockState refineDummyState(BlockState baseState, BlockPos offset, Direction facing) {
        BlockState state = baseState;
        if (offset.getY() == 0 && state.hasProperty(TentRoofBlock.FLAT)) {
            state = state.setValue(TentRoofBlock.FLAT, true);
        }
        if (state.hasProperty(TentRoofBlock.IS_WALL) && state.hasProperty(TentRoofBlock.FACING)) {
            int minZ = 0;
            int maxZ = 0;
            for (BlockPos p : templateRoofOffsets()) {
                minZ = Math.min(minZ, p.getZ());
                maxZ = Math.max(maxZ, p.getZ());
            }
            boolean isWall = false;
            Direction wallFacing = Direction.NORTH;
            if (offset.getX() <= -2) {
                isWall = true;
                wallFacing = Direction.WEST;
            } else if (offset.getX() >= 2) {
                isWall = true;
                wallFacing = Direction.EAST;
            } else if (offset.getZ() <= minZ) {
                isWall = true;
                wallFacing = Direction.NORTH;
            } else if (offset.getZ() >= maxZ) {
                isWall = true;
                wallFacing = Direction.SOUTH;
            }
            if (isWall) {
                state = state.setValue(TentRoofBlock.IS_WALL, true)
                        .setValue(TentRoofBlock.FACING, rotateDirection(wallFacing, facing));
            }
        }
        return state;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide()) return;
        Direction desired = getPlacementFacing(placer);
        level.removeBlock(pos, false);
        UUID owner = placer instanceof Player player ? player.getUUID() : null;
        deployTent(level, pos.above(getCoreHeightOffset()), desired, stack, owner, false);
    }

    public void deployTent(Level level, BlockPos corePos, Direction facing, ItemStack stack,
                           @Nullable UUID owner, boolean campLocked) {
        if (level.isClientSide()) return;
        BlockState coreState = defaultBlockState().setValue(FACING, facing).setValue(OPEN, false);
        level.setBlock(corePos, coreState, Block.UPDATE_CLIENTS);

        Set<BlockPos> doorwayOffsets = new HashSet<>(templateDoorwayOffsets());
        BlockState defaultDummy = getDummyRoofState();
        for (BlockPos offset : templateRoofOffsets()) {
            BlockPos worldPos = rotate(corePos, offset, facing);
            BlockState shell = refineDummyState(defaultDummy, offset, facing);
            if (doorwayOffsets.contains(offset) && shell.hasProperty(TentRoofBlock.DOORWAY)) {
                shell = shell.setValue(TentRoofBlock.DOORWAY, true);
            }
            level.setBlockAndUpdate(worldPos, shell);
        }

        if (level.getBlockEntity(corePos) instanceof TentBlockEntity tentBE) {
            if (owner != null) {
                tentBE.setOwnerUUID(owner);
            }
            Component customName = stack.get(DataComponents.CUSTOM_NAME);
            if (customName != null) {
                tentBE.setCustomName(customName);
            }
            if (campLocked) {
                tentBE.setCampLocked(true);
            }
            if (stack.has(DataComponents.CUSTOM_DATA)
                    && stack.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean("Bandit").orElse(false)) {
                tentBE.setBandit(true);
            }
        }

        restoreInterior(level, corePos, facing, stack);
    }

    public void deployTentAtGround(Level level, BlockPos groundPos, Direction facing, ItemStack stack, boolean campLocked) {
        deployTent(level, groundPos.above(getCoreHeightOffset()), facing, stack, null, campLocked);
    }

    public ItemStack packTent(Level level, BlockPos corePos) {
        BlockState state = level.getBlockState(corePos);
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
        ItemStack tentItem = new ItemStack(this.asItem());
        if (level.getBlockEntity(corePos) instanceof TentBlockEntity tentBE && tentBE.getCustomName() != null) {
            tentItem.set(DataComponents.CUSTOM_NAME, tentBE.getCustomName());
        }
        captureInterior(level, corePos, facing, tentItem);
        level.removeBlock(corePos, false);
        cleanupTent(level, corePos, facing);
        return tentItem;
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                       Player player, InteractionHand hand, BlockHitResult hit) {

        if (player.isCrouching() && stack.isEmpty()) {
            if (!level.isClientSide()) {
                if (!canModify(level, pos, player)) {
                    player.sendSystemMessage(Component.translatable("message.nomadcaravans.tent.not_owner")
                            .withStyle(net.minecraft.ChatFormatting.RED));
                    return InteractionResult.FAIL;
                }
                Direction facing = state.getValue(FACING);
                ItemStack tentItem = new ItemStack(this.asItem());
                if (level.getBlockEntity(pos) instanceof TentBlockEntity tentBE && tentBE.getCustomName() != null) {
                    tentItem.set(DataComponents.CUSTOM_NAME, tentBE.getCustomName());
                }

                captureInterior(level, pos, facing, tentItem);

                if (!player.getInventory().add(tentItem)) {
                    Block.popResource(level, pos, tentItem);
                }
                level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.BLOCKS, 1.0F, 1.0F);

                level.removeBlock(pos, false);
                cleanupTent(level, pos, facing);
            }
            return InteractionResult.CONSUME;
        }

        if (stack.isEmpty()) {
            if (!level.isClientSide()) {
                boolean willOpen = !state.getValue(OPEN);
                setDoorOpen(level, pos, willOpen);
                level.playSound(null, pos, willOpen ? SoundEvents.WOOL_BREAK : SoundEvents.WOOL_PLACE,
                        SoundSource.BLOCKS, 1.0F, 1.2F);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    protected boolean canModify(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof TentBlockEntity tentBE) {
            if (tentBE.isCampLocked()) {
                return false;
            }
            if (player.isCreative()) {
                return true;
            }
            UUID owner = tentBE.getOwnerUUID();
            return owner == null || owner.equals(player.getUUID());
        }
        return true;
    }

    protected void cleanupTent(Level level, BlockPos corePos, Direction facing) {
        Block roof = ModRegistries.TENT_ROOF_BLOCK.get();
        for (BlockPos p : computeRoofPositions(corePos, facing)) {
            if (level.getBlockState(p).is(roof)) {
                level.removeBlock(p, false);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            cleanupTent(level, pos, state.getValue(FACING));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TentBlockEntity tentBE) {
            if (tentBE.isCampLocked()) {
                return List.of();
            }
            ItemStack tentItem = new ItemStack(this.asItem());
            if (tentBE.getCustomName() != null) {
                tentItem.set(DataComponents.CUSTOM_NAME, tentBE.getCustomName());
            }
            return List.of(tentItem);
        }
        return List.of(new ItemStack(this.asItem()));
    }

    protected Rotation calculateRotation(Direction original, Direction placed) {
        if (original == placed) return Rotation.NONE;
        return switch (original) {
            case NORTH -> placed == Direction.EAST ? Rotation.CLOCKWISE_90 : placed == Direction.SOUTH ? Rotation.CLOCKWISE_180 : Rotation.COUNTERCLOCKWISE_90;
            case EAST -> placed == Direction.SOUTH ? Rotation.CLOCKWISE_90 : placed == Direction.WEST ? Rotation.CLOCKWISE_180 : Rotation.COUNTERCLOCKWISE_90;
            case SOUTH -> placed == Direction.WEST ? Rotation.CLOCKWISE_90 : placed == Direction.NORTH ? Rotation.CLOCKWISE_180 : Rotation.COUNTERCLOCKWISE_90;
            case WEST -> placed == Direction.NORTH ? Rotation.CLOCKWISE_90 : placed == Direction.EAST ? Rotation.CLOCKWISE_180 : Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    protected AABB getPickupInteriorBox(BlockPos corePos) {
        int radius = 1;
        int minY = -getCoreHeightOffset();
        for (BlockPos o : templateInteriorOffsets()) {
            radius = Math.max(radius, Math.max(Math.abs(o.getX()), Math.abs(o.getZ())));
            minY = Math.min(minY, o.getY());
        }
        return new AABB(
                corePos.getX() - radius, corePos.getY() + minY, corePos.getZ() - radius,
                corePos.getX() + radius + 1, corePos.getY() + 1, corePos.getZ() + radius + 1);
    }

    protected void captureInterior(Level level, BlockPos corePos, Direction facing, ItemStack tentItem) {
        CompoundTag tag = tentItem.has(DataComponents.CUSTOM_DATA)
                ? tentItem.get(DataComponents.CUSTOM_DATA).copyTag()
                : new CompoundTag();

        ListTag entityList = new ListTag();
        for (Entity ent : level.getEntitiesOfClass(Entity.class, getPickupInteriorBox(corePos),
                e -> e instanceof ArmorStand || e instanceof HangingEntity)) {
            try {
                ProblemReporter.Collector reporter = new ProblemReporter.Collector(() -> "nomadcaravans-tent-entity");
                TagValueOutput out = TagValueOutput.createWithContext(reporter, level.registryAccess());
                ent.save(out);
                CompoundTag stored = new CompoundTag();
                stored.put("EntityTag", out.buildResult());
                stored.putDouble("relX", ent.getX() - (corePos.getX() + 0.5));
                stored.putDouble("relY", ent.getY() - corePos.getY());
                stored.putDouble("relZ", ent.getZ() - (corePos.getZ() + 0.5));
                entityList.add(stored);
                ent.discard();
            } catch (Exception e) {
                NomadCaravans.LOGGER.error("Failed to pack a tent interior entity; leaving it in place", e);
            }
        }

        ListTag interiorList = new ListTag();
        Set<BlockPos> interiorWorld = new HashSet<>();
        for (BlockPos o : templateInteriorOffsets()) interiorWorld.add(rotate(corePos, o, facing));
        List<BlockPos> toRemove = new ArrayList<>();
        for (BlockPos offset : templateInteriorOffsets()) {
            BlockPos worldPos = rotate(corePos, offset, facing);
            BlockState intState = level.getBlockState(worldPos);
            if (intState.getBlock() instanceof BedBlock
                    && !interiorWorld.contains(worldPos.relative(BedBlock.getConnectedDirection(intState)))) {
                continue;
            }
            if (intState.isAir() || intState.getDestroySpeed(level, worldPos) < 0 || intState.is(DO_NOT_PACK)
                    || intState.getBlock() instanceof TentBlock || intState.getBlock() instanceof TentRoofBlock) {
                continue;
            }
            CompoundTag blockTag = new CompoundTag();
            blockTag.putIntArray("Offset", new int[]{offset.getX(), offset.getY(), offset.getZ()});
            blockTag.put("State", NbtUtils.writeBlockState(intState));
            BlockEntity be = level.getBlockEntity(worldPos);
            if (be != null) {
                try {
                    blockTag.put("NBT", be.saveWithFullMetadata(level.registryAccess()));
                } catch (Exception e) {
                    NomadCaravans.LOGGER.error("Failed to pack tent interior block entity at {}; packing it empty", worldPos, e);
                }
                level.removeBlockEntity(worldPos);
            }
            interiorList.add(blockTag);
            toRemove.add(worldPos);
        }
        for (BlockPos p : toRemove) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), 18);
        }

        if (!interiorList.isEmpty()) tag.put("InteriorData", interiorList);
        if (!entityList.isEmpty()) tag.put("EntityData", entityList);
        if (!interiorList.isEmpty() || !entityList.isEmpty()) {
            tag.putString("OriginalFacing", facing.getName());
            tentItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    protected void restoreInterior(Level level, BlockPos corePos, Direction placedFacing, ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) return;
        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
        if (!tag.contains("InteriorData") && !tag.contains("EntityData")) return;

        Direction originalFacing = Direction.byName(tag.getString("OriginalFacing").orElse("north"));
        if (originalFacing == null) originalFacing = placedFacing;
        Rotation rot = calculateRotation(originalFacing, placedFacing);

        ListTag interiorList = tag.getListOrEmpty("InteriorData");
        List<BlockPos> placed = new ArrayList<>();
        for (int i = 0; i < interiorList.size(); i++) {
            CompoundTag blockTag = interiorList.getCompound(i).orElse(new CompoundTag());
            int[] off = blockTag.getIntArray("Offset").orElse(new int[]{0, 0, 0});
            if (off.length != 3) continue;
            BlockPos worldPos = rotate(corePos, new BlockPos(off[0], off[1], off[2]), placedFacing);
            BlockState intState = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK),
                    blockTag.getCompound("State").orElse(new CompoundTag())).rotate(rot);
            level.setBlock(worldPos, intState, 18);
            placed.add(worldPos);

            if (blockTag.contains("NBT")) {
                BlockEntity be = level.getBlockEntity(worldPos);
                if (be != null) {
                    CompoundTag nbt = blockTag.getCompound("NBT").orElse(new CompoundTag());
                    try {
                        ProblemReporter.Collector reporter = new ProblemReporter.Collector(() -> "nomadcaravans-tent-be");
                        TagValueInput in = (TagValueInput) TagValueInput.create(reporter, level.registryAccess(), nbt);
                        be.loadWithComponents(in);
                    } catch (Exception e) {
                        NomadCaravans.LOGGER.error("Failed to restore tent interior block entity at {}; placed empty", worldPos, e);
                    }
                }
            }
        }
        for (BlockPos p : placed) {
            level.updateNeighborsAt(p, level.getBlockState(p).getBlock());
        }

        if (tag.contains("EntityData")) {
            ListTag entityList = tag.getListOrEmpty("EntityData");
            for (int i = 0; i < entityList.size(); i++) {
                CompoundTag stored = entityList.getCompound(i).orElse(new CompoundTag());
                CompoundTag entityData = stored.getCompound("EntityTag").orElse(new CompoundTag());
                double dx = stored.getDouble("relX").orElse(0.0);
                double dy = stored.getDouble("relY").orElse(0.0);
                double dz = stored.getDouble("relZ").orElse(0.0);
                double rx = dx;
                double rz = dz;
                float yawOffset = 0;
                switch (rot) {
                    case CLOCKWISE_90 -> { rx = -dz; rz = dx; yawOffset = 90; }
                    case CLOCKWISE_180 -> { rx = -dx; rz = -dz; yawOffset = 180; }
                    case COUNTERCLOCKWISE_90 -> { rx = dz; rz = -dx; yawOffset = -90; }
                    default -> { }
                }
                double newX = corePos.getX() + 0.5 + rx;
                double newY = corePos.getY() + dy;
                double newZ = corePos.getZ() + 0.5 + rz;

                if (entityData.contains("Facing")) {
                    Direction oldFacing = Direction.from3DDataValue(entityData.getByte("Facing").orElse((byte) 0) & 0xFF);
                    Direction newFacing = rot.rotate(oldFacing);
                    entityData.putByte("Facing", (byte) newFacing.get3DDataValue());
                    entityData.putInt("TileX", Mth.floor(newX));
                    entityData.putInt("TileY", Mth.floor(newY));
                    entityData.putInt("TileZ", Mth.floor(newZ));
                }

                final float finalYaw = yawOffset;
                Entity spawned = EntityType.loadEntityRecursive(entityData, level, EntitySpawnReason.LOAD, e -> {
                    e.setPos(newX, newY, newZ);
                    e.setYRot(e.getYRot() + finalYaw);
                    if (e instanceof LivingEntity) {
                        e.setYHeadRot(e.getYHeadRot() + finalYaw);
                        e.setYBodyRot(e.getYRot());
                    }
                    return e;
                });
                if (spawned != null) {
                    level.addFreshEntity(spawned);
                }
            }
        }
    }
}