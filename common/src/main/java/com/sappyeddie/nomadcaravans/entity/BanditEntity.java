package com.sappyeddie.nomadcaravans.entity;

import com.sappyeddie.nomadcaravans.ModRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
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

import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;

import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;


public class BanditEntity extends Monster implements RangedAttackMob {

    private static final EntityDataAccessor<Boolean> DATA_ARCHER =
            SynchedEntityData.defineId(BanditEntity.class, EntityDataSerializers.BOOLEAN);

    private static final float GEAR_DROP_CHANCE = 0.085F;

    private BanditBowAttackGoal bowGoal;
    private MeleeAttackGoal meleeGoal;

    public BanditEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ARMOR, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ARCHER, true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));


        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, BanditEntity.class).setAlertOthers());

    this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Villager.class, false));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));


        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, CaravanGuardEntity.class, true
        ));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, CaravanLeaderEntity.class, true
                ));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, CaravanFollowerEntity.class, true
        ));

        reassessWeaponGoal();
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
            this.bowGoal = new BanditBowAttackGoal(this, 1.0, 20, 15.0F);
        }
        if (this.meleeGoal == null) {
            this.meleeGoal = new MeleeAttackGoal(this, 1.25, false);
        }
        this.goalSelector.removeGoal(this.meleeGoal);
        this.goalSelector.removeGoal(this.bowGoal);
        if (this.getMainHandItem().getItem() instanceof BowItem) {
            this.goalSelector.addGoal(4, this.bowGoal);
        } else {
            this.goalSelector.addGoal(4, this.meleeGoal);
        }
    }

    public void equipBandit(boolean archer) {
        setArcher(archer);
        this.setLeftHanded(false);
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModRegistries.BANDIT_HELMET.get()));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModRegistries.BANDIT_CHESTPLATE.get()));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModRegistries.BANDIT_LEGGINGS.get()));
        setItemSlot(EquipmentSlot.FEET, new ItemStack(ModRegistries.BANDIT_BOOTS.get()));

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
        equipBandit(this.random.nextFloat() < 0.5F);
        return result;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.getMainHandItem().isEmpty()) {
            equipBandit(this.isArcher());
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
        this.playSound(SoundEvents.PILLAGER_AMBIENT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(arrow);
    }



    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Ranged", isArcher());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setArcher(input.getBooleanOr("Ranged", true));
        reassessWeaponGoal();
    }

    /**
     * Same strafing bow-attack pattern as CaravanGuardEntity's GuardBowAttackGoal,
     * duplicated here rather than shared since the two goal classes bind to different
     * concrete entity types via their constructor parameter.
     */
    static class BanditBowAttackGoal extends Goal {
        private final BanditEntity bandit;
        private final double speedModifier;
        private final int attackIntervalMin;
        private final float attackRadiusSqr;
        private int attackTime = -1;
        private int seeTime;
        private boolean strafingClockwise;
        private boolean strafingBackwards;
        private int strafingTime = -1;

        BanditBowAttackGoal(BanditEntity bandit, double speedModifier, int attackIntervalMin, float attackRadius) {
            this.bandit = bandit;
            this.speedModifier = speedModifier;
            this.attackIntervalMin = attackIntervalMin;
            this.attackRadiusSqr = attackRadius * attackRadius;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        private boolean isHoldingBow() {
            return this.bandit.isHolding(is -> is.getItem() instanceof BowItem);
        }

        @Override
        public boolean canUse() {
            return this.bandit.getTarget() != null && isHoldingBow();
        }

        @Override
        public boolean canContinueToUse() {
            return (canUse() || !this.bandit.getNavigation().isDone()) && isHoldingBow();
        }

        @Override
        public void start() {
            super.start();
            this.bandit.setAggressive(true);
        }

        @Override
        public void stop() {
            super.stop();
            this.bandit.setAggressive(false);
            this.seeTime = 0;
            this.attackTime = -1;
            this.bandit.stopUsingItem();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.bandit.getTarget();
            if (target == null) {
                return;
            }
            double distSq = this.bandit.distanceToSqr(target.getX(), target.getY(), target.getZ());
            boolean hasLineOfSight = this.bandit.getSensing().hasLineOfSight(target);
            if (hasLineOfSight != this.seeTime > 0) {
                this.seeTime = 0;
            }
            if (hasLineOfSight) {
                this.seeTime++;
            } else {
                this.seeTime--;
            }

            if (distSq <= this.attackRadiusSqr && this.seeTime >= 20) {
                this.bandit.getNavigation().stop();
                this.strafingTime++;
            } else {
                this.bandit.getNavigation().moveTo(target, this.speedModifier);
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20) {
                if (this.bandit.getRandom().nextFloat() < 0.3F) {
                    this.strafingClockwise = !this.strafingClockwise;
                }
                if (this.bandit.getRandom().nextFloat() < 0.3F) {
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
                this.bandit.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F,
                        this.strafingClockwise ? 0.5F : -0.5F);
                this.bandit.lookAt(target, 30.0F, 30.0F);
            } else {
                this.bandit.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }

            if (this.bandit.isUsingItem()) {
                if (!hasLineOfSight && this.seeTime < -60) {
                    this.bandit.stopUsingItem();
                } else if (hasLineOfSight) {
                    int ticksUsing = this.bandit.getTicksUsingItem();
                    if (ticksUsing >= 20) {
                        this.bandit.stopUsingItem();
                        this.bandit.performRangedAttack(target, BowItem.getPowerForTime(ticksUsing));
                        this.attackTime = this.attackIntervalMin;
                    }
                }
            } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
                this.bandit.startUsingItem(InteractionHand.MAIN_HAND);
            }
        }
    }
}
