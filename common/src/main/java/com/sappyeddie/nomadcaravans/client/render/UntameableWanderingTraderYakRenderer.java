package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.sappyeddie.nomadcaravans.entity.UntameableWanderingTraderYakEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class UntameableWanderingTraderYakRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<UntameableWanderingTraderYakEntity, R> {

    private static final String[] LANTERN_RIG =
            {"lanternattach", "stick", "stick2", "rope_physics", "Lantern", "Glow"};

    private static final DataTicket<Boolean> HAS_WOOL =
            DataTicket.create("nomadcaravans_untameable_trader_yak_has_wool", Boolean.class);

    public UntameableWanderingTraderYakRenderer(EntityRendererProvider.Context context) {
        super(context, new UntameableWanderingTraderYakModel());
    }

    @Override
    public void addRenderData(UntameableWanderingTraderYakEntity animatable, Void relatedObject, R renderState,
                              float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(HAS_WOOL, animatable.hasWool());
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        boolean hasWool = renderPassInfo.getGeckolibData(HAS_WOOL);
        snapshots.ifPresent("wool", boneSnapshot -> boneSnapshot.skipRender(!hasWool));

        snapshots.ifPresent("chests", boneSnapshot -> boneSnapshot.skipRender(true));
        snapshots.ifPresent("saddle", boneSnapshot -> boneSnapshot.skipRender(true));
        snapshots.ifPresent("fabric", boneSnapshot -> boneSnapshot.skipRender(true));
        for (String bone : LANTERN_RIG) {
            snapshots.ifPresent(bone, boneSnapshot -> boneSnapshot.skipRender(true));
        }
    }
}
