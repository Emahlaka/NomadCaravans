package com.sappyeddie.nomadcaravans.neoforge;

import com.sappyeddie.nomadcaravans.ModRegistries;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.entity.BanditEntity;
import com.sappyeddie.nomadcaravans.entity.CaravanGuardEntity;
import com.sappyeddie.nomadcaravans.entity.YakEntity;
import com.sappyeddie.nomadcaravans.platform.NomadPlatform;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import java.util.Set;

@Mod(NomadCaravans.MOD_ID)
public final class NomadCaravansNeoForge {
    public NomadCaravansNeoForge(IEventBus modBus) {
        NomadPlatform.blockEntityTypeFactory = (factory, block) ->
                new BlockEntityType<>(factory, Set.of(block));
        NomadCaravans.init();

        modBus.addListener(this::onEntityAttributeCreation);
    }

    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModRegistries.YAK.get(), YakEntity.createAttributes().build());
        event.put(ModRegistries.WANDERING_TRADER_YAK.get(), YakEntity.createAttributes().build());
        event.put(ModRegistries.CARAVAN_LEADER.get(), Villager.createAttributes().build());
        event.put(ModRegistries.CARAVAN_FOLLOWER.get(), Villager.createAttributes().build());
        event.put(ModRegistries.CARAVAN_GUARD.get(), CaravanGuardEntity.createAttributes().build());
        event.put(ModRegistries.BANDIT.get(), BanditEntity.createAttributes().build());
        event.put(ModRegistries.UNTAMEABLE_YAK.get(),
                com.sappyeddie.nomadcaravans.entity.UntameableYakEntity.createAttributes().build());
        event.put(ModRegistries.UNTAMEABLE_WILD_YAK.get(),
                com.sappyeddie.nomadcaravans.entity.UntameableWildYakEntity.createAttributes().build());
        event.put(ModRegistries.UNTAMEABLE_WANDERING_TRADER_YAK.get(),
                com.sappyeddie.nomadcaravans.entity.UntameableWanderingTraderYakEntity.createAttributes().build());
        event.put(ModRegistries.UNTAMEABLE_BABY_YAK.get(),
                com.sappyeddie.nomadcaravans.entity.UntameableYakEntity.createAttributes().build());
    }
}