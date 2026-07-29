package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.entity.BabyYakEntity;
import com.sappyeddie.nomadcaravans.entity.YakEntity;

public class BabyYakModel extends DefaultedEntityGeoModel<BabyYakEntity> {
    public BabyYakModel() {
        super(NomadCaravans.id("baby_yak"));
    }
}