package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.sappyeddie.nomadcaravans.entity.UntameableBabyYakEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class UntameableBabyYakRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<UntameableBabyYakEntity, R> {

    private static final String[] HIDDEN_BONES =
            {"chests", "saddle", "saddle2", "saddle3", "saddle4", "fabric", "rope_physics",
             "lanternattach", "stick", "stick2", "Lantern", "Glow"};

    public UntameableBabyYakRenderer(EntityRendererProvider.Context context) {
        super(context, new UntameableBabyYakModel());
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        for (String bone : HIDDEN_BONES) {
            snapshots.ifPresent(bone, boneSnapshot -> boneSnapshot.skipRender(true));
        }
    }
}
