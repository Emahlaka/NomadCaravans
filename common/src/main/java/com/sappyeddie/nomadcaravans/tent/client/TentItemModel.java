package com.sappyeddie.nomadcaravans.tent.client;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.tent.block.TentBlockItem;
import net.minecraft.resources.Identifier;

public class TentItemModel extends GeoModel<TentBlockItem> {

    private static final Identifier TEXTURE = NomadCaravans.id("textures/item/rolleduptent.png");
    private static final Identifier BANDIT_TEXTURE = NomadCaravans.id("textures/item/bandit_rolleduptent.png");

    private final Identifier model;

    public TentItemModel(Identifier model) {
        this.model = model;
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return this.model;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        boolean bandit = Boolean.TRUE.equals(renderState.getGeckolibData(TentBlockRenderer.BANDIT));
        return bandit ? BANDIT_TEXTURE : TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(TentBlockItem animatable) {
        return null;
    }
}