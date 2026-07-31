package com.sappyeddie.nomadcaravans.neoforge.tent.client;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.tent.block.YurtTentBlockEntity;
import com.sappyeddie.nomadcaravans.neoforge.tent.client.TentBlockRenderer;
import net.minecraft.resources.Identifier;

public class YurtTentModel extends GeoModel<YurtTentBlockEntity> {

    private static final Identifier MODEL = NomadCaravans.id("block/yurt");
    private static final Identifier TEXTURE = NomadCaravans.id("textures/block/yurt.png");
    private static final Identifier BANDIT_TEXTURE = NomadCaravans.id("textures/block/bandit_yurt.png");
    private static final Identifier ANIMATION = NomadCaravans.id("block/yurt_tent");

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
    public Identifier getAnimationResource(YurtTentBlockEntity animatable) {
        return ANIMATION;
    }
}