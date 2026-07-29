package com.sappyeddie.nomadcaravans.item;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
import com.sappyeddie.nomadcaravans.client.render.NomadBowRenderer;

import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;

import java.util.function.Consumer;
import java.util.function.Supplier;
import com.google.common.base.Suppliers;
import org.jetbrains.annotations.Nullable;

public class NomadBowItem extends BowItem implements GeoItem {

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public NomadBowItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private final Supplier<NomadBowRenderer> renderer =
                    Suppliers.memoize(() -> new NomadBowRenderer(NomadBowItem.this));

            @Override
            public @Nullable NomadBowRenderer getGeoItemRenderer() {
                return this.renderer.get();
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("drawController", 0, this::drawPredicate));
    }

    private PlayState drawPredicate(AnimationTest<NomadBowItem> test) {
        boolean drawing = test.getDataOrDefault(NomadBowRenderer.IS_DRAWING, false);

        if (drawing) {
            test.setAnimation(RawAnimation.begin().thenPlayAndHold("animation.nomad_bow.draw"));
        } else {
            test.setAnimation(RawAnimation.begin().thenPlay("animation.nomad_bow.idle"));
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
