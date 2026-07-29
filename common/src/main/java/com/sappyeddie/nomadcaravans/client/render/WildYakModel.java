package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.entity.WildYakEntity;
import com.sappyeddie.nomadcaravans.entity.YakEntity;

public class WildYakModel extends DefaultedEntityGeoModel<WildYakEntity> {
    public WildYakModel() {
        super(NomadCaravans.id("wild_yak"));
    }
}