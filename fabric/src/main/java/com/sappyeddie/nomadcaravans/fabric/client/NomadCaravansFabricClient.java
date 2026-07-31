package com.sappyeddie.nomadcaravans.fabric.client;

import com.sappyeddie.nomadcaravans.ModRegistries;
import com.sappyeddie.nomadcaravans.client.render.*;
import com.sappyeddie.nomadcaravans.tent.client.TipiTentRenderer;
import com.sappyeddie.nomadcaravans.tent.client.YurtTentRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.VillagerRenderer;

public final class NomadCaravansFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        ModelLayerRegistry.registerModelLayer(NomadModelLayers.NOMAD_GUARD, NomadGuardModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(NomadModelLayers.NOMAD_GUARD_HEAD, NomadGuardModel::createHeadLayer);

        dev.architectury.event.events.client.ClientTickEvent.CLIENT_POST.register(
                com.sappyeddie.nomadcaravans.client.NomadDynamicLights::tick);


        EntityRendererRegistry.register(ModRegistries.YAK.get(), YakRenderer::new);
        EntityRendererRegistry.register(ModRegistries.WILD_YAK.get(), WildYakRenderer::new);
        EntityRendererRegistry.register(ModRegistries.BABY_YAK.get(), BabyYakRenderer::new);
        EntityRendererRegistry.register(ModRegistries.WANDERING_TRADER_YAK.get(), WanderingTraderYakRenderer::new);
        EntityRendererRegistry.register(ModRegistries.CARAVAN_LEADER.get(), VillagerRenderer::new);
        EntityRendererRegistry.register(ModRegistries.CARAVAN_FOLLOWER.get(), VillagerRenderer::new);
        EntityRendererRegistry.register(ModRegistries.CARAVAN_GUARD.get(), CaravanGuardRenderer::new);
        EntityRendererRegistry.register(ModRegistries.BANDIT.get(), BanditRenderer::new);
        EntityRendererRegistry.register(ModRegistries.UNTAMEABLE_YAK.get(), UntameableYakRenderer::new);
        EntityRendererRegistry.register(ModRegistries.UNTAMEABLE_WILD_YAK.get(), UntameableWildYakRenderer::new);
        EntityRendererRegistry.register(ModRegistries.UNTAMEABLE_WANDERING_TRADER_YAK.get(),
                UntameableWanderingTraderYakRenderer::new);
        EntityRendererRegistry.register(ModRegistries.UNTAMEABLE_BABY_YAK.get(), UntameableBabyYakRenderer::new);

        BlockEntityRenderers.register(ModRegistries.YURT_TENT_BE.get(), YurtTentRenderer::new);
        BlockEntityRenderers.register(ModRegistries.TIPI_TENT_BE.get(), TipiTentRenderer::new);
    }
}