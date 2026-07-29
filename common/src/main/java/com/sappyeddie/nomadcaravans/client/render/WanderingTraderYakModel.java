package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.entity.WanderingTraderYakEntity;
import com.sappyeddie.nomadcaravans.entity.YakEntity;

public class WanderingTraderYakModel extends DefaultedEntityGeoModel<WanderingTraderYakEntity> {
    public WanderingTraderYakModel() {
        super(NomadCaravans.id("wandering_trader_yak"));
    }
}