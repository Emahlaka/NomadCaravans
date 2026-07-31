package com.sappyeddie.nomadcaravans.neoforge.tent.client;

import com.geckolib.renderer.base.GeoRenderState;
import com.sappyeddie.nomadcaravans.tent.block.TipiTentBlockEntity;
import com.sappyeddie.nomadcaravans.neoforge.tent.client.TentBlockRenderer;
import com.sappyeddie.nomadcaravans.neoforge.tent.client.TipiTentModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class TipiTentRenderer<R extends BlockEntityRenderState & GeoRenderState>
        extends TentBlockRenderer<TipiTentBlockEntity, R> {

    public TipiTentRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx, new TipiTentModel());
    }
}
