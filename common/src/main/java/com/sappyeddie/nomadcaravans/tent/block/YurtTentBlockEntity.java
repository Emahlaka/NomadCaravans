package com.sappyeddie.nomadcaravans.tent.block;

import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animatable.manager.AnimatableManager;
import com.sappyeddie.nomadcaravans.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class YurtTentBlockEntity extends TentBlockEntity {

    private static final RawAnimation DOOR_OPEN = RawAnimation.begin().thenPlayAndHold("animatio.yurt_tent.open");
    private static final RawAnimation DOOR_CLOSE = RawAnimation.begin().thenPlayAndHold("animatio.yurt_tent.close");
    private static final RawAnimation OPEN_IDLE = RawAnimation.begin().thenLoop("animatio.yurt_tent.openidle");
    private static final RawAnimation CLOSE_IDLE = RawAnimation.begin().thenLoop("animatio.yurt_tent.closeidle");

    @Nullable
    private Boolean lastOpen = null;

    private RawAnimation current = CLOSE_IDLE;

    public YurtTentBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.YURT_TENT_BE.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<YurtTentBlockEntity>("door", 0, state -> {
            boolean open = getBlockState().hasProperty(TentBlock.OPEN) && getBlockState().getValue(TentBlock.OPEN);
            if (this.lastOpen == null) {

                this.lastOpen = open;
                this.current = open ? OPEN_IDLE : CLOSE_IDLE;
            } else if (open != this.lastOpen) {

                this.lastOpen = open;
                this.current = open ? DOOR_OPEN : DOOR_CLOSE;
            }
            return state.setAndContinue(this.current);
        }));
    }
}
