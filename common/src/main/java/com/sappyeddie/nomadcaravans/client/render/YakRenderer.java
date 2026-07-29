package com.sappyeddie.nomadcaravans.client.render;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sappyeddie.nomadcaravans.entity.YakEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3f;

public class YakRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<YakEntity, R> {
    private static final DataTicket<Boolean> HAS_CHEST =
            DataTicket.create("nomadcaravans_yak_has_chest", Boolean.class);
    private static final DataTicket<Boolean> HAS_CARPET =
            DataTicket.create("nomadcaravans_yak_has_carpet", Boolean.class);
    private static final DataTicket<Integer> CARPET_COLOR =
            DataTicket.create("nomadcaravans_yak_carpet_color", Integer.class);
    private static final DataTicket<Boolean> HAS_SADDLE =
            DataTicket.create("nomadcaravans_yak_has_saddle", Boolean.class);
    private static final DataTicket<Boolean> HAS_WOOL =
            DataTicket.create("nomadcaravans_yak_has_wool", Boolean.class);

    private static final DataTicket<Entity> RIDER =
            DataTicket.create("nomadcaravans_yak_rider", Entity.class);

    private static final DataTicket<Float> PARTIAL_TICK =
            DataTicket.create("nomadcaravans_yak_partial_tick", Float.class);

    private @Nullable BakedGeoModel lastModel = null;
    private @Nullable GeoBone fabricBone = null;
    private @Nullable GeoBone saddleBone = null;

    public YakRenderer(EntityRendererProvider.Context context) {
        super(context, new YakModel());
    }

    @Override
    public void addRenderData(YakEntity animatable, Void relatedObject, R renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);

        renderState.addGeckolibData(HAS_CHEST, animatable.hasChest());
        renderState.addGeckolibData(HAS_CARPET, animatable.hasCarpet());
        renderState.addGeckolibData(HAS_SADDLE, animatable.isSaddled());
        renderState.addGeckolibData(HAS_WOOL, animatable.hasWool());

        if (animatable.hasCarpet()) {
            renderState.addGeckolibData(CARPET_COLOR, 0xFF000000 | animatable.getCarpetColor().getTextureDiffuseColor());
        }

        renderState.addGeckolibData(PARTIAL_TICK, partialTick);

        Entity firstPassenger = animatable.getFirstPassenger();
        if (animatable.isSaddled() && firstPassenger != null) {
            renderState.addGeckolibData(RIDER, firstPassenger);
        }
    }

    @Override
    public void preRenderPass(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);

        refreshBoneCache(renderPassInfo);

        final Entity rider = renderPassInfo.getGeckolibData(RIDER);

        if (this.saddleBone != null && rider != null) {
            renderPassInfo.addPerBoneRender(this.saddleBone, (passInfo, boneArg, tasks) ->
                    renderRiderAtBone(rider, passInfo, tasks));
        }

        if (this.fabricBone == null || !renderPassInfo.getGeckolibData(HAS_CARPET))
            return;

        final GeoBone bone = this.fabricBone;

        renderPassInfo.addPerBoneRender(bone, (passInfo, boneArg, tasks) -> {
            final R renderState = passInfo.renderState();
            final int tintColor = renderState.getOrDefaultGeckolibData(CARPET_COLOR, 0xFFFFFFFF);
            final int finalColor = ARGB.multiply(passInfo.renderColor(), tintColor);
            final RenderType renderType = getRenderType(renderState, getTextureLocation(renderState));

            if (renderType != null) {
                tasks.submitCustomGeometry(passInfo.poseStack(), renderType, (pose, vertexConsumer) -> {
                    PoseStack poseStack = new PoseStack();
                    poseStack.last().set(pose);

                    boneArg.translateAwayFromPivotPoint(poseStack);

                    boneArg.render(passInfo, poseStack, vertexConsumer, passInfo.packedLight(), passInfo.packedOverlay(), finalColor);
                });
            }
        });
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        boolean hasChest = renderPassInfo.getGeckolibData(HAS_CHEST);
        snapshots.ifPresent("chests", boneSnapshot -> boneSnapshot.skipRender(!hasChest));

        boolean hasSaddle = renderPassInfo.getGeckolibData(HAS_SADDLE);
        snapshots.ifPresent("saddle", boneSnapshot -> boneSnapshot.skipRender(!hasSaddle));

        boolean hasWool = renderPassInfo.getGeckolibData(HAS_WOOL);
        snapshots.ifPresent("wool", boneSnapshot -> boneSnapshot.skipRender(!hasWool));

        snapshots.ifPresent("fabric", boneSnapshot -> boneSnapshot.skipRender(true));
    }

    private void refreshBoneCache(RenderPassInfo<R> renderPassInfo) {
        BakedGeoModel model = renderPassInfo.model();

        if (model != this.lastModel) {
            this.lastModel = model;
            this.fabricBone = findBone(model.topLevelBones(), "fabric");

            this.saddleBone = findBone(model.topLevelBones(), "saddle_seat");
        }
    }

    @SuppressWarnings("unchecked")
    private void renderRiderAtBone(Entity rider, RenderPassInfo<R> passInfo, SubmitNodeCollector tasks) {
        if (rider.isRemoved())
            return;

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<Entity, EntityRenderState> renderer =
                (EntityRenderer<Entity, EntityRenderState>) dispatcher.getRenderer(rider);

        if (renderer == null)
            return;

        PoseStack poseStack = new PoseStack();
        poseStack.last().set(passInfo.poseStack().last());
        stripRotationKeepTranslation(poseStack);

        float partialTick = passInfo.getOrDefaultGeckolibData(PARTIAL_TICK, 1.0F);

        EntityRenderState riderState = renderer.createRenderState(rider, partialTick);

        renderer.submit(riderState, poseStack, tasks, passInfo.cameraState());
    }

    private static void stripRotationKeepTranslation(PoseStack poseStack) {
        Vector3f translation = poseStack.last().pose().getTranslation(new Vector3f());
        poseStack.last().pose().identity().translate(translation);
        poseStack.last().normal().identity();
    }

    private static @Nullable GeoBone findBone(GeoBone[] bones, String name) {
        for (GeoBone bone : bones) {
            if (bone.name().equals(name))
                return bone;

            GeoBone found = findBone(bone.children(), name);

            if (found != null)
                return found;
        }

        return null;
    }
}
