package com.sappyeddie.nomadcaravans.tent.client;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.GeoQuad;
import com.geckolib.cache.model.GeoVertex;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Map;
import java.util.function.BiConsumer;

public abstract class RecursiveBoneRenderLayer<
        T extends GeoAnimatable,
        C,
        R extends GeoRenderState>
        extends GeoRenderLayer<T, C, R> {

    private final String keyword;

    public RecursiveBoneRenderLayer(GeoRenderer<T, C, R> renderer, String keyword) {
        super(renderer);
        this.keyword = keyword;
    }

    protected abstract Identifier getTexture(R renderState);

    protected abstract RenderType getRenderType(R renderState, Identifier texture);

    protected abstract int getColor(R renderState);

    @Override
    protected Identifier getTextureResource(R renderState) {
        return getTexture(renderState);
    }

    @Override
    public void preRender(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        if (!renderPassInfo.willRender()) return;
        renderPassInfo.addBoneUpdater((info, snapshots) -> {
            for (String name : renderPassInfo.model().boneLookup().get().keySet()) {
                if (name.contains(keyword)) {
                    snapshots.get(name).ifPresent(snapshot -> {
                        snapshot.skipRender(true);
                        snapshot.skipChildrenRender(false);
                    });
                }
            }
        });
    }

    @Override
    public void addPerBoneRender(RenderPassInfo<R> renderPassInfo,
                                 BiConsumer<GeoBone, PerBoneRender<R>> consumer) {
        if (!renderPassInfo.willRender()) return;
        for (Map.Entry<String, GeoBone> entry : renderPassInfo.model().boneLookup().get().entrySet()) {
            if (entry.getKey().contains(keyword)
                    && entry.getValue() instanceof CuboidGeoBone cuboid
                    && cuboid.cubes.length > 0) {
                consumer.accept(cuboid, this::renderSingleBone);
            }
        }
    }

    private void renderSingleBone(RenderPassInfo<R> renderPassInfo, GeoBone bone,
                                  SubmitNodeCollector renderTasks) {
        R renderState = renderPassInfo.renderState();
        Identifier texture = getTexture(renderState);
        RenderType renderType = getRenderType(renderState, texture);
        if (renderType == null) return;

        Identifier baseTexture = this.renderer.getTextureLocation(renderState);
        IntIntPair boneSize = RenderUtil.getTextureDimensions(texture);
        IntIntPair baseSize = RenderUtil.getTextureDimensions(baseTexture);
        float uRatio = baseSize.firstInt() / (float) boneSize.firstInt();
        float vRatio = baseSize.secondInt() / (float) boneSize.secondInt();

        int color = getColor(renderState);
        int packedLight = renderPassInfo.packedLight();
        int packedOverlay = renderPassInfo.packedOverlay();

        renderTasks.submitCustomGeometry(renderPassInfo.poseStack(), renderType, (pose, buffer) -> {
            PoseStack ps = new PoseStack();
            ps.last().set(pose);
            bone.translateAwayFromPivotPoint(ps);
            ps.pushPose();

            for (GeoCube cube : ((CuboidGeoBone) bone).cubes) {
                renderCube(cube, ps, buffer, packedLight, packedOverlay, color, uRatio, vRatio);
            }

            ps.popPose();
        });
    }

    private void renderCube(GeoCube cube, PoseStack ps, VertexConsumer buffer,
                            int packedLight, int packedOverlay, int color,
                            float uRatio, float vRatio) {
        ps.pushPose();
        cube.translateToPivotPoint(ps);
        cube.rotate(ps);
        cube.translateAwayFromPivotPoint(ps);

        Matrix3f normal3f = ps.last().normal();
        Matrix4f pose4f = new Matrix4f(ps.last().pose());

        for (GeoQuad quad : cube.quads()) {
            if (quad == null) continue;
            Vector3f normal = normal3f.transform(new Vector3f(quad.normalVec()));
            RenderUtil.fixInvertedFlatCube(cube, normal);

            for (GeoVertex vertex : quad.vertices()) {
                Vector4f pos = pose4f.transform(
                        new Vector4f(vertex.posX(), vertex.posY(), vertex.posZ(), 1f));
                buffer.addVertex(pos.x(), pos.y(), pos.z(),
                        color,
                        vertex.texU() * uRatio,
                        vertex.texV() * vRatio,
                        packedOverlay, packedLight,
                        normal.x(), normal.y(), normal.z());
            }
        }
        ps.popPose();
    }
}
