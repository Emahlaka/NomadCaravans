package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.entity.YakEntity;

public class YakModel extends DefaultedEntityGeoModel<YakEntity> {
    public YakModel() {
        super(NomadCaravans.id("yak"));
    }
}