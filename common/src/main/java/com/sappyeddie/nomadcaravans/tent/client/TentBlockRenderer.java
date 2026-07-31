package com.sappyeddie.nomadcaravans.tent.client;

import com.geckolib.constant.dataticket.DataTicket;
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
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class TentBlockRenderer<T extends TentBlockEntity, R extends BlockEntityRenderState & GeoRenderState>
        extends GeoBlockRenderer<T, R> {

    private static final Identifier FRAME_TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/block/stripped_oak_log.png");

    private static final Identifier BANDIT_FRAME_TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/block/stripped_dark_oak_log.png");

    public static final DataTicket<Boolean> BANDIT =
            DataTicket.create("nomadcaravans_tent_bandit", Boolean.class);

    protected TentBlockRenderer(BlockEntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);

        withRenderLayer(new RecursiveBoneRenderLayer<T, Void, R>(this, "frame") {
            @Override
            protected Identifier getTexture(R renderState) {
                boolean bandit = Boolean.TRUE.equals(renderState.getGeckolibData(BANDIT));
                return bandit ? BANDIT_FRAME_TEXTURE : FRAME_TEXTURE;
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
    public void addRenderData(T animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(BANDIT, animatable.isBandit());
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


    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
    @Override
   public int getViewDistance() {
        return 128;
    }
    @Override
    public boolean shouldRender(final T blockEntity, final Vec3 cameraPosition) {
        return true;
    }
}