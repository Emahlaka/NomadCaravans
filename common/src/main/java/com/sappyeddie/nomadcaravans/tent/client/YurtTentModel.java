package com.sappyeddie.nomadcaravans.tent.client;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.tent.block.YurtTentBlockEntity;
import net.minecraft.resources.Identifier;

public class YurtTentModel extends GeoModel<YurtTentBlockEntity> {

    private static final Identifier MODEL = NomadCaravans.id("block/yurt");
    private static final Identifier TEXTURE = NomadCaravans.id("textures/block/yurt.png");
    private static final Identifier ANIMATION = NomadCaravans.id("block/yurt_tent");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(YurtTentBlockEntity animatable) {
        return ANIMATION;
    }
}
