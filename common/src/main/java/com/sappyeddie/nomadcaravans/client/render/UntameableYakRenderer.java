package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.sappyeddie.nomadcaravans.entity.UntameableYakEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class UntameableYakRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<UntameableYakEntity, R> {

    private static final DataTicket<Boolean> HAS_WOOL =
            DataTicket.create("nomadcaravans_untameable_yak_has_wool", Boolean.class);

    public UntameableYakRenderer(EntityRendererProvider.Context context) {
        super(context, new UntameableYakModel());
    }

    @Override
    public void addRenderData(UntameableYakEntity animatable, Void relatedObject, R renderState, float partialTick) {
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
    }
}
