package com.sappyeddie.nomadcaravans.entity;

import com.sappyeddie.nomadcaravans.ModRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

public class CaravanGuardEntity extends PathfinderMob implements RangedAttackMob {

    private static final EntityDataAccessor<Boolean> DATA_ARCHER =
            SynchedEntityData.defineId(CaravanGuardEntity.class, EntityDataSerializers.BOOLEAN);

    private GuardBowAttackGoal bowGoal;
    private MeleeAttackGoal meleeGoal;

    private static final double LEASH_RANGE = 25.0;
    private static final double LEASH_RANGE_SQ = LEASH_RANGE * LEASH_RANGE;
    private int nomadScanTimer;
    @Nullable
    private LivingEntity anchorNomad;

    public CaravanGuardEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ARCHER, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this,
                CaravanGuardEntity.class, CaravanLeaderEntity.class, CaravanFollowerEntity.class).setAlertOthers());

        this.targetSelector.addGoal(2, new DefendCaravanGoal(this));

        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 5, false, false,
                (entity, level) -> entity instanceof Enemy
                        && !(entity instanceof Creeper)
                        && !(entity instanceof CaravanGuardEntity)
                        && isWithinLeashOfCaravan(entity)));

        reassessWeaponGoal();
    }

    public static boolean isCaravanMember(net.minecraft.world.entity.LivingEntity entity) {
        return entity instanceof CaravanGuardEntity
                || entity instanceof CaravanLeaderEntity
                || entity instanceof CaravanFollowerEntity
                || entity instanceof YakEntity
                || entity instanceof BabyYakEntity
                || entity instanceof WildYakEntity
                || entity instanceof WanderingTraderYakEntity;
    }

    private boolean isWithinLeashOfCaravan(LivingEntity entity) {
        LivingEntity anchor = this.anchorNomad;
        return anchor == null || entity.distanceToSqr(anchor) <= LEASH_RANGE_SQ;
    }

    private void updateAnchorNomad() {
        LivingEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (PathfinderMob mob : this.level().getEntitiesOfClass(PathfinderMob.class,
                this.getBoundingBox().inflate(40.0),
                e -> e instanceof CaravanLeaderEntity || e instanceof CaravanFollowerEntity)) {
            double d = this.distanceToSqr(mob);
            if (d < best) {
                best = d;
                nearest = mob;
            }
        }
        this.anchorNomad = nearest;
    }

    public boolean isArcher() {
        return this.entityData.get(DATA_ARCHER);
    }

    public void setArcher(boolean archer) {
        this.entityData.set(DATA_ARCHER, archer);
    }

    public void reassessWeaponGoal() {
        if (this.level() == null || this.level().isClientSide()) {
            return;
        }
        if (this.bowGoal == null) {
            this.bowGoal = new GuardBowAttackGoal(this, 1.0, 20, 15.0F);
        }
        if (this.meleeGoal == null) {
            this.meleeGoal = new MeleeAttackGoal(this, 1.2, false);
        }
        this.goalSelector.removeGoal(this.meleeGoal);
        this.goalSelector.removeGoal(this.bowGoal);
        if (this.getMainHandItem().getItem() instanceof BowItem) {
            this.goalSelector.addGoal(4, this.bowGoal);
        } else {
            this.goalSelector.addGoal(4, this.meleeGoal);
        }
    }

    private static final float GEAR_DROP_CHANCE = 0.085F;

    public void equipGuard(boolean archer) {
        setArcher(archer);
        this.setLeftHanded(false);
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModRegistries.NOMAD_HELMET.get()));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModRegistries.NOMAD_CHESTPLATE.get()));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModRegistries.NOMAD_LEGGINGS.get()));
        setItemSlot(EquipmentSlot.FEET, new ItemStack(ModRegistries.NOMAD_BOOTS.get()));
        if (archer) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModRegistries.NOMAD_BOW.get()));
            setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        } else {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModRegistries.NOMAD_SWORD.get()));
            setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModRegistries.NOMAD_SHIELD.get()));
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.setDropChance(slot, GEAR_DROP_CHANCE);
        }
        reassessWeaponGoal();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        equipGuard(this.isArcher());
        return result;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        if (this.getMainHandItem().isEmpty()) {
            equipGuard(this.isArcher());
        }
        if (--this.nomadScanTimer <= 0) {
            this.nomadScanTimer = 20;
            updateAnchorNomad();
        }

        LivingEntity anchor = this.anchorNomad;
        if (anchor != null && anchor.isAlive() && this.distanceToSqr(anchor) > LEASH_RANGE_SQ) {
            this.setTarget(null);
            if (this.getNavigation().isDone()) {
                this.getNavigation().moveTo(anchor, 1.1);
            }
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (!(this.level() instanceof ServerLevel)) {
            return;
        }
        Arrow arrow = new Arrow(this.level(), this, new ItemStack(Items.ARROW), this.getMainHandItem());
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + horizontal * 0.2, dz, 1.6F, (float) (14 - this.level().getDifficulty().getId() * 4));
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(arrow);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Archer", isArcher());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setArcher(input.getBooleanOr("Archer", false));
        reassessWeaponGoal();
    }

    static class DefendCaravanGoal extends TargetGoal {
        private final CaravanGuardEntity guard;
        @Nullable
        private LivingEntity found;

        DefendCaravanGoal(CaravanGuardEntity guard) {
            super(guard, false);
            this.guard = guard;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            double range = this.guard.getAttributeValue(Attributes.FOLLOW_RANGE);
            AABB area = this.guard.getBoundingBox().inflate(range, 8.0, range);
            for (LivingEntity member : this.guard.level().getEntitiesOfClass(
                    LivingEntity.class, area, CaravanGuardEntity::isCaravanMember)) {
                LivingEntity attacker = member.getLastHurtByMob();
                if (attacker != null && attacker.isAlive() && attacker != this.guard && !isCaravanMember(attacker)) {
                    this.found = attacker;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            this.guard.setTarget(this.found);
            super.start();
        }
    }

    static class GuardBowAttackGoal extends Goal {
        private final CaravanGuardEntity guard;
        private final double speedModifier;
        private final int attackIntervalMin;
        private final float attackRadiusSqr;
        private int attackTime = -1;
        private int seeTime;
        private boolean strafingClockwise;
        private boolean strafingBackwards;
        private int strafingTime = -1;

        GuardBowAttackGoal(CaravanGuardEntity guard, double speedModifier, int attackIntervalMin, float attackRadius) {
            this.guard = guard;
            this.speedModifier = speedModifier;
            this.attackIntervalMin = attackIntervalMin;
            this.attackRadiusSqr = attackRadius * attackRadius;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        private boolean isHoldingBow() {
            return this.guard.isHolding(is -> is.getItem() instanceof BowItem);
        }

        @Override
        public boolean canUse() {
            return this.guard.getTarget() != null && isHoldingBow();
        }

        @Override
        public boolean canContinueToUse() {
            return (canUse() || !this.guard.getNavigation().isDone()) && isHoldingBow();
        }

        @Override
        public void start() {
            super.start();
            this.guard.setAggressive(true);
        }

        @Override
        public void stop() {
            super.stop();
            this.guard.setAggressive(false);
            this.seeTime = 0;
            this.attackTime = -1;
            this.guard.stopUsingItem();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.guard.getTarget();
            if (target == null) {
                return;
            }
            double distSq = this.guard.distanceToSqr(target.getX(), target.getY(), target.getZ());
            boolean hasLineOfSight = this.guard.getSensing().hasLineOfSight(target);
            if (hasLineOfSight != this.seeTime > 0) {
                this.seeTime = 0;
            }
            if (hasLineOfSight) {
                this.seeTime++;
            } else {
                this.seeTime--;
            }

            if (distSq <= this.attackRadiusSqr && this.seeTime >= 20) {
                this.guard.getNavigation().stop();
                this.strafingTime++;
            } else {
                this.guard.getNavigation().moveTo(target, this.speedModifier);
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20) {
                if (this.guard.getRandom().nextFloat() < 0.3F) {
                    this.strafingClockwise = !this.strafingClockwise;
                }
                if (this.guard.getRandom().nextFloat() < 0.3F) {
                    this.strafingBackwards = !this.strafingBackwards;
                }
                this.strafingTime = 0;
            }

            if (this.strafingTime > -1) {
                if (distSq > this.attackRadiusSqr * 0.75F) {
                    this.strafingBackwards = false;
                } else if (distSq < this.attackRadiusSqr * 0.25F) {
                    this.strafingBackwards = true;
                }
                this.guard.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F,
                        this.strafingClockwise ? 0.5F : -0.5F);
                this.guard.lookAt(target, 30.0F, 30.0F);
            } else {
                this.guard.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }

            if (this.guard.isUsingItem()) {
                if (!hasLineOfSight && this.seeTime < -60) {
                    this.guard.stopUsingItem();
                } else if (hasLineOfSight) {
                    int ticksUsing = this.guard.getTicksUsingItem();
                    if (ticksUsing >= 20) {
                        this.guard.stopUsingItem();
                        this.guard.performRangedAttack(target, BowItem.getPowerForTime(ticksUsing));
                        this.attackTime = this.attackIntervalMin;
                    }
                }
            } else if (--this.attackTime <= 0 && this.seeTime >= -60) {

                this.guard.startUsingItem(InteractionHand.MAIN_HAND);
            }
        }
    }
}
