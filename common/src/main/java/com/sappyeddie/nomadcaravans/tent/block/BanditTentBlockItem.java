package com.sappyeddie.nomadcaravans.tent.block;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;

/**
 * Places the same tent block as {@link TentBlockItem}, but stamps the item stack
 * with a "Bandit" flag before placement. TentBlock#deployTent reads that flag off
 * the stack and marks the resulting block entity as a bandit variant, so it renders
 * with the bandit_yurt / bandit_tipi texture instead of the normal one.
 *
 * Note: this has to happen via the stack rather than by setting the block entity
 * directly after placement, because TentBlock#setPlacedBy tears down and rebuilds
 * the whole tent (roof, walls, block entity) via deployTent right after placement -
 * anything set on the block entity before that point gets discarded.
 */
public class BanditTentBlockItem extends TentBlockItem {

    public BanditTentBlockItem(Block block, Properties props) {
        super(block, props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.has(DataComponents.CUSTOM_DATA)
                ? stack.get(DataComponents.CUSTOM_DATA).copyTag()
                : new CompoundTag();
        tag.putBoolean("Bandit", true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return super.useOn(context);
    }
}