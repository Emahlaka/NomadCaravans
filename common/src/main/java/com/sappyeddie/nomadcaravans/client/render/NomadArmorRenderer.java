package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedGeoModel;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.sappyeddie.nomadcaravans.NomadCaravans;
import com.sappyeddie.nomadcaravans.item.NomadArmorItem;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class NomadArmorRenderer<R extends HumanoidRenderState & GeoRenderState> extends GeoArmorRenderer<NomadArmorItem, R> {

    private static final Identifier SHARED_ASSET_ID =
            Identifier.fromNamespaceAndPath(NomadCaravans.MOD_ID, "nomad_armor");

    private static final DataTicket<Float> TUSSEL_SWING_X =
            DataTicket.create("nomadcaravans_nomad_helmet_tussel_swing_x", Float.class);
    private static final DataTicket<Float> TUSSEL_SWING_Z =
            DataTicket.create("nomadcaravans_nomad_helmet_tussel_swing_z", Float.class);

    public NomadArmorRenderer(NomadArmorItem armorItem) {
        super(buildModel(armorItem));
    }

    private static DefaultedGeoModel<NomadArmorItem> buildModel(NomadArmorItem armorItem) {
        return new DefaultedGeoModel<NomadArmorItem>(BuiltInRegistries.ITEM.getKey(armorItem)) {
            @Override
            protected String subtype() {
                return "armor";
            }
        }
                .withAltTexture(SHARED_ASSET_ID)
                .withAltAnimations(SHARED_ASSET_ID);
    }

    @Override
    public List<ArmorSegment> getSegmentsForSlot(R renderState, EquipmentSlot slot) {
        return switch (slot) {

            case LEGS -> List.of(ArmorSegment.LEFT_LEG, ArmorSegment.RIGHT_LEG, ArmorSegment.CHEST);
            default -> super.getSegmentsForSlot(renderState, slot);
        };
    }

    @Override
    public void captureDefaultRenderState(NomadArmorItem animatable, RenderData renderData, R renderState, float partialTick) {
        super.captureDefaultRenderState(animatable, renderData, renderState, partialTick);

        if (renderData.slot() != EquipmentSlot.HEAD)
            return;

        LivingEntity wearer = renderData.entity();
        TusselPhysics physics = TusselPhysics.get(wearer.getUUID());
        physics.tickIfNeeded(wearer.level().getGameTime(), wearer.getYHeadRot(), wearer.getXRot());

        renderState.addGeckolibData(TUSSEL_SWING_X, physics.getSwingX(partialTick));
        renderState.addGeckolibData(TUSSEL_SWING_Z, physics.getSwingZ(partialTick));
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        Float swingXData = renderPassInfo.getGeckolibData(TUSSEL_SWING_X);

        if (swingXData == null)
            return;

        float swingX = swingXData * Mth.DEG_TO_RAD;
        float swingZ = renderPassInfo.getGeckolibData(TUSSEL_SWING_Z) * Mth.DEG_TO_RAD;

        snapshots.ifPresent("phys_tussel", boneSnapshot -> {
            boneSnapshot.setRotX(boneSnapshot.getRotX() + swingX);
            boneSnapshot.setRotZ(boneSnapshot.getRotZ() + swingZ);
        });
    }
}
