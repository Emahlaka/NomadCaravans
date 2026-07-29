package com.sappyeddie.nomadcaravans.entity;

import com.sappyeddie.nomadcaravans.ModRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class UntameableBabyYakEntity extends UntameableYakEntity {

    public UntameableBabyYakEntity(EntityType<? extends UntameableYakEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.getAge() < -1) {
                this.setAge(this.getAge() + 1);
            } else {
                growUp();
            }
        }
    }

    private void growUp() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        UntameableWildYakEntity adult = ModRegistries.UNTAMEABLE_WILD_YAK.get()
                .create(serverLevel, EntitySpawnReason.CONVERSION);
        if (adult == null) {
            return;
        }

        adult.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        adult.setHealth(this.getHealth());
        adult.setWool(this.hasWool());

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
