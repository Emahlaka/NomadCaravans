package com.sappyeddie.nomadcaravans.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/**
 * Draws the head-only NomadGuardModel (baked from
 * NomadModelLayers.NOMAD_GUARD_HEAD) with its own texture, completely
 * independent of whatever texture the base renderer uses for the body.
 *
 * The parent renderer's model must have its head bone stripped out
 * (see NomadGuardModel#createBodyLayer) so the two textures never draw
 * on top of each other.
 */
public class HeadTextureLayer<S extends HumanoidRenderState>
        extends RenderLayer<S, NomadGuardModel> {

    private final NomadGuardModel headModel;
    private final Identifier headTexture;

    public HeadTextureLayer(RenderLayerParent<S, NomadGuardModel> renderer,
                            NomadGuardModel headModel,
                            Identifier headTexture) {
        super(renderer);
        this.headModel = headModel;
        this.headTexture = headTexture;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       S renderState, float yRot, float xRot) {
        this.headModel.setupAnim(renderState);
 this.renderColoredCutoutModel(this.headModel, this.headTexture, poseStack, collector,
                lightCoords, renderState, -1, 0);
    }
}