package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.entity.UntameableWanderingTraderYakEntity;

public class UntameableWanderingTraderYakModel extends DefaultedEntityGeoModel<UntameableWanderingTraderYakEntity> {
    public UntameableWanderingTraderYakModel() {
        super(NomadCaravans.id("wandering_trader_yak"));
    }
}
