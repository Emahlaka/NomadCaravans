package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.entity.UntameableWildYakEntity;

public class UntameableWildYakModel extends DefaultedEntityGeoModel<UntameableWildYakEntity> {
    public UntameableWildYakModel() {
        super(NomadCaravans.id("wild_yak"));
    }
}
