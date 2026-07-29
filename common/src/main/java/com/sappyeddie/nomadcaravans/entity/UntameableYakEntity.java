package com.sappyeddie.nomadcaravans.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import com.sappyeddie.nomadcaravans.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
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

public class UntameableYakEntity extends Animal implements GeoEntity {

    private static final EntityDataAccessor<Boolean> DATA_HAS_WOOL =
            SynchedEntityData.defineId(UntameableYakEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_GRAZING =
            SynchedEntityData.defineId(UntameableYakEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.yak.breathing");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.yak.walk");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.yak.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.yak.death");
    private static final RawAnimation GRAZE =
            RawAnimation.begin().thenLoop("animation.yak.grazing");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public UntameableYakEntity(EntityType<? extends UntameableYakEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HAS_WOOL, true);
        builder.define(DATA_GRAZING, false);
    }

    public void setWool(boolean hasWool) {
        this.entityData.set(DATA_HAS_WOOL, hasWool);
    }

    public boolean hasWool() {
        return this.entityData.get(DATA_HAS_WOOL);
    }

    public boolean isGrazing() {
        return this.entityData.get(DATA_GRAZING);
    }

    public void setGrazing(boolean grazing) {
        this.entityData.set(DATA_GRAZING, grazing);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("HasWool", this.hasWool());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setWool(input.getBooleanOr("HasWool", true));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, stack -> stack.is(Items.WHEAT), false));
        this.goalSelector.addGoal(4, new YakGrazeGoal(this));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(Items.WHEAT);
    }

    public class YakGrazeGoal extends Goal {

        private final UntameableYakEntity yak;
        private int eatTime;

        public YakGrazeGoal(UntameableYakEntity yak) {
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
                ItemStack wool = new ItemStack(ModRegistries.BROWN_YAK_WOOL_ITEM.get(), 2);
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

            if (this.isGrazing())
                return state.setAndContinue(GRAZE);

            if (state.isMoving())
                return state.setAndContinue(WALK);

            return state.setAndContinue(IDLE);
        }));

        controllers.add(new AnimationController<UntameableYakEntity>("reaction", 0, state -> {
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
}
