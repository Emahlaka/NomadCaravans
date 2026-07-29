package com.sappyeddie.nomadcaravans.tent.block;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import com.sappyeddie.nomadcaravans.tent.client.TentItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class TentBlockItem extends BlockItem implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public TentBlockItem(Block block, Properties props) {
        super(block, props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        Direction desired = player != null ? player.getDirection().getOpposite() : Direction.NORTH;

        if (this.getBlock() instanceof TentBlock tentBlock && !tentBlock.isTentSpaceClear(level, pos, desired)) {
            if (player != null && !level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("message.nomadcaravans.tent.no_space"));
            }
            return InteractionResult.FAIL;
        }

        return super.useOn(context);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private TentItemRenderer renderer;

            @Override
            public TentItemRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new TentItemRenderer(getBlock() instanceof TentBlock tent
                            ? tent.getRolledUpModelId()
                            : com.sappyeddie.nomadcaravans.NomadCaravans.id("item/rolleduptent"));
                }
                return this.renderer;
            }
        });
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }
}
