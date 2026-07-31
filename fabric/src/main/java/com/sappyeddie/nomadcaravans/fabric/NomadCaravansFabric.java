package com.sappyeddie.nomadcaravans.fabric;

import com.sappyeddie.nomadcaravans.ModRegistries;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.entity.*;
import com.sappyeddie.nomadcaravans.platform.NomadPlatform;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.npc.villager.Villager;

public final class NomadCaravansFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NomadPlatform.blockEntityTypeFactory = (factory, block) ->
                FabricBlockEntityTypeBuilder.create(factory::create, block).build();
        NomadCaravans.init();


        FabricDefaultAttributeRegistry.register(ModRegistries.YAK.get(), YakEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModRegistries.WILD_YAK.get(), WildYakEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModRegistries.BABY_YAK.get(), BabyYakEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModRegistries.WANDERING_TRADER_YAK.get(), YakEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModRegistries.CARAVAN_LEADER.get(), Villager.createAttributes());
        FabricDefaultAttributeRegistry.register(ModRegistries.CARAVAN_FOLLOWER.get(), Villager.createAttributes());
        FabricDefaultAttributeRegistry.register(ModRegistries.CARAVAN_GUARD.get(), CaravanGuardEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModRegistries.BANDIT.get(), BanditEntity.createAttributes());

        FabricDefaultAttributeRegistry.register(ModRegistries.UNTAMEABLE_YAK.get(),
                com.sappyeddie.nomadcaravans.entity.UntameableYakEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModRegistries.UNTAMEABLE_WILD_YAK.get(),
                com.sappyeddie.nomadcaravans.entity.UntameableWildYakEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModRegistries.UNTAMEABLE_WANDERING_TRADER_YAK.get(),
                com.sappyeddie.nomadcaravans.entity.UntameableWanderingTraderYakEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModRegistries.UNTAMEABLE_BABY_YAK.get(),
                com.sappyeddie.nomadcaravans.entity.UntameableYakEntity.createAttributes());
    }
}