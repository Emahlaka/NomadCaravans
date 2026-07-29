package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sappyeddie.nomadcaravans.item.NomadSwordItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;

public class NomadSwordRenderer extends GeoItemRenderer<NomadSwordItem> {


    public NomadSwordRenderer(NomadSwordItem item) {
        super(item);
    }



    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState> renderPassInfo) {
        super.adjustRenderPose(renderPassInfo);

        ItemDisplayContext context = renderPassInfo.getGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE);
        PoseStack poseStack = renderPassInfo.poseStack();

        switch (context) {
            case THIRD_PERSON_RIGHT_HAND -> {
                poseStack.translate(1f / 16f, 0.5f / 16f, 1.25f / 16f);
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(-12), (float) Math.toRadians(-90), (float) Math.toRadians(0)));
                poseStack.scale(1.5f, 1.5f, 1.5f);
            }
            case THIRD_PERSON_LEFT_HAND -> {
                poseStack.translate(-1f / 16f, 0.5f / 16f, 1.25f / 16f);
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(-12), (float) Math.toRadians(90), (float) Math.toRadians(0)));
                poseStack.scale(1.5f, 1.5f, 1.5f);
            }
            case FIRST_PERSON_RIGHT_HAND -> {
                poseStack.translate(2.75f / 16f, 2.25f / 16f, 0f);
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(0), (float) Math.toRadians(-90), (float) Math.toRadians(12)));
            }
            case FIRST_PERSON_LEFT_HAND -> {
                poseStack.translate(2.75f / 16f, 2.25f / 16f, 0.75f / 16f);
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(-32), (float) Math.toRadians(-90), (float) Math.toRadians(12)));
            }
            case GUI -> {
                poseStack.translate(-0.75f / 16f, -2.5f / 16f, 0f);
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(0), (float) Math.toRadians(0), (float) Math.toRadians(-35)));
            }
            case HEAD -> {
                poseStack.translate(0f, 11f / 16f, 0f);
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(180), (float) Math.toRadians(0), (float) Math.toRadians(0)));
            }
            case FIXED -> {
                poseStack.translate(7f / 16f, 0.75f / 16f, -1.75f / 16f);
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(0), (float) Math.toRadians(0), (float) Math.toRadians(90)));
                poseStack.scale(2f, 2f, 2f);
            }
            case ON_SHELF -> {
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(0), (float) Math.toRadians(0), (float) Math.toRadians(-15)));
                poseStack.scale(1.5f, 1.5f, 1.5f);
            }
            default -> { }
        }
    }
}