package com.sappyeddie.nomadcaravans.tent.client;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.sappyeddie.nomadcaravans.tent.block.TentBlock;
import com.sappyeddie.nomadcaravans.tent.block.TipiTentBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jspecify.annotations.Nullable;

public class TipiTentRenderer<R extends BlockEntityRenderState & GeoRenderState>
        extends TentBlockRenderer<TipiTentBlockEntity, R> {

    private static final DataTicket<Boolean> DOOR_OPEN =
            DataTicket.create("nomadcaravans_tipi_door_open", Boolean.class);

    public TipiTentRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx, new TipiTentModel());
    }

    @Override
    public void addRenderData(TipiTentBlockEntity animatable, @Nullable Void relatedObject,
                              R renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        boolean open = animatable.getBlockState().hasProperty(TentBlock.OPEN)
                && animatable.getBlockState().getValue(TentBlock.OPEN);
        renderState.addGeckolibData(DOOR_OPEN, open);
    }

    @Override
    public void preRenderPass(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);
        boolean open = Boolean.TRUE.equals(renderPassInfo.renderState().getGeckolibData(DOOR_OPEN));
        String hide = open ? "fabric_doorClosed" : "fabric_doorOpen";
        renderPassInfo.addBoneUpdater((info, snapshots) ->
                snapshots.get(hide).ifPresent(snapshot -> {
                    snapshot.skipRender(true);
                    snapshot.skipChildrenRender(true);
                }));
    }
}
