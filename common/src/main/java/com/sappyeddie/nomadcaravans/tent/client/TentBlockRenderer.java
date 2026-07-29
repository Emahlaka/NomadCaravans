package com.sappyeddie.nomadcaravans.tent.client;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.sappyeddie.nomadcaravans.tent.block.TentBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public abstract class TentBlockRenderer<T extends TentBlockEntity, R extends BlockEntityRenderState & GeoRenderState>
        extends GeoBlockRenderer<T, R> {

    private static final Identifier FRAME_TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/block/stripped_oak_log.png");

    protected TentBlockRenderer(BlockEntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);

        withRenderLayer(new RecursiveBoneRenderLayer<T, Void, R>(this, "frame") {
            @Override
            protected Identifier getTexture(R renderState) {
                return FRAME_TEXTURE;
            }

            @Override
            protected RenderType getRenderType(R renderState, Identifier texture) {
                return RenderTypes.entityCutout(texture);
            }

            @Override
            protected int getColor(R renderState) {
                return 0xFFFFFFFF;
            }
        });
    }

    protected double getCoreDropDistance() {
        return 3.0;
    }

    @Override
    public int getRenderColor(T animatable, @Nullable Void relatedObject, float partialTick) {
        return 0xFFFFFFFF;
    }

    @Override
    public void preRenderPass(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);
        renderPassInfo.poseStack().translate(0, -getCoreDropDistance(), 0);
    }

    public AABB getRenderBoundingBox(T blockEntity) {
        return new AABB(blockEntity.getBlockPos())
                .expandTowards(0.0, -getCoreDropDistance(), 0.0)
                .inflate(3.0);
    }
}
