package com.sappyeddie.nomadcaravans.item;

import com.sappyeddie.nomadcaravans.entity.WildYakEntity;
import com.sappyeddie.nomadcaravans.entity.YakEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;

public class WildYakSpawnEggItem extends SpawnEggItem {
    public WildYakSpawnEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult result = super.useOn(context);

        if (result.consumesAction() && !context.getLevel().isClientSide() && context.getPlayer() != null) {
            BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());

            context.getLevel().getEntitiesOfClass(WildYakEntity.class, new AABB(spawnPos).inflate(1.0)).stream()
                    .filter(wildyak -> !wildyak.isTamed())
                    .findFirst()
                    .ifPresent(wildyak -> wildyak.setOwnerUUID(context.getPlayer().getUUID()));
        }

        return result;
    }
}