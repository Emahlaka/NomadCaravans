package com.sappyeddie.nomadcaravans.client.render;

import com.sappyeddie.nomadcaravans.entity.CaravanGuardEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BowItem;

public class CaravanGuardRenderer
        extends HumanoidMobRenderer<CaravanGuardEntity, HumanoidRenderState, NomadGuardModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("nomadcaravans", "textures/entity/nomad_guard.png");

    private static final Identifier HEAD_TEXTURE =
            Identifier.fromNamespaceAndPath("nomadcaravans", "textures/entity/nomad_head.png");

    public CaravanGuardRenderer(EntityRendererProvider.Context context) {
        super(context, new NomadGuardModel(context.bakeLayer(NomadModelLayers.NOMAD_GUARD)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this));
        ArmorModelSet<HumanoidModel<HumanoidRenderState>> armorModels =
                ArmorModelSet.bake(ModelLayers.HUSK_ARMOR, context.getModelSet(), HumanoidModel::new);
        this.addLayer(new HumanoidArmorLayer<>(this, armorModels, context.getEquipmentRenderer()));
        this.addLayer(new HeadTextureLayer<>(this,
                new NomadGuardModel(context.bakeLayer(NomadModelLayers.NOMAD_GUARD_HEAD)),
                HEAD_TEXTURE));
    }

    @Override
    public HumanoidRenderState createRenderState() {
        return new HumanoidRenderState();
    }

    @Override
    public void extractRenderState(CaravanGuardEntity entity, HumanoidRenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);

        if (entity.isUsingItem() && entity.getUseItem().getItem() instanceof BowItem) {
            HumanoidArm arm = entity.getUsedItemHand() == InteractionHand.MAIN_HAND
                    ? entity.getMainArm()
                    : entity.getMainArm().getOpposite();
            if (arm == HumanoidArm.RIGHT) {
                renderState.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
            } else {
                renderState.leftArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
        }
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState renderState) {
        return TEXTURE;
    }
}