package com.sappyeddie.nomadcaravans.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;

public final class ModFoods {
    private ModFoods() {
    }
    public static final FoodProperties RAW_YAK_MEAT = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.3f).build();

    public static final FoodProperties COOKED_YAK_MEAT = new FoodProperties.Builder()
            .nutrition(8).saturationModifier(0.8f).build();

    public static final FoodProperties DRIED_YAK_MEAT = new FoodProperties.Builder()
            .nutrition(12).saturationModifier(0.5f).build();

}
