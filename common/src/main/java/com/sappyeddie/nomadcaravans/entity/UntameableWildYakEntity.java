package com.sappyeddie.nomadcaravans.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import com.sappyeddie.nomadcaravans.LanternLight;
import com.sappyeddie.nomadcaravans.ModRegistries;
import com.sappyeddie.nomadcaravans.NomadConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class UntameableWildYakEntity extends Animal implements GeoEntity, NeutralMob {

    private static final EntityDataAccessor<Boolean> DATA_HAS_WOOL =
            SynchedEntityData.defineId(UntameableWildYakEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_GRAZING =
            SynchedEntityData.defineId(UntameableWildYakEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Byte> DATA_RAM_STATE =
            SynchedEntityData.defineId(UntameableWildYakEntity.class, EntityDataSerializers.BYTE);

    public static final byte RAM_NONE = 0;
    public static final byte RAM_PREPARE = 1;
    public static final byte RAM_CHARGE = 2;
    public static final byte RAM_IMPACT = 3;

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);

    private long persistentAngerEndTime = NO_ANGER_END_TIME;
    @Nullable
    private EntityReference<LivingEntity> persistentAngerTarget;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.yak.breathing");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.yak.walk");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.yak.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.yak.death");
    private static final RawAnimation GRAZE =
            RawAnimation.begin().thenLoop("animation.yak.grazing");
    private static final RawAnimation RAM_PREPARE_ANIM =
            RawAnimation.begin().thenPlayAndHold("animation.yak.ram_prepare");
    private static final RawAnimation RAM_CHARGE_ANIM =
            RawAnimation.begin().thenLoop("animation.yak.ram_charge");
    private static final RawAnimation RAM_IMPACT_ANIM =
            RawAnimation.begin().thenPlay("animation.yak.ram_impact");
    private static final RawAnimation ATTACK =
            RawAnimation.begin().thenPlay("animation.yak.attack");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public UntameableWildYakEntity(EntityType<? extends UntameableWildYakEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.STEP_HEIGHT, 1.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HAS_WOOL, true);
        builder.define(DATA_GRAZING, false);
        builder.define(DATA_RAM_STATE, RAM_NONE);
    }

    public byte getRamState() {
        return this.entityData.get(DATA_RAM_STATE);
    }

    public void setRamState(byte state) {
        this.entityData.set(DATA_RAM_STATE, state);
    }

    public boolean hasWool() {
        return this.entityData.get(DATA_HAS_WOOL);
    }

    public void setWool(boolean hasWool) {
        this.entityData.set(DATA_HAS_WOOL, hasWool);
    }

    public boolean isGrazing() {
        return this.entityData.get(DATA_GRAZING);
    }

    public void setGrazing(boolean grazing) {
        this.entityData.set(DATA_GRAZING, grazing);
    }

    @Override
    public long getPersistentAngerEndTime() {
        return this.persistentAngerEndTime;
    }

    @Override
    public void setPersistentAngerEndTime(long endTime) {
        this.persistentAngerEndTime = endTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> target) {
        this.persistentAngerTarget = target;
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setPersistentAngerEndTime(this.level().getGameTime() + PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            if (source.getEntity() instanceof LivingEntity attacker) {
                this.startPersistentAngerTimer();
                this.setPersistentAngerTarget(EntityReference.of(attacker));
            }
        }
        return hurt;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("HasWool", this.hasWool());
        this.addPersistentAngerSaveData(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setWool(input.getBooleanOr("HasWool", true));
        this.readPersistentAngerSaveData(this.level(), input);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0));
        this.goalSelector.addGoal(2, new RamAttackGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.25, stack -> stack.is(Items.WHEAT), false));
        this.goalSelector.addGoal(5, new YakGrazeGoal(this));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(Items.WHEAT);
    }

    public class YakGrazeGoal extends Goal {

        private final UntameableWildYakEntity yak;
        private int eatTime;

        public YakGrazeGoal(UntameableWildYakEntity yak) {
            this.yak = yak;
        }

        @Override
        public boolean canUse() {

            if (yak.hasWool())
                return false;

            if (yak.getRandom().nextInt(800) != 0)
                return false;

            BlockPos pos = yak.blockPosition().below();

            return yak.level().getBlockState(pos).is(Blocks.GRASS_BLOCK);
        }

        @Override
        public boolean canContinueToUse() {
            return eatTime > 0;
        }

        @Override
        public void start() {
            eatTime = 70;
            yak.setGrazing(true);
            yak.getNavigation().stop();
        }

        @Override
        public void tick() {
            eatTime--;

            yak.getNavigation().stop();

            if (eatTime <= 70 / 2) {
                BlockPos pos = yak.blockPosition().below();

                if (yak.level().getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
                    yak.level().setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
                }

                yak.setWool(true);
            }
            if (eatTime <= 0) {

                yak.setGrazing(false);
            }
        }

        @Override
        public void stop() {
            yak.setGrazing(false);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Items.SHEARS)) {
            if (!this.level().isClientSide() && this.hasWool()) {
                ItemStack wool = new ItemStack(ModRegistries.GRAY_YAK_WOOL_ITEM.get(), 2);
                Containers.dropItemStack(
                        this.level(),
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        wool
                );
                this.setWool(false);
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.HORSE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HORSE_DEATH;
    }

    @Override
    public void travel(Vec3 movementInput) {
        if (this.isGrazing()) {
            super.travel(Vec3.ZERO);
            return;
        }
        super.travel(movementInput);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("movement", 6, state -> {

            if (this.getRamState() != RAM_NONE)
                return PlayState.STOP;

            if (this.isGrazing())
                return state.setAndContinue(GRAZE);

            if (state.isMoving())
                return state.setAndContinue(WALK);

            return state.setAndContinue(IDLE);
        }));

        controllers.add(new AnimationController<UntameableWildYakEntity>("attack", 0, state -> {
            if (this.getRamState() == RAM_NONE && this.swinging) {
                return state.setAndContinue(ATTACK);
            }
            return PlayState.STOP;
        }));

        controllers.add(new AnimationController<UntameableWildYakEntity>("ram", 0, state -> switch (this.getRamState()) {
            case RAM_PREPARE -> state.setAndContinue(RAM_PREPARE_ANIM);
            case RAM_CHARGE -> state.setAndContinue(RAM_CHARGE_ANIM);
            case RAM_IMPACT -> state.setAndContinue(RAM_IMPACT_ANIM);
            default -> PlayState.STOP;
        }));

        controllers.add(new AnimationController<UntameableWildYakEntity>("reaction", 0, state -> {
            if (this.isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            if (this.hurtTime > 0) {
                return state.setAndContinue(HURT);
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        BabyYakEntity baby = ModRegistries.BABY_YAK.get().create(level, EntitySpawnReason.BREEDING);
        if (baby != null) {

            baby.setAge(-24000);

            baby.setParents(this, partner);
        }

        return baby;
    }

    private static final float COMPANION_PLAIN_CHANCE = 0.50F;
    private static final float COMPANION_TRADER_CHANCE = 0.35F;

    private static final float COMPANION_CALF_CHANCE = 0.15F;

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);

        if (reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION) {
            spawnHerd(level, difficulty);
        }
        return result;
    }

    private void spawnHerd(ServerLevelAccessor level, DifficultyInstance difficulty) {
        int companions = 1 + this.random.nextInt(3);
        for (int i = 0; i < companions; i++) {
            EntityType<?> type = rollCompanion();
            if (!(type.create(level.getLevel(), EntitySpawnReason.EVENT) instanceof Mob companion)) {
                continue;
            }
            companion.setPos(
                    this.getX() + this.random.nextInt(5) - 2,
                    this.getY(),
                    this.getZ() + this.random.nextInt(5) - 2);
            companion.setYRot(this.random.nextFloat() * 360.0F);
            if (companion instanceof UntameableBabyYakEntity calf) {
                calf.setAge(-24000);
            }
            companion.finalizeSpawn(level, difficulty, EntitySpawnReason.EVENT, null);
            level.addFreshEntity(companion);
        }
    }

    private EntityType<?> rollCompanion() {
        if (this.random.nextFloat() < COMPANION_CALF_CHANCE) {
            return ModRegistries.UNTAMEABLE_BABY_YAK.get();
        }
        float roll = this.random.nextFloat();
        if (roll < COMPANION_PLAIN_CHANCE) {
            return ModRegistries.UNTAMEABLE_YAK.get();
        }
        if (roll < COMPANION_PLAIN_CHANCE + COMPANION_TRADER_CHANCE) {
            return ModRegistries.UNTAMEABLE_WANDERING_TRADER_YAK.get();
        }
        return ModRegistries.UNTAMEABLE_WILD_YAK.get();
    }

    private Vec3 lastDeltaMovement = Vec3.ZERO;
    private float ropeSwingX, ropeSwingXO, ropeSwingVelX;
    private float ropeSwingZ, ropeSwingZO, ropeSwingVelZ;

    @Override
    public void tick() {
        super.tick();

        if (this.level() instanceof ServerLevel serverLevel) {
            this.updatePersistentAnger(serverLevel, true);
        }

        this.ropeSwingXO = this.ropeSwingX;
        this.ropeSwingZO = this.ropeSwingZ;

        Vec3 velocity = this.getDeltaMovement();
        Vec3 acceleration = velocity.subtract(this.lastDeltaMovement);
        this.lastDeltaMovement = velocity;

        float targetX = Mth.clamp((float) (-acceleration.z * 400.0), -25.0F, 25.0F);
        float targetZ = Mth.clamp((float) (acceleration.x * 400.0), -25.0F, 25.0F);

        final float stiffness = 0.15F;
        final float damping = 0.8F;

        this.ropeSwingVelX = (this.ropeSwingVelX + (targetX - this.ropeSwingX) * stiffness) * damping;
        this.ropeSwingX += this.ropeSwingVelX;

        this.ropeSwingVelZ = (this.ropeSwingVelZ + (targetZ - this.ropeSwingZ) * stiffness) * damping;
        this.ropeSwingZ += this.ropeSwingVelZ;
    }

    static class RamAttackGoal extends Goal {
        private static final int PREPARE_TICKS = 30;
        private static final int MAX_CHARGE_TICKS = 60;
        private static final int IMPACT_TICKS = 10;
        private static final int COOLDOWN_TICKS = 120;
        private static final double MIN_RANGE = 4.0;
        private static final double MAX_RANGE = 16.0;

        private static final double CHARGE_SPEED = 0.42;

        private final UntameableWildYakEntity yak;
        private int cooldown;
        private int timer;
        private Vec3 chargeDir = Vec3.ZERO;

        RamAttackGoal(UntameableWildYakEntity yak) {
            this.yak = yak;
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.cooldown > 0) {
                this.cooldown--;
                return false;
            }
            LivingEntity target = this.yak.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            double distSq = this.yak.distanceToSqr(target);
            return distSq >= MIN_RANGE * MIN_RANGE && distSq <= MAX_RANGE * MAX_RANGE
                    && this.yak.getSensing().hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            return this.yak.getRamState() != RAM_NONE && this.yak.getTarget() != null;
        }

        @Override
        public void start() {
            this.yak.getNavigation().stop();
            this.yak.setRamState(RAM_PREPARE);
            this.timer = PREPARE_TICKS;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.yak.getTarget();
            if (target == null) {
                return;
            }

            switch (this.yak.getRamState()) {
                case RAM_PREPARE -> {
                    this.yak.getNavigation().stop();
                    this.yak.getLookControl().setLookAt(target, 30.0F, 30.0F);
                    if (--this.timer <= 0) {

                        Vec3 toTarget = target.position().subtract(this.yak.position());
                        this.chargeDir = new Vec3(toTarget.x, 0.0, toTarget.z).normalize();
                        this.yak.setRamState(RAM_CHARGE);
                        this.timer = MAX_CHARGE_TICKS;
                        this.yak.playSound(SoundEvents.GOAT_PREPARE_RAM, 1.0F, 1.0F);
                    }
                }
                case RAM_CHARGE -> {
                    this.yak.setDeltaMovement(
                            this.chargeDir.x * CHARGE_SPEED,
                            this.yak.getDeltaMovement().y,
                            this.chargeDir.z * CHARGE_SPEED);
                    this.yak.setYRot((float) (Mth.atan2(this.chargeDir.z, this.chargeDir.x) * Mth.RAD_TO_DEG) - 90.0F);
                    this.yak.yBodyRot = this.yak.getYRot();

                    if (this.yak.distanceToSqr(target) <= 4.0 && this.yak.level() instanceof ServerLevel serverLevel) {
                        this.yak.doHurtTarget(serverLevel, target);
                        target.knockback(1.6, -this.chargeDir.x, -this.chargeDir.z);
                        this.yak.playSound(SoundEvents.GOAT_RAM_IMPACT, 1.0F, 1.0F);
                        this.yak.setRamState(RAM_IMPACT);
                        this.timer = IMPACT_TICKS;
                    } else if (--this.timer <= 0 || this.yak.horizontalCollision) {

                        this.yak.setRamState(RAM_IMPACT);
                        this.timer = IMPACT_TICKS;
                    }
                }
                case RAM_IMPACT -> {
                    this.yak.setDeltaMovement(Vec3.ZERO.x, this.yak.getDeltaMovement().y, Vec3.ZERO.z);
                    if (--this.timer <= 0) {
                        this.yak.setRamState(RAM_NONE);
                    }
                }
                default -> { }
            }
        }

        @Override
        public void stop() {
            this.yak.setRamState(RAM_NONE);
            this.yak.getNavigation().stop();
            this.cooldown = COOLDOWN_TICKS;
        }
    }

    public float getRopeSwingX(float partialTick) {
        return Mth.lerp(partialTick, this.ropeSwingXO, this.ropeSwingX);
    }

    public float getRopeSwingZ(float partialTick) {
        return Mth.lerp(partialTick, this.ropeSwingZO, this.ropeSwingZ);
    }
}
