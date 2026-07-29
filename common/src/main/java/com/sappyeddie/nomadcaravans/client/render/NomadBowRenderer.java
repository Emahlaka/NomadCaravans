package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sappyeddie.nomadcaravans.item.NomadBowItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;

public class NomadBowRenderer extends GeoItemRenderer<NomadBowItem> {
    public static final DataTicket<Boolean> IS_DRAWING =
            DataTicket.create("nomadcaravans_is_drawing_bow", Boolean.class);

    public NomadBowRenderer(NomadBowItem item) {
        super(item);
    }

    @Override
    public void addRenderData(NomadBowItem animatable, RenderData relatedObject, GeoRenderState renderState, float partialTick) {
        boolean drawing = relatedObject.itemOwner() instanceof LivingEntity livingEntity
                && livingEntity.isUsingItem()
                && livingEntity.getUseItem() == relatedObject.itemStack();

        renderState.addGeckolibData(IS_DRAWING, drawing);
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState> renderPassInfo) {
        super.adjustRenderPose(renderPassInfo);

        ItemDisplayContext context = renderPassInfo.getGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE);
        PoseStack poseStack = renderPassInfo.poseStack();

        switch (context) {
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                poseStack.translate(-0.5f / 16f, 0.5f / 16f, -0.5f / 16f);
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(0), (float) Math.toRadians(-12), (float) Math.toRadians(90)));
                poseStack.scale(0.5f, 0.5f, 0.5f);
            }
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                poseStack.translate(0f, 3f / 16f, -2.25f / 16f);
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(20), (float) Math.toRadians(0), (float) Math.toRadians(74)));
                poseStack.scale(0.5f, 0.5f, 0.5f);
            }
            case GROUND -> {
                poseStack.translate(0f, 7f / 16f, 0f);
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(0), (float) Math.toRadians(0), (float) Math.toRadians(60)));
                poseStack.scale(0.25f, 0.25f, 0.25f);
            }
            case GUI -> {
                poseStack.translate(-1.25f / 16f, 0.75f / 16f, 0f);
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(27), (float) Math.toRadians(66), (float) Math.toRadians(47)));
                poseStack.scale(0.25f, 0.25f, 0.25f);
            }
            case FIXED -> {
                poseStack.translate(0f, 7.5f / 16f, 0f);
                poseStack.mulPose(new Quaternionf().rotationXYZ(
                        (float) Math.toRadians(94), (float) Math.toRadians(0), (float) Math.toRadians(0)));
            }
            default -> { }
        }
    }
}