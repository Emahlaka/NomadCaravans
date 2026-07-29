package com.sappyeddie.nomadcaravans.tent.client;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.tent.block.TipiTentBlockEntity;
import net.minecraft.resources.Identifier;

public class TipiTentModel extends GeoModel<TipiTentBlockEntity> {

    private static final Identifier MODEL = NomadCaravans.id("block/tipi");
    private static final Identifier TEXTURE = NomadCaravans.id("textures/block/tipi.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(TipiTentBlockEntity animatable) {

        return null;
    }
}
