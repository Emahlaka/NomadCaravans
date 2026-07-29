package com.sappyeddie.nomadcaravans.trade;

import com.google.common.collect.ImmutableSet;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;

public final class ModVillagerProfessions {
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS =
            DeferredRegister.create(NomadCaravans.MOD_ID, Registries.VILLAGER_PROFESSION);

    private static final Int2ObjectMap<ResourceKey<TradeSet>> TRADE_SETS_BY_LEVEL = Int2ObjectMap.ofEntries(
            Int2ObjectMap.entry(1, ResourceKey.create(Registries.TRADE_SET, NomadCaravans.id("nomad_merchant/level_1"))));

    public static final RegistrySupplier<VillagerProfession> NOMAD_MERCHANT = VILLAGER_PROFESSIONS.register(
            "nomad_merchant",
            () -> new VillagerProfession(
                    Component.translatable("entity.nomadcaravans.villager.nomad_merchant"),
                    poiState -> true,
                    poiState -> true,
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    null,
                    TRADE_SETS_BY_LEVEL));

    private ModVillagerProfessions() {
    }

    public static void init() {
        VILLAGER_PROFESSIONS.register();
    }
}
