package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.entity.UntameableYakEntity;

public class UntameableYakModel extends DefaultedEntityGeoModel<UntameableYakEntity> {
    public UntameableYakModel() {
        super(NomadCaravans.id("yak"));
    }
}
