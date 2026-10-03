package com.fosder.pocketdimension.world;

import com.fosder.pocketdimension.PocketDimensionMod;
import com.fosder.pocketdimension.setup.ModBlocks;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.template.BlockMatchRuleTest;
import net.minecraft.world.gen.feature.template.RuleTest;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Generación de la Mena de Batería Estabilizadora.
 *
 * Idea de rareza pedida:
 * - End: 80% de biomas/end load habilitan la mena y con más venas por chunk.
 * - Nether: 5%, muy rara.
 * - Overworld: 1%, extremadamente rara.
 *
 * Nota: BiomeLoadingEvent trabaja por bioma; se usa un roll determinístico por nombre
 * de bioma para mantener la generación estable entre reinicios.
 */
@Mod.EventBusSubscriber(modid = PocketDimensionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PocketOreGeneration {
    private static final RuleTest END_STONE = new BlockMatchRuleTest(Blocks.END_STONE);
    private static final RuleTest NETHERRACK = new BlockMatchRuleTest(Blocks.NETHERRACK);

    private PocketOreGeneration() {}

    @SubscribeEvent
    public static void onBiomeLoading(BiomeLoadingEvent event) {
        Biome.Category category = event.getCategory();

        if (category == Biome.Category.THEEND) {
            // 80%: sale mucho más en el End.
            if (passesChance(event.getName(), 80)) {
                event.getGeneration().addFeature(
                        GenerationStage.Decoration.UNDERGROUND_ORES,
                        ore(END_STONE, 6, 8, 8, 72)
                );
            }
            return;
        }

        if (category == Biome.Category.NETHER) {
            // 5%: aparece muy raro en Nether.
            if (passesChance(event.getName(), 5)) {
                event.getGeneration().addFeature(
                        GenerationStage.Decoration.UNDERGROUND_ORES,
                        ore(NETHERRACK, 4, 1, 12, 112)
                );
            }
            return;
        }

        // 1%: casi inexistente en Overworld.
        if (passesChance(event.getName(), 1)) {
            event.getGeneration().addFeature(
                    GenerationStage.Decoration.UNDERGROUND_ORES,
                    ore(OreFeatureConfig.FillerBlockType.NATURAL_STONE, 3, 1, 4, 28)
            );
        }
    }

    private static ConfiguredFeature<?, ?> ore(RuleTest target, int veinSize, int veinsPerChunk, int minY, int maxY) {
        return Feature.ORE
                .configured(new OreFeatureConfig(target, ModBlocks.STABILIZER_BATTERY_ORE.get().defaultBlockState(), veinSize))
                .decorated(Placement.RANGE.configured(new TopSolidRangeConfig(minY, 0, maxY)))
                .squared()
                .count(veinsPerChunk);
    }

    private static boolean passesChance(ResourceLocation biomeName, int percent) {
        if (percent >= 100) return true;
        if (percent <= 0) return false;
        String key = biomeName == null ? "unknown" : biomeName.toString();
        int roll = Math.floorMod(key.hashCode(), 100);
        return roll < percent;
    }
}
