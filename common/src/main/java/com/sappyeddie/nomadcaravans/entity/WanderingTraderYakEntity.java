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
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class WanderingTraderYakEntity extends Animal implements GeoEntity {

    private static final EntityDataAccessor<Boolean> DATA_HAS_CHEST =
            SynchedEntityData.defineId(WanderingTraderYakEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_CARPET_COLOR =
            SynchedEntityData.defineId(WanderingTraderYakEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_SADDLED =
            SynchedEntityData.defineId(WanderingTraderYakEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<String> DATA_OWNER_UUID =
            SynchedEntityData.defineId(WanderingTraderYakEntity.class, EntityDataSerializers.STRING);

    private static final int CHEST_INVENTORY_SIZE = 27;

    private static final String DEED_YAK_UUID_MOST_KEY = "BoundYakMost";
    private static final String DEED_YAK_UUID_LEAST_KEY = "BoundYakLeast";
    private static final EntityDataAccessor<Boolean> DATA_HAS_WOOL =
            SynchedEntityData.defineId(WanderingTraderYakEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> DATA_GRAZING =
            SynchedEntityData.defineId(WanderingTraderYakEntity.class, EntityDataSerializers.BOOLEAN);

    private final SimpleContainer chestInventory = new SimpleContainer(CHEST_INVENTORY_SIZE);

    private static final String CARPET_SUFFIX = "_carpet";

    @Nullable
    private static DyeColor getCarpetColor(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());

        if (id == null || !id.getNamespace().equals("minecraft") || !id.getPath().endsWith(CARPET_SUFFIX))
            return null;

        String colorName = id.getPath().substring(0, id.getPath().length() - CARPET_SUFFIX.length());

        return DyeColor.byName(colorName, null);
    }

    protected void showTamedHearts() {
        this.level().broadcastEntityEvent(this, (byte) 18);
    }

    private static boolean isRightOfOwnership(ItemStack stack) {
        return stack.is(ModRegistries.RIGHT_OF_YAK_OWNERSHIP.get());
    }

    public static ItemStack createDeed(WanderingTraderYakEntity wandering_trader_yak) {
        ItemStack deed = new ItemStack(ModRegistries.YAK_DEED.get());
        UUID wandering_trader_yakId = wandering_trader_yak.getUUID();
        CompoundTag tag = new CompoundTag();
        tag.putLong(DEED_YAK_UUID_MOST_KEY, wandering_trader_yakId.getMostSignificantBits());
        tag.putLong(DEED_YAK_UUID_LEAST_KEY, wandering_trader_yakId.getLeastSignificantBits());
        deed.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return deed;
    }

    @Nullable
    private static UUID getDeedYakUUID(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (!tag.contains(DEED_YAK_UUID_MOST_KEY) || !tag.contains(DEED_YAK_UUID_LEAST_KEY))
            return null;

        long most = tag.getLong(DEED_YAK_UUID_MOST_KEY).orElse(0L);
        long least = tag.getLong(DEED_YAK_UUID_LEAST_KEY).orElse(0L);
        return new UUID(most, least);
    }

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.wandering_trader_yak.breathing");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.wandering_trader_yak.walk");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.wandering_trader_yak.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.wandering_trader_yak.death");
    private static final RawAnimation GRAZE =
            RawAnimation.begin().thenLoop("animation.wandering_trader_yak.grazing");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public WanderingTraderYakEntity(EntityType<? extends WanderingTraderYakEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HAS_CHEST, true);
        builder.define(DATA_CARPET_COLOR, (byte) -1);
        builder.define(DATA_SADDLED, true);
        builder.define(DATA_OWNER_UUID, "");
        builder.define(DATA_HAS_WOOL, true);
        builder.define(DATA_GRAZING, false);
    }

    public boolean hasChest() {
        return this.entityData.get(DATA_HAS_CHEST);
    }

    public void setChest(boolean hasChest) {
        this.entityData.set(DATA_HAS_CHEST, hasChest);
    }

    public boolean hasCarpet() {
        return this.entityData.get(DATA_CARPET_COLOR) >= 0;
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

    public void setCarpetColor(@Nullable DyeColor color) {
        this.entityData.set(DATA_CARPET_COLOR, (byte) (color == null ? -1 : color.getId()));
    }

    public boolean isSaddled() {
        return this.entityData.get(DATA_SADDLED);
    }

    public void setSaddled(boolean saddled) {
        this.entityData.set(DATA_SADDLED, saddled);
    }

    public boolean isTamed() {
        return !this.entityData.get(DATA_OWNER_UUID).isEmpty();
    }

    @Nullable
    public UUID getOwnerUUID() {
        String raw = this.entityData.get(DATA_OWNER_UUID);
        return raw.isEmpty() ? null : UUID.fromString(raw);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, uuid == null ? "" : uuid.toString());
    }

    public boolean isOwnedBy(Player player) {
        return this.isTamed() && player.getUUID().equals(this.getOwnerUUID());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("HasChest", this.hasChest());
        output.putInt("CarpetColor", this.entityData.get(DATA_CARPET_COLOR));
        output.putBoolean("Saddled", this.isSaddled());
        output.storeNullable("Owner", UUIDUtil.CODEC, this.getOwnerUUID());
        output.putBoolean("HasWool", this.hasWool());

        if (this.hasChest()) {
            NonNullList<ItemStack> items = NonNullList.withSize(CHEST_INVENTORY_SIZE, ItemStack.EMPTY);
            for (int i = 0; i < CHEST_INVENTORY_SIZE; i++) {
                items.set(i, this.chestInventory.getItem(i));
            }
            ContainerHelper.saveAllItems(output, items);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setChest(input.getBooleanOr("HasChest", true));
        this.entityData.set(DATA_CARPET_COLOR, (byte) input.getIntOr("CarpetColor", -1));
        this.setSaddled(input.getBooleanOr("Saddled", true));
        this.setOwnerUUID(input.read("Owner", UUIDUtil.CODEC).orElse(null));
        this.setWool(input.getBooleanOr("HasWool", true));

        if (this.hasChest()) {
            NonNullList<ItemStack> items = NonNullList.withSize(CHEST_INVENTORY_SIZE, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(input, items);
            for (int i = 0; i < CHEST_INVENTORY_SIZE; i++) {
                this.chestInventory.setItem(i, items.get(i));
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, stack -> stack.is(Items.WHEAT), false));
        this.goalSelector.addGoal(4, new WanderingTraderYakGrazeGoal(this));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(Items.WHEAT);
    }

    public class WanderingTraderYakGrazeGoal extends Goal {

        private final WanderingTraderYakEntity yak;
        private int eatTime;

        public WanderingTraderYakGrazeGoal(WanderingTraderYakEntity yak) {
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
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Items.SHEARS)) {
            if (!this.level().isClientSide() && this.hasWool()) {

                ItemStack wool = new ItemStack(ModRegistries.WHITE_YAK_WOOL_ITEM.get(), 4);

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

        if (isRightOfOwnership(stack) && !this.isTamed()) {
            if (!this.level().isClientSide()) {
                this.setOwnerUUID(player.getUUID());
                stack.consume(1, player);
                showTamedHearts();
            }
            return InteractionResult.SUCCESS;
        }

        if (stack.is(Items.HAY_BLOCK) && !this.isTamed()) {
            if (!this.level().isClientSide()) {
                this.setOwnerUUID(player.getUUID());
                stack.consume(1, player);
                showTamedHearts();
            }
            return InteractionResult.SUCCESS;
        }

        UUID deedTarget = getDeedYakUUID(stack);
        if (deedTarget != null && deedTarget.equals(this.getUUID())) {
            if (!this.level().isClientSide()) {
                this.setOwnerUUID(player.getUUID());
                stack.consume(1, player);
            }
            return InteractionResult.SUCCESS;
        }

        if (stack.is(Items.WRITABLE_BOOK) && this.isOwnedBy(player)) {
            if (!this.level().isClientSide()) {
                stack.shrink(1);
                ItemStack deed = createDeed(this);
                if (!player.getInventory().add(deed)) {
                    player.drop(deed, false);
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (!this.isBaby()) {
            if (!this.hasChest() && stack.is(Items.CHEST)) {
                if (!this.isOwnedBy(player))
                    return InteractionResult.FAIL;

                this.setChest(true);
                this.playChestEquipSound();
                stack.consume(1, player);
                return InteractionResult.SUCCESS;
            }

            DyeColor carpetColor = getCarpetColor(stack);

            if (carpetColor != null) {
                if (!this.isOwnedBy(player))
                    return InteractionResult.FAIL;

                this.setCarpetColor(carpetColor);
                this.playCarpetEquipSound();
                stack.consume(1, player);
                return InteractionResult.SUCCESS;
            }

            if (!this.isSaddled() && stack.is(Items.SADDLE)) {
                if (!this.isOwnedBy(player))
                    return InteractionResult.FAIL;

                this.setSaddled(true);
                this.playSaddleEquipSound();
                stack.consume(1, player);
                return InteractionResult.SUCCESS;
            }

            if (this.hasChest() && player.isSecondaryUseActive()) {
                if (!this.isOwnedBy(player))
                    return InteractionResult.FAIL;

                if (!this.level().isClientSide()) {
                    player.openMenu(new SimpleMenuProvider(
                            (containerId, playerInventory, p) -> ChestMenu.threeRows(containerId, playerInventory, this.chestInventory),
                            this.getDisplayName()));
                }
                return InteractionResult.SUCCESS;
            }

            if (this.isSaddled() && stack.isEmpty() && !this.isVehicle()) {
                if (!this.isOwnedBy(player))
                    return InteractionResult.FAIL;

                if (!this.level().isClientSide()) {
                    player.startRiding(this);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    private void playChestEquipSound() {
        this.playSound(SoundEvents.DONKEY_CHEST, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
    }

    private void playCarpetEquipSound() {
        this.playSound(SoundEvents.LLAMA_SWAG.value(), 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
    }

    private void playSaddleEquipSound() {
        this.playSound(SoundEvents.HORSE_SADDLE.value(), 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
    }

    @Override
    protected void dropEquipment(ServerLevel level) {
        super.dropEquipment(level);
        if (this.hasChest()) {
            this.spawnAtLocation(level, Blocks.CHEST);
            Containers.dropContents(level, this, this.chestInventory);
            this.setChest(false);
        }
        if (this.isSaddled()) {
            this.spawnAtLocation(level, Items.SADDLE);
            this.setSaddled(false);
        }
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
    public @Nullable LivingEntity getControllingPassenger() {
        Entity passenger = this.getFirstPassenger();
        return this.isSaddled() && passenger instanceof Player player ? player : null;
    }

    @Override
    public void travel(Vec3 movementInput) {

        if (this.isGrazing()) {
            super.travel(Vec3.ZERO);
            return;
        }
        if (this.isAlive() && this.isVehicle() && this.isSaddled()
                && this.getControllingPassenger() instanceof Player controller) {
            this.setYRot(controller.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(controller.getXRot() * 0.5F);
            this.setRot(this.getYRot(), this.getXRot());
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.yBodyRot;

            float strafe = controller.xxa * 0.5F;
            float forward = controller.zza;
            if (forward <= 0.0F) {
                forward *= 0.25F;
            }

            this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
            super.travel(new Vec3(strafe, movementInput.y, forward));
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
        controllers.add(new AnimationController<WanderingTraderYakEntity>("reaction", 0, state -> {
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

            if (partner instanceof WanderingTraderYakEntity wandering_trader_yakPartner) {
                UUID thisOwner = this.getOwnerUUID();
                if (thisOwner != null && thisOwner.equals(wandering_trader_yakPartner.getOwnerUUID())) {
                    baby.setOwnerUUID(thisOwner);
                }
            }
        }

        return baby;
    }

    private Vec3 lastDeltaMovement = Vec3.ZERO;
    private float ropeSwingX, ropeSwingXO, ropeSwingVelX;
    private float ropeSwingZ, ropeSwingZO, ropeSwingVelZ;

    @Nullable
    private BlockPos lanternLightPos;

    @Override
    public void tick() {
        super.tick();

        if (NomadConfig.lanternLightBlocks && this.level() instanceof ServerLevel serverLevel) {
            this.lanternLightPos = LanternLight.follow(serverLevel, this, this.lanternLightPos);
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

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.level() instanceof ServerLevel serverLevel) {
            LanternLight.clear(serverLevel, this.lanternLightPos);
            this.lanternLightPos = null;
        }
        super.remove(reason);
    }

    public float getRopeSwingX(float partialTick) {
        return Mth.lerp(partialTick, this.ropeSwingXO, this.ropeSwingX);
    }

    public float getRopeSwingZ(float partialTick) {
        return Mth.lerp(partialTick, this.ropeSwingZO, this.ropeSwingZ);
    }
}
