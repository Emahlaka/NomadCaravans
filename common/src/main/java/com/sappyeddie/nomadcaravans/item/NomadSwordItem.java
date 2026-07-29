package com.sappyeddie.nomadcaravans.item;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import com.sappyeddie.nomadcaravans.client.render.NomadSwordRenderer;

import net.minecraft.world.item.Item;

import java.util.function.Consumer;
import java.util.function.Supplier;
import com.google.common.base.Suppliers;
import org.jetbrains.annotations.Nullable;

public class NomadSwordItem extends Item implements GeoItem {

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public NomadSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private final Supplier<NomadSwordRenderer> renderer =
                    Suppliers.memoize(() -> new NomadSwordRenderer(NomadSwordItem.this));

            @Override
            public @Nullable NomadSwordRenderer getGeoItemRenderer() {
                return this.renderer.get();
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
