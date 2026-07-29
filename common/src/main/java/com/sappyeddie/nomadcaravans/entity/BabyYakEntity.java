package com.sappyeddie.nomadcaravans.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import com.sappyeddie.nomadcaravans.ModRegistries;
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

public class BabyYakEntity extends YakEntity {

    private static final String BREED_YAK = "yak";
    private static final String BREED_WILD = "wild_yak";
    private static final String BREED_TRADER = "wandering_trader_yak";

    private String parentBreedA = BREED_YAK;
    private String parentBreedB = BREED_YAK;

    public BabyYakEntity(EntityType<? extends YakEntity> entityType, Level level) {
        super(entityType, level);
    }

    public void setParents(Entity parentA, Entity parentB) {
        this.parentBreedA = breedKeyOf(parentA);
        this.parentBreedB = breedKeyOf(parentB);
    }

    private static String breedKeyOf(@Nullable Entity entity) {

        if (entity instanceof WildYakEntity) return BREED_WILD;
        if (entity instanceof WanderingTraderYakEntity) return BREED_TRADER;

        if (entity instanceof UntameableWildYakEntity) return BREED_WILD;
        if (entity instanceof UntameableWanderingTraderYakEntity) return BREED_TRADER;
        return BREED_YAK;
    }

    private static EntityType<?> breedType(String key) {
        return switch (key) {
            case BREED_WILD -> ModRegistries.WILD_YAK.get();
            case BREED_TRADER -> ModRegistries.WANDERING_TRADER_YAK.get();
            default -> ModRegistries.YAK.get();
        };
    }

    private EntityType<?> pickAdultBreed() {
        String chosen = this.random.nextBoolean() ? this.parentBreedA : this.parentBreedB;
        return breedType(chosen);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("ParentBreedA", this.parentBreedA);
        output.putString("ParentBreedB", this.parentBreedB);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.parentBreedA = input.getStringOr("ParentBreedA", BREED_YAK);
        this.parentBreedB = input.getStringOr("ParentBreedB", BREED_YAK);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            if (this.getAge() < -1) {
                this.setAge(this.getAge() + 1);
            } else {
                growUp();
            }
        }
    }

    private void growUp() {
        if (!(this.level() instanceof ServerLevel serverLevel))
            return;

        if (!(pickAdultBreed().create(serverLevel, EntitySpawnReason.CONVERSION) instanceof Animal adult)) {
            return;
        }

        adult.snapTo(
                this.getX(),
                this.getY(),
                this.getZ(),
                this.getYRot(),
                this.getXRot()
        );

        adult.setHealth(this.getHealth());

        UUID owner = this.getOwnerUUID();
        if (owner != null) {
            if (adult instanceof WildYakEntity wild) {
                wild.setOwnerUUID(owner);
            } else if (adult instanceof WanderingTraderYakEntity trader) {
                trader.setOwnerUUID(owner);
            } else if (adult instanceof YakEntity yak) {
                yak.setOwnerUUID(owner);
            }
        }

        if (this.hasCustomName()) {
            adult.setCustomName(this.getCustomName());
            adult.setCustomNameVisible(this.isCustomNameVisible());
        }

        if (this.isPersistenceRequired()) {
            adult.setPersistenceRequired();
        }

        adult.setNoAi(this.isNoAi());

        adult.setInvulnerable(this.isInvulnerable());
        adult.setSilent(this.isSilent());
        adult.setNoGravity(this.isNoGravity());
        adult.setGlowingTag(this.isCurrentlyGlowing());

        adult.setRemainingFireTicks(this.getRemainingFireTicks());

        adult.setDeltaMovement(this.getDeltaMovement());

        serverLevel.addFreshEntity(adult);

        this.discard();
    }
}
