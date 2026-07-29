package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sappyeddie.nomadcaravans.item.NomadShieldItem;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;

public class NomadShieldRenderer extends GeoItemRenderer<NomadShieldItem> {

    public NomadShieldRenderer(NomadShieldItem item) {
        super(item);
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState> renderPassInfo) {
        super.adjustRenderPose(renderPassInfo);

        ItemDisplayContext context = renderPassInfo.getGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE);
        PoseStack poseStack = renderPassInfo.poseStack();

        switch (context) {
            case THIRD_PERSON_RIGHT_HAND -> {
                poseStack.translate(1.5f / 16f, -1.5f / 16f, 1.75f / 16f);
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(90), (float) Math.toRadians(0), (float) Math.toRadians(-90)));
                poseStack.scale(0.75f, 0.75f, 0.75f);
            }
            case THIRD_PERSON_LEFT_HAND -> {
                poseStack.translate(-1.5f / 16f, -1.5f / 16f, 1.75f / 16f);
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(90), (float) Math.toRadians(0), (float) Math.toRadians(90)));
                poseStack.scale(0.75f, 0.75f, 0.75f);
            }
            case FIRST_PERSON_RIGHT_HAND -> {
                poseStack.translate(2.5f / 16f, -1.75f / 16f, 0f / 16f);
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(-90), (float) Math.toRadians(90), (float) Math.toRadians(0)));
            }
            case FIRST_PERSON_LEFT_HAND -> {
                poseStack.translate(2.5f / 16f, -1.75f / 16f, 0f / 16f);
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(-90), (float) Math.toRadians(90), (float) Math.toRadians(0)));
            }
            case GUI -> {
                poseStack.translate(0f / 16f, -0.25f / 16f, 0f / 16f);
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(4.04), (float) Math.toRadians(19.03), (float) Math.toRadians(56.73)));
                poseStack.scale(0.75f, 0.75f, 0.75f);
            }
            case HEAD -> {
                poseStack.translate(0f / 16f, 5.25f / 16f, 0f / 16f);
            }
            case FIXED -> {
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(-90), (float) Math.toRadians(0), (float) Math.toRadians(0)));
                poseStack.scale(2f, 2f, 2f);
            }
            case GROUND -> {
                poseStack.scale(0.5f, 0.5f, 0.5f);
            }
            case ON_SHELF -> {
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(90), (float) Math.toRadians(-90), (float) Math.toRadians(0)));
                poseStack.scale(0.75f, 0.75f, 0.75f);
            }
            default -> { }
        }
    }
}