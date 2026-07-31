package com.sappyeddie.nomadcaravans.neoforge.tent.client;

import com.geckolib.renderer.base.GeoRenderState;
import com.sappyeddie.nomadcaravans.tent.block.YurtTentBlockEntity;
import com.sappyeddie.nomadcaravans.neoforge.tent.client.TentBlockRenderer;
import com.sappyeddie.nomadcaravans.neoforge.tent.client.YurtTentModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class YurtTentRenderer<R extends BlockEntityRenderState & GeoRenderState>
        extends TentBlockRenderer<YurtTentBlockEntity, R> {

    public YurtTentRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx, new YurtTentModel());
    }
}
