package com.sappyeddie.nomadcaravans;

import com.sappyeddie.nomadcaravans.entity.*;
import com.sappyeddie.nomadcaravans.item.*;
import com.sappyeddie.nomadcaravans.trade.ModVillagerProfessions;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import com.sappyeddie.nomadcaravans.block.CampFencePostBlock;
import com.sappyeddie.nomadcaravans.platform.NomadPlatform;
import com.sappyeddie.nomadcaravans.tent.block.*;
import net.minecraft.world.item.component.BlocksAttacks;

import java.util.List;
import java.util.Optional;

public final class ModRegistries {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(NomadCaravans.MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(NomadCaravans.MOD_ID, Registries.ITEM);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(NomadCaravans.MOD_ID, Registries.ENTITY_TYPE);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(NomadCaravans.MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<Item> NOMAD_HELMET = ITEMS.register("nomad_helmet",
            () -> new NomadArmorItem(NomadArmorMaterials.YAK_HIDE, ArmorType.HELMET,
                    new Item.Properties()
                            .durability(ArmorType.HELMET.getDurability(NomadArmorMaterials.YAK_HIDE_BASE_DURABILITY))
                            .enchantable(NomadArmorMaterials.YAK_HIDE_ENCHANTMENT_VALUE)
                            .setId(itemKey("nomad_helmet"))));

    public static final RegistrySupplier<Item> NOMAD_CHESTPLATE = ITEMS.register("nomad_chestplate",
            () -> new NomadArmorItem(NomadArmorMaterials.YAK_HIDE, ArmorType.CHESTPLATE,
                    new Item.Properties()
                            .durability(ArmorType.CHESTPLATE.getDurability(NomadArmorMaterials.YAK_HIDE_BASE_DURABILITY))
                            .enchantable(NomadArmorMaterials.YAK_HIDE_ENCHANTMENT_VALUE)
                            .setId(itemKey("nomad_chestplate"))));

    public static final RegistrySupplier<Item> NOMAD_LEGGINGS = ITEMS.register("nomad_leggings",
            () -> new NomadArmorItem(NomadArmorMaterials.YAK_HIDE, ArmorType.LEGGINGS,
                    new Item.Properties()
                            .durability(ArmorType.LEGGINGS.getDurability(NomadArmorMaterials.YAK_HIDE_BASE_DURABILITY))
                            .enchantable(NomadArmorMaterials.YAK_HIDE_ENCHANTMENT_VALUE)
                            .setId(itemKey("nomad_leggings"))));

    public static final RegistrySupplier<Item> NOMAD_BOOTS = ITEMS.register("nomad_boots",
            () -> new NomadArmorItem(NomadArmorMaterials.YAK_HIDE, ArmorType.BOOTS,
                    new Item.Properties()
                            .durability(ArmorType.BOOTS.getDurability(NomadArmorMaterials.YAK_HIDE_BASE_DURABILITY))
                            .enchantable(NomadArmorMaterials.YAK_HIDE_ENCHANTMENT_VALUE)
                            .setId(itemKey("nomad_boots"))));

    public static final RegistrySupplier<Item> NOMAD_BOW = ITEMS.register("nomad_bow",
            () -> new NomadBowItem(new Item.Properties()
                    .durability(384)
                    .enchantable(1)
                    .setId(itemKey("nomad_bow"))));

    public static final RegistrySupplier<Item> NOMAD_SWORD = ITEMS.register("nomad_sword",
            () -> new NomadSwordItem(new Item.Properties()
                    .sword(
                            ToolMaterial.IRON,
                            2.5f,
                            -1.75f
                    )
                    .setId(itemKey("nomad_sword"))));

    public static final RegistrySupplier<Item> NOMAD_SHIELD = ITEMS.register("nomad_shield",
            () -> new NomadShieldItem(new Item.Properties()
                    .durability(336)

                    .component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                            0.25F, 1.0F,
                            List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
                            new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                            Optional.empty(), Optional.empty(), Optional.empty()))
                    .setId(itemKey("nomad_shield"))));

    public static final RegistrySupplier<EntityType<YakEntity>> YAK = ENTITY_TYPES.register("yak",
            () -> EntityType.Builder.of(YakEntity::new, MobCategory.CREATURE)
                    .sized(1.1F, 1.6F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, NomadCaravans.id("yak"))));

    public static final RegistrySupplier<EntityType<BabyYakEntity>> BABY_YAK = ENTITY_TYPES.register("baby_yak",
            () -> EntityType.Builder.of(BabyYakEntity::new, MobCategory.CREATURE)
                    .sized(0.55F, 0.8F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, NomadCaravans.id("baby_yak"))));

    public static final RegistrySupplier<EntityType<WanderingTraderYakEntity>> WANDERING_TRADER_YAK = ENTITY_TYPES.register("wandering_trader_yak",
            () -> EntityType.Builder.of(WanderingTraderYakEntity::new, MobCategory.CREATURE)
                    .sized(1.1F, 1.6F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, NomadCaravans.id("wandering_trader_yak"))));

    public static final RegistrySupplier<EntityType<WildYakEntity>> WILD_YAK = ENTITY_TYPES.register("wild_yak",
            () -> EntityType.Builder.of(WildYakEntity::new, MobCategory.CREATURE)
                    .sized(1.1F, 1.6F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, NomadCaravans.id("wild_yak"))));

    public static final RegistrySupplier<EntityType<UntameableYakEntity>> UNTAMEABLE_YAK =
            ENTITY_TYPES.register("untameable_yak",
                    () -> EntityType.Builder.of(UntameableYakEntity::new, MobCategory.CREATURE)
                            .sized(1.1F, 1.6F)
                            .clientTrackingRange(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, NomadCaravans.id("untameable_yak"))));

    public static final RegistrySupplier<EntityType<UntameableWildYakEntity>> UNTAMEABLE_WILD_YAK =
            ENTITY_TYPES.register("untameable_wild_yak",
                    () -> EntityType.Builder.of(UntameableWildYakEntity::new, MobCategory.CREATURE)
                            .sized(1.1F, 1.6F)
                            .clientTrackingRange(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, NomadCaravans.id("untameable_wild_yak"))));

    public static final RegistrySupplier<EntityType<UntameableBabyYakEntity>> UNTAMEABLE_BABY_YAK =
            ENTITY_TYPES.register("untameable_baby_yak",
                    () -> EntityType.Builder.of(UntameableBabyYakEntity::new, MobCategory.CREATURE)
                            .sized(0.55F, 0.8F)
                            .clientTrackingRange(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    NomadCaravans.id("untameable_baby_yak"))));

    public static final RegistrySupplier<EntityType<UntameableWanderingTraderYakEntity>> UNTAMEABLE_WANDERING_TRADER_YAK =
            ENTITY_TYPES.register("untameable_wandering_trader_yak",
                    () -> EntityType.Builder.of(UntameableWanderingTraderYakEntity::new, MobCategory.CREATURE)
                            .sized(1.1F, 1.6F)
                            .clientTrackingRange(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    NomadCaravans.id("untameable_wandering_trader_yak"))));

    public static final RegistrySupplier<EntityType<CaravanLeaderEntity>> CARAVAN_LEADER = ENTITY_TYPES.register(
            "caravan_leader",
            () -> EntityType.Builder.of(CaravanLeaderEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, NomadCaravans.id("caravan_leader"))));

    public static final RegistrySupplier<Item> RIGHT_OF_YAK_OWNERSHIP =
            ITEMS.register("right_of_yak_ownership", () -> new Item(new Item.Properties()
                    .setId(itemKey("right_of_yak_ownership"))));

    public static final RegistrySupplier<Item> YAK_DEED =
            ITEMS.register("yak_deed", () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .setId(itemKey("yak_deed"))));

    public static final RegistrySupplier<Item> YAK_SPAWN_EGG =
            ITEMS.register("yak_spawn_egg", () -> new YakSpawnEggItem(new Item.Properties()
                    .spawnEgg(YAK.get())
                    .setId(itemKey("yak_spawn_egg"))));

    public static final RegistrySupplier<Item> WILD_YAK_SPAWN_EGG =
            ITEMS.register("wild_yak_spawn_egg", () -> new WildYakSpawnEggItem(new Item.Properties()
                    .spawnEgg(WILD_YAK.get())
                    .setId(itemKey("wild_yak_spawn_egg"))));

    public static final RegistrySupplier<Item> WANDERING_TRADER_YAK_SPAWN_EGG =
            ITEMS.register("wandering_trader_yak_spawn_egg", () -> new WanderingTraderYakSpawnEggItem(new Item.Properties()
                    .spawnEgg(WANDERING_TRADER_YAK.get())
                    .setId(itemKey("wandering_trader_yak_spawn_egg"))));

    public static final RegistrySupplier<Item> UNTAMEABLE_YAK_SPAWN_EGG =
            ITEMS.register("untameable_yak_spawn_egg", () -> new SpawnEggItem(new Item.Properties()
                    .spawnEgg(UNTAMEABLE_YAK.get())
                    .setId(itemKey("untameable_yak_spawn_egg"))));

    public static final RegistrySupplier<Item> UNTAMEABLE_WILD_YAK_SPAWN_EGG =
            ITEMS.register("untameable_wild_yak_spawn_egg", () -> new SpawnEggItem(new Item.Properties()
                    .spawnEgg(UNTAMEABLE_WILD_YAK.get())
                    .setId(itemKey("untameable_wild_yak_spawn_egg"))));

    public static final RegistrySupplier<Item> UNTAMEABLE_WANDERING_TRADER_YAK_SPAWN_EGG =
            ITEMS.register("untameable_wandering_trader_yak_spawn_egg", () -> new SpawnEggItem(new Item.Properties()
                    .spawnEgg(UNTAMEABLE_WANDERING_TRADER_YAK.get())
                    .setId(itemKey("untameable_wandering_trader_yak_spawn_egg"))));

    public static final DeferredRegister<VillagerType> VILLAGER_TYPES =
            DeferredRegister.create(NomadCaravans.MOD_ID, Registries.VILLAGER_TYPE);

    public static final RegistrySupplier<VillagerType> NOMAD =
            VILLAGER_TYPES.register("nomad", VillagerType::new);

    public static final RegistrySupplier<EntityType<CaravanFollowerEntity>> CARAVAN_FOLLOWER = ENTITY_TYPES.register(
            "caravan_follower",
            () -> EntityType.Builder.of(CaravanFollowerEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, NomadCaravans.id("caravan_follower"))));

    public static final RegistrySupplier<EntityType<CaravanGuardEntity>> CARAVAN_GUARD = ENTITY_TYPES.register(
            "caravan_guard",
            () -> EntityType.Builder.of(CaravanGuardEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, NomadCaravans.id("caravan_guard"))));

    public static final RegistrySupplier<Item> CARAVAN_GUARD_SPAWN_EGG =
            ITEMS.register("caravan_guard_spawn_egg", () -> new SpawnEggItem(new Item.Properties()
                    .spawnEgg(CARAVAN_GUARD.get())
                    .setId(itemKey("caravan_guard_spawn_egg"))));

    public static final RegistrySupplier<Item> CARAVAN_LEADER_SPAWN_EGG =
            ITEMS.register("caravan_leader_spawn_egg", () -> new SpawnEggItem(new Item.Properties()
                    .spawnEgg(CARAVAN_LEADER.get())
                    .setId(itemKey("caravan_leader_spawn_egg"))));

    public static final RegistrySupplier<Item> CARAVAN_FOLLOWER_SPAWN_EGG =
            ITEMS.register("caravan_follower_spawn_egg", () -> new SpawnEggItem(new Item.Properties()
                    .spawnEgg(CARAVAN_FOLLOWER.get())
                    .setId(itemKey("caravan_follower_spawn_egg"))));

    public static final RegistrySupplier<Item> RAW_YAK_MEAT = ITEMS.register("raw_yak_meat",
            () -> new Item(new Item.Properties()
                    .food(ModFoods.RAW_YAK_MEAT)
                    .setId(itemKey("raw_yak_meat"))));

    public static final RegistrySupplier<Item> COOKED_YAK_MEAT = ITEMS.register("cooked_yak_meat",
            () -> new Item(new Item.Properties()
                    .food(ModFoods.COOKED_YAK_MEAT)
                    .setId(itemKey("cooked_yak_meat"))));

    public static final RegistrySupplier<Item> DRIED_YAK_MEAT = ITEMS.register("dried_yak_meat",
            () -> new Item(new Item.Properties()
                    .food(ModFoods.DRIED_YAK_MEAT)
                    .setId(itemKey("dried_yak_meat"))));

    public static final RegistrySupplier<Block> BROWN_YAK_WOOL = BLOCKS.register("brown_yak_wool",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(0.8F)
                    .sound(SoundType.WOOL)
                    .ignitedByLava()
                    .setId(blockKey("brown_yak_wool"))));

    public static final RegistrySupplier<Block> GRAY_YAK_WOOL = BLOCKS.register("gray_yak_wool",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(0.8F)
                    .sound(SoundType.WOOL)
                    .ignitedByLava()
                    .setId(blockKey("gray_yak_wool"))));

    public static final RegistrySupplier<Block> WHITE_YAK_WOOL = BLOCKS.register("white_yak_wool",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(0.8F)
                    .sound(SoundType.WOOL)
                    .ignitedByLava()
                    .setId(blockKey("white_yak_wool"))));

    public static final RegistrySupplier<Item> BROWN_YAK_WOOL_ITEM = ITEMS.register("brown_yak_wool",
            () -> new BlockItem(BROWN_YAK_WOOL.get(), new Item.Properties()
                    .setId(itemKey("brown_yak_wool"))));

    public static final RegistrySupplier<Item> GRAY_YAK_WOOL_ITEM = ITEMS.register("gray_yak_wool",
            () -> new BlockItem(GRAY_YAK_WOOL.get(), new Item.Properties()
                    .setId(itemKey("gray_yak_wool"))));

    public static final RegistrySupplier<Item> WHITE_YAK_WOOL_ITEM = ITEMS.register("white_yak_wool",
            () -> new BlockItem(WHITE_YAK_WOOL.get(), new Item.Properties()
                    .setId(itemKey("white_yak_wool"))));

    public static final RegistrySupplier<Item> SALT = ITEMS.register("salt",
            () -> new Item(new Item.Properties()
                    .setId(itemKey("salt"))));

    public static final RegistrySupplier<Item> YAK_HIDE = ITEMS.register("yak_hide",
            () -> new Item(new Item.Properties()
                    .setId(itemKey("yak_hide"))));

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(NomadCaravans.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<Block> TENT_ROOF_BLOCK = BLOCKS.register("tent_roof_block",
            () -> new TentRoofBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()
                    .noLootTable()
                    .setId(blockKey("tent_roof_block"))));

    public static final RegistrySupplier<Block> YURT_TENT = BLOCKS.register("yurt_tent",
            () -> new YurtTentBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .noOcclusion()
                    .noLootTable()
                    .setId(blockKey("yurt_tent"))));

    public static final RegistrySupplier<Block> TIPI_TENT = BLOCKS.register("tipi_tent",
            () -> new TipiTentBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .noOcclusion()
                    .noLootTable()
                    .setId(blockKey("tipi_tent"))));

    public static final RegistrySupplier<Item> YURT_TENT_ITEM = ITEMS.register("yurt_tent",
            () -> new TentBlockItem(YURT_TENT.get(), new Item.Properties()
                    .stacksTo(1)
                    .setId(itemKey("yurt_tent"))));

    public static final RegistrySupplier<Item> TIPI_TENT_ITEM = ITEMS.register("tipi_tent",
            () -> new TentBlockItem(TIPI_TENT.get(), new Item.Properties()
                    .stacksTo(1)
                    .setId(itemKey("tipi_tent"))));

    public static final RegistrySupplier<Block> CAMP_FENCE_POST = BLOCKS.register("camp_fence_post",
            () -> new CampFencePostBlock(BlockBehaviour.Properties.of()
                    .strength(1.5F)
                    .noOcclusion()
                    .noLootTable()
                    .setId(blockKey("camp_fence_post"))));

    public static final RegistrySupplier<Item> CAMP_FENCE_POST_ITEM = ITEMS.register("camp_fence_post",
            () -> new BlockItem(CAMP_FENCE_POST.get(), new Item.Properties()
                    .setId(itemKey("camp_fence_post"))));

    public static final RegistrySupplier<BlockEntityType<YurtTentBlockEntity>> YURT_TENT_BE =
            BLOCK_ENTITY_TYPES.register("yurt_tent",
                    () -> NomadPlatform.blockEntityType(YurtTentBlockEntity::new, YURT_TENT.get()));

    public static final RegistrySupplier<BlockEntityType<TipiTentBlockEntity>> TIPI_TENT_BE =
            BLOCK_ENTITY_TYPES.register("tipi_tent",
                    () -> NomadPlatform.blockEntityType(TipiTentBlockEntity::new, TIPI_TENT.get()));

    public static final RegistrySupplier<CreativeModeTab> NOMAD_CARAVANS_TAB = CREATIVE_TABS.register(
            "nomad_caravans",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.nomadcaravans"))
                    .icon(() -> new ItemStack(YAK_SPAWN_EGG.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(YAK_SPAWN_EGG.get());
                        output.accept(WILD_YAK_SPAWN_EGG.get());
                        output.accept(WANDERING_TRADER_YAK_SPAWN_EGG.get());
                        output.accept(UNTAMEABLE_YAK_SPAWN_EGG.get());
                        output.accept(UNTAMEABLE_WILD_YAK_SPAWN_EGG.get());
                        output.accept(UNTAMEABLE_WANDERING_TRADER_YAK_SPAWN_EGG.get());
                        output.accept(CARAVAN_GUARD_SPAWN_EGG.get());
                        output.accept(CARAVAN_LEADER_SPAWN_EGG.get());
                        output.accept(CARAVAN_FOLLOWER_SPAWN_EGG.get());
                        output.accept(RIGHT_OF_YAK_OWNERSHIP.get());
                        output.accept(YAK_DEED.get());
                        output.accept(RAW_YAK_MEAT.get());
                        output.accept(COOKED_YAK_MEAT.get());
                        output.accept(DRIED_YAK_MEAT.get());
                        output.accept(BROWN_YAK_WOOL_ITEM.get());
                        output.accept(WHITE_YAK_WOOL_ITEM.get());
                        output.accept(GRAY_YAK_WOOL_ITEM.get());
                        output.accept(SALT.get());
                        output.accept(YAK_HIDE.get());
                        output.accept(NOMAD_HELMET.get());
                        output.accept(NOMAD_CHESTPLATE.get());
                        output.accept(NOMAD_LEGGINGS.get());
                        output.accept(NOMAD_BOOTS.get());
                        output.accept(NOMAD_BOW.get());
                        output.accept(NOMAD_SWORD.get());
                        output.accept(NOMAD_SHIELD.get());
                        output.accept(YURT_TENT_ITEM.get());
                        output.accept(TIPI_TENT_ITEM.get());

                    })
                    .build());

    private ModRegistries() {
    }

    private static ResourceKey<Item> itemKey(String path) {
        return ResourceKey.create(Registries.ITEM, NomadCaravans.id(path));
    }

    private static ResourceKey<Block> blockKey(String path) {
        return ResourceKey.create(Registries.BLOCK, NomadCaravans.id(path));
    }

    public static void init() {
        BLOCKS.register();
        BLOCK_ENTITY_TYPES.register();
        ENTITY_TYPES.register();
        ITEMS.register();
        CREATIVE_TABS.register();
        VILLAGER_TYPES.register();
        ModVillagerProfessions.init();
        WanderingTraderYakEscortHandler.init();
    }
}
