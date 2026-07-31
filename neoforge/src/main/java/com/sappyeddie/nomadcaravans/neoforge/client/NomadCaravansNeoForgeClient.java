package com.sappyeddie.nomadcaravans.neoforge.client;

import com.sappyeddie.nomadcaravans.ModRegistries;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.client.render.BabyYakRenderer;
import com.sappyeddie.nomadcaravans.client.render.CaravanGuardRenderer;
import com.sappyeddie.nomadcaravans.client.render.NomadGuardModel;
import com.sappyeddie.nomadcaravans.client.render.NomadModelLayers;
import com.sappyeddie.nomadcaravans.client.render.UntameableWanderingTraderYakRenderer;
import com.sappyeddie.nomadcaravans.client.render.UntameableWildYakRenderer;
import com.sappyeddie.nomadcaravans.client.render.UntameableYakRenderer;
import com.sappyeddie.nomadcaravans.client.render.WanderingTraderYakRenderer;
import com.sappyeddie.nomadcaravans.client.render.WildYakRenderer;
import com.sappyeddie.nomadcaravans.client.render.YakRenderer;
import com.sappyeddie.nomadcaravans.client.render.BanditRenderer;
import com.sappyeddie.nomadcaravans.neoforge.tent.client.TipiTentRenderer;
import com.sappyeddie.nomadcaravans.neoforge.tent.client.YurtTentRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = NomadCaravans.MOD_ID, dist = Dist.CLIENT)
public final class NomadCaravansNeoForgeClient {
    public NomadCaravansNeoForgeClient(IEventBus modBus) {
        modBus.addListener(this::onRegisterLayerDefinitions);
        modBus.addListener(this::onRegisterRenderers);

        dev.architectury.event.events.client.ClientTickEvent.CLIENT_POST.register(
                com.sappyeddie.nomadcaravans.client.NomadDynamicLights::tick);
    }

    private void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(NomadModelLayers.NOMAD_GUARD, NomadGuardModel::createBodyLayer);
        event.registerLayerDefinition(NomadModelLayers.NOMAD_GUARD_HEAD, NomadGuardModel::createHeadLayer);

    }

    private void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModRegistries.YAK.get(), YakRenderer::new);
        event.registerEntityRenderer(ModRegistries.WILD_YAK.get(), WildYakRenderer::new);
        event.registerEntityRenderer(ModRegistries.BABY_YAK.get(), BabyYakRenderer::new);
        event.registerEntityRenderer(ModRegistries.WANDERING_TRADER_YAK.get(), WanderingTraderYakRenderer::new);
        event.registerEntityRenderer(ModRegistries.CARAVAN_LEADER.get(), VillagerRenderer::new);
        event.registerEntityRenderer(ModRegistries.CARAVAN_FOLLOWER.get(), VillagerRenderer::new);
        event.registerEntityRenderer(ModRegistries.CARAVAN_GUARD.get(), CaravanGuardRenderer::new);
        event.registerEntityRenderer(ModRegistries.BANDIT.get(), BanditRenderer::new);
        event.registerEntityRenderer(ModRegistries.UNTAMEABLE_YAK.get(), UntameableYakRenderer::new);
        event.registerEntityRenderer(ModRegistries.UNTAMEABLE_WILD_YAK.get(), UntameableWildYakRenderer::new);
        event.registerEntityRenderer(ModRegistries.UNTAMEABLE_WANDERING_TRADER_YAK.get(),
                UntameableWanderingTraderYakRenderer::new);
        event.registerEntityRenderer(ModRegistries.UNTAMEABLE_BABY_YAK.get(),
                com.sappyeddie.nomadcaravans.client.render.UntameableBabyYakRenderer::new);

        event.registerBlockEntityRenderer(ModRegistries.YURT_TENT_BE.get(), YurtTentRenderer::new);
        event.registerBlockEntityRenderer(ModRegistries.TIPI_TENT_BE.get(), TipiTentRenderer::new);
    }
}