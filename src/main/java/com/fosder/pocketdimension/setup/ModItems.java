package com.fosder.pocketdimension.setup;

import com.fosder.pocketdimension.PocketDimensionMod;
import com.fosder.pocketdimension.item.CuriosVoidStabilizerItem;
import com.fosder.pocketdimension.item.DimensionalKeyItem;
import com.fosder.pocketdimension.item.DimensionalRecipeBookItem;
import com.fosder.pocketdimension.item.HackerKeyItem;
import com.fosder.pocketdimension.item.PatchouliRecipeBookItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PocketDimensionMod.MOD_ID);

    public static final RegistryObject<Item> DIMENSIONAL_TABLE_ITEM = ITEMS.register("dimensional_table", () ->
            new BlockItem(ModBlocks.DIMENSIONAL_TABLE.get(), new Item.Properties()
                    .tab(PocketCreativeTab.TAB)
                    .stacksTo(64)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> STABILIZER_MACHINE_ITEM = ITEMS.register("stabilizer_machine", () ->
            new BlockItem(ModBlocks.STABILIZER_MACHINE.get(), new Item.Properties()
                    .tab(PocketCreativeTab.TAB)
                    .stacksTo(16)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> QUANTUM_AIR_GENERATOR_ITEM = ITEMS.register("quantum_air_generator", () ->
            new BlockItem(ModBlocks.QUANTUM_AIR_GENERATOR.get(), new Item.Properties()
                    .tab(PocketCreativeTab.TAB)
                    .stacksTo(16)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> STABILIZER_BATTERY_ORE_ITEM = ITEMS.register("stabilizer_battery_ore", () ->
            new BlockItem(ModBlocks.STABILIZER_BATTERY_ORE.get(), new Item.Properties()
                    .tab(PocketCreativeTab.TAB)
                    .stacksTo(64)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> DIMENSIONAL_PORTAL_ITEM = ITEMS.register("dimensional_portal", () ->
            new BlockItem(ModBlocks.DIMENSIONAL_PORTAL.get(), new Item.Properties()
                    .tab(PocketCreativeTab.TAB)
                    .stacksTo(16)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> DARK_DIMENSIONAL_RIFT_ITEM = ITEMS.register("dark_dimensional_rift", () ->
            new BlockItem(ModBlocks.DARK_DIMENSIONAL_RIFT.get(), new Item.Properties()
                    .tab(PocketCreativeTab.TAB)
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> STABILIZER_BATTERY = ITEMS.register("stabilizer_battery", () ->
            new Item(new Item.Properties()
                    .tab(PocketCreativeTab.TAB)
                    .stacksTo(64)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> VOID_SHARD = ITEMS.register("void_shard", () ->
            new Item(new Item.Properties()
                    .tab(PocketCreativeTab.TAB)
                    .stacksTo(64)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> POCKET_CORE = ITEMS.register("pocket_core", () ->
            new Item(new Item.Properties()
                    .tab(PocketCreativeTab.TAB)
                    .stacksTo(16)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> VOID_STABILIZER = ITEMS.register("void_stabilizer", () ->
            createVoidStabilizer(new Item.Properties()
                    .tab(PocketCreativeTab.TAB)
                    .stacksTo(16)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> SIZE_UPGRADE = ITEMS.register("size_upgrade", () ->
            new Item(new Item.Properties()
                    .tab(PocketCreativeTab.TAB)
                    .stacksTo(16)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> SECURITY_UPGRADE = ITEMS.register("security_upgrade", () ->
            new Item(new Item.Properties()
                    .tab(PocketCreativeTab.TAB)
                    .stacksTo(16)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> ENERGY_UPGRADE = ITEMS.register("energy_upgrade", () ->
            new Item(new Item.Properties()
                    .tab(PocketCreativeTab.TAB)
                    .stacksTo(64)
                    .rarity(Rarity.EPIC)));


    public static final RegistryObject<Item> DIMENSIONAL_KEY = ITEMS.register("dimensional_key", () ->
            new DimensionalKeyItem(new Item.Properties()
                    .tab(PocketCreativeTab.TAB)
                    .stacksTo(1)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> HACKER_KEY = ITEMS.register("hacker_key", () ->
            new HackerKeyItem(new Item.Properties()
                    .tab(PocketCreativeTab.TAB)
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> DIMENSIONAL_RECIPE_BOOK = ITEMS.register("dimensional_recipe_book", () ->
            createRecipeBook(new Item.Properties()
                    .tab(PocketCreativeTab.TAB)
                    .stacksTo(1)
                    .rarity(Rarity.RARE)));

    private ModItems() {}

    private static Item createVoidStabilizer(Item.Properties properties) {
        if (ModList.get().isLoaded("curios")) {
            return new CuriosVoidStabilizerItem(properties);
        }
        return new Item(properties);
    }

    private static Item createRecipeBook(Item.Properties properties) {
        if (ModList.get().isLoaded("patchouli")) {
            return new PatchouliRecipeBookItem(properties);
        }
        return new DimensionalRecipeBookItem(properties);
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
