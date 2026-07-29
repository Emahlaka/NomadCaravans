package com.sappyeddie.nomadcaravans;

import com.sappyeddie.nomadcaravans.entity.WanderingTraderYakEntity;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;

import net.minecraft.world.entity.animal.equine.TraderLlama;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class WanderingTraderYakEscortHandler {

    private static final float ESCORT_CHANCE = 0.2F;

    private static final int DEED_PRICE_EMERALDS = 24;

    private static final Set<UUID> seenTraders = new HashSet<>();

    private WanderingTraderYakEscortHandler() {
    }

    public static void init() {
        TickEvent.SERVER_LEVEL_POST.register(WanderingTraderYakEscortHandler::onLevelTick);

        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (entity instanceof WanderingTrader trader) {
                dropDeedIfUnsold(trader);
            }
            return EventResult.pass();
        });
    }

    private static void onLevelTick(ServerLevel level) {
        for (WanderingTrader trader : level.getEntities(EntityType.WANDERING_TRADER, t -> true)) {
            if (!seenTraders.add(trader.getUUID())) continue;
            maybeGiveYakEscort(level, trader);
        }
    }

    private static void maybeGiveYakEscort(ServerLevel level, WanderingTrader trader) {
        if (trader.getRandom().nextFloat() >= ESCORT_CHANCE) return;

        List<TraderLlama> llamas = level.getEntitiesOfClass(
                TraderLlama.class,
                trader.getBoundingBox().inflate(16.0),
                llama -> llama.getLeashHolder() == trader
        );
        for (TraderLlama llama : llamas) {
            llama.discard();
        }

        WanderingTraderYakEntity yak = ModRegistries.WANDERING_TRADER_YAK.get().create(level, EntitySpawnReason.EVENT);
        if (yak == null) return;

        double offsetAngle = trader.getRandom().nextDouble() * Math.PI * 2.0;
        double offsetX = Math.cos(offsetAngle) * 2.0;
        double offsetZ = Math.sin(offsetAngle) * 2.0;
        yak.snapTo(trader.getX() + offsetX, trader.getY(), trader.getZ() + offsetZ, trader.getYRot(), 0.0F);
        level.addFreshEntity(yak);

        yak.setLeashedTo(trader, true);

        ItemStack deed = WanderingTraderYakEntity.createDeed(yak);
        MerchantOffer deedOffer = new MerchantOffer(
                new ItemCost(Items.EMERALD, DEED_PRICE_EMERALDS),
                deed,
                1,
                4,
                0.2F
        );
        trader.getOffers().add(0, deedOffer);
    }

    private static void dropDeedIfUnsold(WanderingTrader trader) {
        if (!(trader.level() instanceof ServerLevel serverLevel)) return;

        boolean hasUnsoldDeed = trader.getOffers().stream()
                .anyMatch(offer -> offer.getResult().is(ModRegistries.YAK_DEED.get()) && !offer.isOutOfStock());
        if (!hasUnsoldDeed) return;

        List<WanderingTraderYakEntity> escortYaks = serverLevel.getEntitiesOfClass(
                WanderingTraderYakEntity.class,
                trader.getBoundingBox().inflate(16.0),
                yak -> yak.getLeashHolder() == trader
        );
        if (escortYaks.isEmpty()) return;

        trader.spawnAtLocation(serverLevel, WanderingTraderYakEntity.createDeed(escortYaks.get(0)));
    }
}
