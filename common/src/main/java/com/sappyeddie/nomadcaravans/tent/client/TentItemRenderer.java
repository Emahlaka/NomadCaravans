package com.sappyeddie.nomadcaravans.tent.client;

import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.sappyeddie.nomadcaravans.tent.block.BanditTentBlockItem;
import com.sappyeddie.nomadcaravans.tent.block.TentBlockItem;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class TentItemRenderer extends GeoItemRenderer<TentBlockItem> {

    private static final Identifier FRAME_TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/block/stripped_oak_log.png");

    private static final Identifier BANDIT_FRAME_TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/block/stripped_dark_oak_log.png");

    public TentItemRenderer(Identifier model) {
        super(new TentItemModel(model));

        withRenderLayer(new RecursiveBoneRenderLayer<>(this, "frame") {
            @Override
            protected Identifier getTexture(GeoRenderState renderState) {
                boolean bandit = Boolean.TRUE.equals(renderState.getGeckolibData(TentBlockRenderer.BANDIT));
                return bandit ? BANDIT_FRAME_TEXTURE : FRAME_TEXTURE;
            }

            @Override
            protected RenderType getRenderType(GeoRenderState renderState, Identifier texture) {
                return RenderTypes.entityCutout(texture);
            }

            @Override
            protected int getColor(GeoRenderState renderState) {
                return 0xFFFFFFFF;
            }
        });
    }

    @Override
    public void addRenderData(TentBlockItem animatable, @Nullable RenderData relatedObject,
                              GeoRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(TentBlockRenderer.BANDIT, animatable instanceof BanditTentBlockItem);
    }

    @Override
    public int getRenderColor(TentBlockItem animatable, @Nullable RenderData relatedObject, float partialTick) {
        return 0xFFFFFFFF;
    }
}