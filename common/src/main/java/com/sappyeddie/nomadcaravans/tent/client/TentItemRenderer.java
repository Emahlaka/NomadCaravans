package com.sappyeddie.nomadcaravans.tent.client;

import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.sappyeddie.nomadcaravans.tent.block.TentBlockItem;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class TentItemRenderer extends GeoItemRenderer<TentBlockItem> {

    private static final Identifier FRAME_TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/block/stripped_oak_log.png");

    public TentItemRenderer(Identifier model) {
        super(new TentItemModel(model));

        withRenderLayer(new RecursiveBoneRenderLayer<>(this, "frame") {
            @Override
            protected Identifier getTexture(GeoRenderState renderState) {
                return FRAME_TEXTURE;
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
    public int getRenderColor(TentBlockItem animatable, @Nullable RenderData relatedObject, float partialTick) {
        return 0xFFFFFFFF;
    }
}
