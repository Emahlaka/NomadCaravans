package com.sappyeddie.nomadcaravans.entity;

import com.sappyeddie.nomadcaravans.ModRegistries;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.UUID;

public class CaravanFollowerEntity extends Villager {
    private static final String LEADER_UUID_TAG = "NomadLeaderUUID";

    private static final int SUCCESSION_CHECK_INTERVAL = 40;
    private int successionTimer = 40;

    @Nullable
    private UUID leaderUuid;
    @Nullable
    private CaravanLeaderEntity leader;

    public CaravanFollowerEntity(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
    }

    public void setLeader(CaravanLeaderEntity leader) {
        this.leader = leader;
        this.leaderUuid = leader.getUUID();
    }

    @Nullable
    private CaravanLeaderEntity resolveLeader() {
        if (this.leader != null && this.leader.isAlive()) {
            return this.leader;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        if (this.leaderUuid != null
                && serverLevel.getEntity(this.leaderUuid) instanceof CaravanLeaderEntity caravanLeader
                && caravanLeader.isAlive()) {
            this.leader = caravanLeader;
            return caravanLeader;
        }

        CaravanLeaderEntity nearest = serverLevel.getEntitiesOfClass(CaravanLeaderEntity.class,
                        this.getBoundingBox().inflate(48.0), LivingEntity::isAlive).stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
        if (nearest != null) {
            this.leader = nearest;
            this.leaderUuid = nearest.getUUID();
        }
        return nearest;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new FollowLeaderGoal(this));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason,
                                        @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        Holder<VillagerProfession> none = level.registryAccess()
                .lookupOrThrow(Registries.VILLAGER_PROFESSION)
                .getOrThrow(VillagerProfession.NONE);
        this.setVillagerData(new VillagerData(ModRegistries.NOMAD, none, 1));
        return data;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.leaderUuid != null) {
            output.store(LEADER_UUID_TAG, UUIDUtil.CODEC, this.leaderUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read(LEADER_UUID_TAG, UUIDUtil.CODEC).ifPresent(uuid -> this.leaderUuid = uuid);
    }

    private static class FollowLeaderGoal extends Goal {
        private static final double STOP_DISTANCE_SQ = 9.0;
        private static final double START_DISTANCE_SQ = 49.0;
        private static final double TELEPORT_DISTANCE_SQ = 400.0;

        private final CaravanFollowerEntity follower;
        @Nullable
        private CaravanLeaderEntity target;

        FollowLeaderGoal(CaravanFollowerEntity follower) {
            this.follower = follower;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            this.target = this.follower.resolveLeader();

            return this.target != null && this.target.isTraveling()
                    && this.follower.distanceToSqr(this.target) > START_DISTANCE_SQ;
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null && this.target.isAlive() && this.target.isTraveling()
                    && this.follower.distanceToSqr(this.target) > STOP_DISTANCE_SQ;
        }

        @Override
        public void tick() {
            if (this.target == null) return;
            if (this.follower.distanceToSqr(this.target) > TELEPORT_DISTANCE_SQ) {
                BlockPos pos = this.target.blockPosition();
                this.follower.teleportTo(pos.getX(), pos.getY(), pos.getZ());
                return;
            }
            this.follower.getNavigation().moveTo(this.target, 0.6);
            this.follower.getLookControl().setLookAt(this.target);
        }

        @Override
        public void stop() {
            this.follower.getNavigation().stop();
            this.target = null;
        }
    }
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        VillagerData data = this.getVillagerData();
        if (data.type().value() != ModRegistries.NOMAD.get()) {
            this.setVillagerData(new VillagerData(ModRegistries.NOMAD, data.profession(), data.level()));
        }
        if (--this.successionTimer <= 0) {
            this.successionTimer = SUCCESSION_CHECK_INTERVAL;
            if (this.level() instanceof ServerLevel serverLevel) {
                checkSuccession(serverLevel);
            }
        }
    }

    private void checkSuccession(ServerLevel level) {
        if (resolveLeader() != null) {
            return;
        }
        CaravanFollowerEntity senior = level.getEntitiesOfClass(CaravanFollowerEntity.class,
                        this.getBoundingBox().inflate(48.0), LivingEntity::isAlive).stream()
                .min(Comparator.comparing(CaravanFollowerEntity::getUUID))
                .orElse(this);
        if (senior == this) {
            promoteToLeader(level);
        }
    }

    private void promoteToLeader(ServerLevel level) {
        CaravanLeaderEntity newLeader = ModRegistries.CARAVAN_LEADER.get()
                .create(level, EntitySpawnReason.CONVERSION);
        if (newLeader == null) {
            return;
        }
        newLeader.setPos(this.getX(), this.getY(), this.getZ());
        newLeader.setYRot(this.getYRot());
        newLeader.setXRot(this.getXRot());
        newLeader.finalizeSpawn(level, level.getCurrentDifficultyAt(this.blockPosition()),
                EntitySpawnReason.CONVERSION, null);
        if (this.hasCustomName()) {
            newLeader.setCustomName(this.getCustomName());
        }
        newLeader.setPersistenceRequired();
        level.addFreshEntity(newLeader);
        this.discard();
        NomadCaravans.LOGGER.info("Caravan follower promoted to leader at {}", this.blockPosition());
    }
}
