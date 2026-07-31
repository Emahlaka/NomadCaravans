package com.sappyeddie.nomadcaravans.tent.block;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public class TentBlockEntity extends BlockEntity implements GeoBlockEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Nullable
    private UUID ownerUUID = null;
    @Nullable
    private Component customName = null;

    private boolean campLocked = false;

    private boolean bandit = false;

    int doorCheckTimer = 0;

    boolean autoOpenedDoor = false;

    boolean doorwaysMarked = false;

    public TentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public @Nullable UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        markAndSync();
    }

    public @Nullable Component getCustomName() {
        return this.customName;
    }

    public void setCustomName(@Nullable Component customName) {
        this.customName = customName;
        markAndSync();
    }

    public boolean isCampLocked() {
        return this.campLocked;
    }

    public void setCampLocked(boolean campLocked) {
        this.campLocked = campLocked;
        setChanged();
    }

    public boolean isBandit() {
        return this.bandit;
    }

    public void setBandit(boolean bandit) {
        this.bandit = bandit;
        markAndSync();
    }

    private void markAndSync() {
        setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.ownerUUID != null) {
            output.putString("OwnerUUID", this.ownerUUID.toString());
        }
        if (this.customName != null) {
            output.store("CustomName", ComponentSerialization.CODEC, this.customName);
        }
        if (this.campLocked) {
            output.putBoolean("CampLocked", true);
        }
        if (this.bandit) {
            output.putBoolean("Bandit", true);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getString("OwnerUUID").ifPresent(uuidStr -> {
            try {
                this.ownerUUID = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                this.ownerUUID = null;
            }
        });
        input.read("CustomName", ComponentSerialization.CODEC).ifPresent(c -> this.customName = c);
        this.campLocked = input.getBooleanOr("CampLocked", false);
        this.bandit = input.getBooleanOr("Bandit", false);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        if (this.ownerUUID != null) {
            tag.putString("OwnerUUID", this.ownerUUID.toString());
        }
        if (this.bandit) {
            tag.putBoolean("Bandit", true);
        }
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /* === OFFSCREEN RENDERING TRACKER === */
    private static final Set<TentBlockEntity> ALL_RENDERABLE_TENTS = Collections.newSetFromMap(new WeakHashMap<>());

    // onLoad() removed from BlockEntity in MC 26.x; called manually from MyGeoBlock if needed
    public void onLoad() {
        if (this.level != null && this.level.isClientSide()) {
            ALL_RENDERABLE_TENTS.add(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.level != null && this.level.isClientSide()) {
            ALL_RENDERABLE_TENTS.remove(this);
        }
    }

    public static Set<TentBlockEntity> getAllTents() {
        return ALL_RENDERABLE_TENTS;
    }
}

