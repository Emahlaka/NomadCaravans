package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.sappyeddie.nomadcaravans.entity.UntameableWildYakEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class UntameableWildYakRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<UntameableWildYakEntity, R> {

    private static final String[] LANTERN_RIG = {"saddle2", "saddle3", "saddle4", "rope_physics"};

    private static final DataTicket<Boolean> HAS_WOOL =
            DataTicket.create("nomadcaravans_untameable_wild_yak_has_wool", Boolean.class);

    public UntameableWildYakRenderer(EntityRendererProvider.Context context) {
        super(context, new UntameableWildYakModel());
    }

    @Override
    public void addRenderData(UntameableWildYakEntity animatable, Void relatedObject, R renderState, float partialTick) {
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
