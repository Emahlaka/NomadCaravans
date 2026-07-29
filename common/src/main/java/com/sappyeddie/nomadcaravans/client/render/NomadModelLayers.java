package com.sappyeddie.nomadcaravans.client.render;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public final class NomadModelLayers {
    public static final ModelLayerLocation NOMAD_GUARD = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("nomadcaravans", "nomad_guard"), "main");

    private NomadModelLayers() {}
}