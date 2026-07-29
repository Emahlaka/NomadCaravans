package com.sappyeddie.nomadcaravans.platform;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.BiFunction;

public final class NomadPlatform {

    public static BiFunction<BlockEntityType.BlockEntitySupplier<?>, Block, BlockEntityType<?>> blockEntityTypeFactory;

    private NomadPlatform() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> BlockEntityType<T> blockEntityType(
            BlockEntityType.BlockEntitySupplier<T> factory, Block block) {
        if (blockEntityTypeFactory == null) {
            throw new IllegalStateException(
                    "NomadPlatform.blockEntityTypeFactory was not installed by the loader entrypoint");
        }
        return (BlockEntityType<T>) blockEntityTypeFactory.apply(factory, block);
    }
}
