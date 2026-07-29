package com.sappyeddie.nomadcaravans;

import com.sappyeddie.nomadcaravans.entity.UntameableWanderingTraderYakEntity;
import com.sappyeddie.nomadcaravans.entity.UntameableWildYakEntity;
import com.sappyeddie.nomadcaravans.entity.UntameableYakEntity;
import dev.architectury.registry.level.biome.BiomeModifications;
import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;

public final class ModSpawns {

    private ModSpawns() {
    }

    public static final TagKey<Biome> SPAWNS_UNTAMEABLE_YAKS =
            TagKey.create(Registries.BIOME, NomadCaravans.id("spawns_untameable_yaks"));

    private static final int HERD_WEIGHT = 2;

    public static void init() {

        SpawnPlacementsRegistry.register(ModRegistries.UNTAMEABLE_YAK, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacementsRegistry.register(ModRegistries.UNTAMEABLE_WILD_YAK, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacementsRegistry.register(ModRegistries.UNTAMEABLE_WANDERING_TRADER_YAK, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);

        BiomeModifications.addProperties(
                context -> context.hasTag(SPAWNS_UNTAMEABLE_YAKS),

                (context, properties) ->
                        addSpawn(properties, ModRegistries.UNTAMEABLE_WILD_YAK.get(), HERD_WEIGHT, 1, 1));
    }

    private static void addSpawn(dev.architectury.hooks.level.biome.BiomeProperties.Mutable properties,
                                 EntityType<?> type, int weight, int min, int max) {
        properties.getSpawnProperties().addSpawn(MobCategory.CREATURE,
                new MobSpawnSettings.SpawnerData(type, min, max), weight);
    }
}
