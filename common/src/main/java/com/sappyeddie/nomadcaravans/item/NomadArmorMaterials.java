package com.sappyeddie.nomadcaravans.item;

import com.sappyeddie.nomadcaravans.NomadCaravans;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

public final class NomadArmorMaterials {

    public static final int YAK_HIDE_BASE_DURABILITY = 12;

    public static final int YAK_HIDE_ENCHANTMENT_VALUE = 12;

    public static final ResourceKey<EquipmentAsset> YAK_HIDE_ASSET_KEY =
            ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(NomadCaravans.MOD_ID, "yak_hide"));

    public static final TagKey<Item> REPAIRS_YAK_HIDE_ARMOR =
            TagKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(NomadCaravans.MOD_ID, "repairs_yak_hide_armor"));

    public static final ArmorMaterial YAK_HIDE = new ArmorMaterial(
            YAK_HIDE_BASE_DURABILITY,
            Map.of(
                    ArmorType.HELMET, 3,
                    ArmorType.CHESTPLATE, 7,
                    ArmorType.LEGGINGS, 5,
                    ArmorType.BOOTS, 3
            ),
            YAK_HIDE_ENCHANTMENT_VALUE,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            1F,
            0.05F,
            REPAIRS_YAK_HIDE_ARMOR,
            YAK_HIDE_ASSET_KEY
    );

    private NomadArmorMaterials() {}
}
