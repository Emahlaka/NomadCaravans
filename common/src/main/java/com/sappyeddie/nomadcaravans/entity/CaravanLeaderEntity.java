package com.sappyeddie.nomadcaravans.entity;

import com.sappyeddie.nomadcaravans.ModRegistries;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.tent.block.TentBlock;
import com.sappyeddie.nomadcaravans.tent.block.TentRoofBlock;
import com.sappyeddie.nomadcaravans.trade.ModVillagerProfessions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;

import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import net.minecraft.world.level.ServerLevelAccessor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CaravanLeaderEntity extends Villager {

    private static final int LEASH_CHECK_INTERVAL = 40;
    private int leashCheckTimer = 20;
    @Nullable
    private BlockPos campPostPos;

    private enum CampPhase { CAMPED, TRAVELING }
    private CampPhase campPhase = CampPhase.CAMPED;
    private int campDaysRemaining = -1;
    private long lastDayStamp = Long.MIN_VALUE;
    private int campInitDelay = 60;
    @Nullable
    private BlockPos travelTarget;
    private final List<PackedTent> packedTents = new ArrayList<>();

    private record PackedTent(ItemStack item, Direction facing, int dx, int dz) {
        static final Codec<PackedTent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ItemStack.CODEC.fieldOf("Item").forGetter(PackedTent::item),
                Direction.CODEC.fieldOf("Facing").forGetter(PackedTent::facing),
                Codec.INT.fieldOf("Dx").forGetter(PackedTent::dx),
                Codec.INT.fieldOf("Dz").forGetter(PackedTent::dz)
        ).apply(inst, PackedTent::new));
    }

    public CaravanLeaderEntity(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason,
                                        @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        this.setVillagerData(new VillagerData(ModRegistries.NOMAD, ModVillagerProfessions.NOMAD_MERCHANT, 1));
        this.setVillagerXp(1);
        if (level instanceof ServerLevel serverLevel) {
            this.updateTrades(serverLevel);
        }

        return data;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.level() instanceof ServerLevel serverLevel && !serverLevel.getServer().isSameThread()) {
            return new MerchantOffers();
        }
        return super.getOffers();
    }

    public boolean isTraveling() {
        return this.campPhase == CampPhase.TRAVELING;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!this.isTrading()) {
            VillagerData data = this.getVillagerData();
            boolean wrongProfession = data.profession().value() != ModVillagerProfessions.NOMAD_MERCHANT.get();
            boolean wrongType = data.type().value() != ModRegistries.NOMAD.get();
            if (wrongProfession || wrongType) {
                this.setVillagerData(new VillagerData(ModRegistries.NOMAD, ModVillagerProfessions.NOMAD_MERCHANT,
                        Math.max(data.level(), 1)));
                this.setVillagerXp(Math.max(this.getVillagerXp(), 1));
                this.updateTrades(serverLevel);
            }
        }

        if (this.campPhase == CampPhase.CAMPED) {

            if (--this.leashCheckTimer <= 0) {
                this.leashCheckTimer = LEASH_CHECK_INTERVAL;
                ensureYaksLeashedToPost(serverLevel);
            }
        }

        tickCamp(serverLevel);
    }

    private void tickCamp(ServerLevel level) {
        if (this.campInitDelay > 0) {
            this.campInitDelay--;
            return;
        }

        long worldTime = level.getGameTime();
        long day = worldTime / 24000L;
        long timeOfDay = Math.floorMod(worldTime, 24000L);

        if (this.campDaysRemaining < 0) {

            this.campDaysRemaining = 1 + this.random.nextInt(5);
            this.lastDayStamp = day;
        }

        if (day != this.lastDayStamp) {
            this.lastDayStamp = day;
            if (this.campPhase == CampPhase.CAMPED && this.campDaysRemaining > 0) {
                this.campDaysRemaining--;
            }
        }

        switch (this.campPhase) {
            case CAMPED -> {

                if (this.campDaysRemaining <= 0 && timeOfDay < 1000L) {
                    packCampAndDepart(level);
                }
            }
            case TRAVELING -> {

                if (timeOfDay >= 12000L) {
                    pitchCamp(level);
                } else {
                    driveTravel(level);
                }
            }
        }
    }

    private void packCampAndDepart(ServerLevel level) {
        if (this.campPostPos == null || !level.getBlockState(this.campPostPos).is(BlockTags.FENCES)) {
            this.campPostPos = findNearbyFencePost(level, this.blockPosition(), 16);
        }
        BlockPos anchor = this.campPostPos != null ? this.campPostPos : this.blockPosition();

        this.packedTents.clear();
        for (BlockPos core : findCampTentCores(level, this.blockPosition(), 20)) {
            if (level.getBlockState(core).getBlock() instanceof TentBlock tent) {
                Direction facing = level.getBlockState(core).getValue(TentBlock.FACING);
                int dx = core.getX() - anchor.getX();
                int dz = core.getZ() - anchor.getZ();
                ItemStack packed = tent.packTent(level, core);
                this.packedTents.add(new PackedTent(packed, facing, dx, dz));
            }
        }

        if (this.campPostPos != null && level.getBlockState(this.campPostPos).is(BlockTags.FENCES)) {
            level.removeBlock(this.campPostPos, false);
        }
        leashYaksTo(level, this);

        this.travelTarget = pickTravelTarget(level);
        this.campPhase = CampPhase.TRAVELING;
        this.campPostPos = null;
        NomadCaravans.LOGGER.info("Nomad caravan packed {} tent(s) and set out toward {}",
                this.packedTents.size(), this.travelTarget);
    }

    private void driveTravel(ServerLevel level) {
        if (this.travelTarget == null) {
            this.travelTarget = pickTravelTarget(level);
        }
        this.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.travelTarget, 0.6F, 1));
    }

    private void pitchCamp(ServerLevel level) {
        BlockPos anchor = groundColumn(level, this.blockPosition());
        int total = this.packedTents.size();
        int placed = 0;
        for (PackedTent pt : this.packedTents) {
            int x = anchor.getX() + pt.dx();
            int z = anchor.getZ() + pt.dz();
            BlockPos groundPos = groundColumn(level, new BlockPos(x, anchor.getY(), z));
            if (Block.byItem(pt.item().getItem()) instanceof TentBlock tent
                    && tent.isTentSpaceClear(level, groundPos, pt.facing())) {
                tent.deployTentAtGround(level, groundPos, pt.facing(), pt.item(), true);
                placed++;
            } else {

                Block.popResource(level, this.blockPosition(), pt.item());
            }
        }

        level.setBlockAndUpdate(anchor, ModRegistries.CAMP_FENCE_POST.get().defaultBlockState());
        this.campPostPos = anchor;
        LeashFenceKnotEntity knot = LeashFenceKnotEntity.getOrCreateKnot(level, anchor);
        leashYaksTo(level, knot);

        this.packedTents.clear();
        this.travelTarget = null;
        this.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        this.campPhase = CampPhase.CAMPED;
        this.campDaysRemaining = 1 + this.random.nextInt(5);
        this.lastDayStamp = level.getGameTime() / 24000L;
        NomadCaravans.LOGGER.info("Nomad caravan pitched {}/{} tent(s) at {}", placed, total, anchor);
    }

    private void leashYaksTo(ServerLevel level, Entity holder) {
        for (YakEntity yak : level.getEntitiesOfClass(YakEntity.class, this.getBoundingBox().inflate(16.0))) {
            yak.setLeashedTo(holder, true);
        }
    }

    private BlockPos pickTravelTarget(ServerLevel level) {
        Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(this.random);
        BlockPos base = this.blockPosition().relative(dir, 80);
        return groundColumn(level, base);
    }

    private static BlockPos groundColumn(ServerLevel level, BlockPos pos) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        return new BlockPos(pos.getX(), y, pos.getZ());
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("CampPhase", this.campPhase.name());
        output.putInt("CampDaysRemaining", this.campDaysRemaining);
        output.putLong("CampLastDay", this.lastDayStamp);
        if (this.campPostPos != null) {
            output.store("CampPost", BlockPos.CODEC, this.campPostPos);
        }
        if (this.travelTarget != null) {
            output.store("TravelTarget", BlockPos.CODEC, this.travelTarget);
        }
        if (!this.packedTents.isEmpty()) {
            output.store("PackedTents", PackedTent.CODEC.listOf(), List.copyOf(this.packedTents));
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        try {
            this.campPhase = CampPhase.valueOf(input.getStringOr("CampPhase", CampPhase.CAMPED.name()));
        } catch (IllegalArgumentException e) {
            this.campPhase = CampPhase.CAMPED;
        }
        this.campDaysRemaining = input.getIntOr("CampDaysRemaining", -1);
        this.lastDayStamp = input.getLongOr("CampLastDay", Long.MIN_VALUE);
        this.campPostPos = input.read("CampPost", BlockPos.CODEC).orElse(null);
        this.travelTarget = input.read("TravelTarget", BlockPos.CODEC).orElse(null);
        this.packedTents.clear();
        input.read("PackedTents", PackedTent.CODEC.listOf()).ifPresent(this.packedTents::addAll);
    }

    private static List<BlockPos> findCampTentCores(ServerLevel level, BlockPos center, int radius) {
        List<BlockPos> cores = new ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-radius, -4, -radius), center.offset(radius, 8, radius))) {
            if (level.getBlockState(p).getBlock() instanceof TentBlock) {
                cores.add(p.immutable());
            }
        }
        return cores;
    }

    private void ensureYaksLeashedToPost(ServerLevel level) {
        if (this.campPostPos == null || !level.getBlockState(this.campPostPos).is(BlockTags.FENCES)) {
            this.campPostPos = findNearbyFencePost(level, this.blockPosition(), 12);
        }
        if (this.campPostPos == null) {
            return;
        }

        List<YakEntity> yaks = level.getEntitiesOfClass(YakEntity.class, new AABB(this.campPostPos).inflate(12.0));
        LeashFenceKnotEntity knot = null;
        int leashed = 0;
        for (YakEntity yak : yaks) {

            if (yak.getLeashHolder() == null) {
                if (knot == null) {
                    knot = LeashFenceKnotEntity.getOrCreateKnot(level, this.campPostPos);
                }
                yak.setLeashedTo(knot, true);
                leashed++;
            }
        }
        if (leashed > 0) {
            NomadCaravans.LOGGER.info("Nomad caravan re-tethered {} yak(s) to the camp post at {}", leashed, this.campPostPos);
        }
    }

    @Nullable
    private static BlockPos findNearbyFencePost(ServerLevel level, BlockPos center, int radius) {
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-radius, -4, -radius), center.offset(radius, 4, radius))) {
            if (level.getBlockState(p).is(BlockTags.FENCES)) {
                double d = center.distSqr(p);
                if (d < bestDistSq) {
                    bestDistSq = d;
                    best = p.immutable();
                }
            }
        }
        return best;
    }
}
