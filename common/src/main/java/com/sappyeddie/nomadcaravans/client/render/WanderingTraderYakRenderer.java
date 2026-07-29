package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;

import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.sappyeddie.nomadcaravans.entity.WanderingTraderYakEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class WanderingTraderYakRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<WanderingTraderYakEntity, R> {
    private static final DataTicket<Float> ROPE_SWING_X =
            DataTicket.create("nomadcaravans_wandering_trader_yak_rope_swing_x", Float.class);
    private static final DataTicket<Float> ROPE_SWING_Z =
            DataTicket.create("nomadcaravans_wandering_trader_yak_rope_swing_z", Float.class);
    private static final DataTicket<Boolean> HAS_WOOL =
            DataTicket.create("nomadcaravans_yak_has_wool", Boolean.class);

    public WanderingTraderYakRenderer(EntityRendererProvider.Context context) {
        super(context, new WanderingTraderYakModel());
        this.withRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void addRenderData(WanderingTraderYakEntity animatable, Void relatedObject, R renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(HAS_WOOL, animatable.hasWool());
        renderState.addGeckolibData(ROPE_SWING_X, animatable.getRopeSwingX(partialTick));
        renderState.addGeckolibData(ROPE_SWING_Z, animatable.getRopeSwingZ(partialTick));
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        boolean hasWool = renderPassInfo.getGeckolibData(HAS_WOOL);
        snapshots.ifPresent("wool", boneSnapshot -> boneSnapshot.skipRender(!hasWool));

        float swingX = renderPassInfo.getGeckolibData(ROPE_SWING_X) * Mth.DEG_TO_RAD;
        float swingZ = renderPassInfo.getGeckolibData(ROPE_SWING_Z) * Mth.DEG_TO_RAD;
        snapshots.ifPresent("rope_physics", boneSnapshot -> {
            boneSnapshot.setRotX(boneSnapshot.getRotX() + swingX);
            boneSnapshot.setRotZ(boneSnapshot.getRotZ() + swingZ);
        });
    }
}