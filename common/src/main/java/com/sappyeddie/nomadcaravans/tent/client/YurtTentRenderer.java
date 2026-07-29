package com.sappyeddie.nomadcaravans.tent.client;

import com.geckolib.renderer.base.GeoRenderState;
import com.sappyeddie.nomadcaravans.tent.block.YurtTentBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class YurtTentRenderer<R extends BlockEntityRenderState & GeoRenderState>
        extends TentBlockRenderer<YurtTentBlockEntity, R> {

    public YurtTentRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx, new YurtTentModel());
    }
}
