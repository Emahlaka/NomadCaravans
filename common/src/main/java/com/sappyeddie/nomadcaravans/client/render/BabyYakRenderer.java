package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.sappyeddie.nomadcaravans.entity.BabyYakEntity;
import com.sappyeddie.nomadcaravans.entity.YakEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class BabyYakRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<BabyYakEntity, R> {
    public BabyYakRenderer(EntityRendererProvider.Context context) {
        super(context, new BabyYakModel());
    }
}