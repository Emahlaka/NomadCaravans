package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.entity.UntameableBabyYakEntity;

public class UntameableBabyYakModel extends DefaultedEntityGeoModel<UntameableBabyYakEntity> {
    public UntameableBabyYakModel() {
        super(NomadCaravans.id("baby_yak"));
    }
}
