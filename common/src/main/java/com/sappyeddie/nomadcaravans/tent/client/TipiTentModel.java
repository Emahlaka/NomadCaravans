package com.sappyeddie.nomadcaravans.tent.client;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.tent.block.TipiTentBlockEntity;
import net.minecraft.resources.Identifier;

public class TipiTentModel extends GeoModel<TipiTentBlockEntity> {

    private static final Identifier MODEL = NomadCaravans.id("block/tipi");
    private static final Identifier TEXTURE = NomadCaravans.id("textures/block/tipi.png");
    private static final Identifier BANDIT_TEXTURE = NomadCaravans.id("textures/block/bandit_tipi.png");
    private static final Identifier ANIMATION = NomadCaravans.id("block/tipi_tent");
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        boolean bandit = Boolean.TRUE.equals(renderState.getGeckolibData(TentBlockRenderer.BANDIT));
        return bandit ? BANDIT_TEXTURE : TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(TipiTentBlockEntity animatable) {
        return ANIMATION;
    }
}