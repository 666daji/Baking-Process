package org.bakingprocess.registry;

import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.food.ModFoodComponents;
import org.bakingprocess.item.*;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BakingProcess.MOD_ID);

    // 工作方块
    public static final RegistryObject<Item> GRINDING_STONE = registerFromBlock(ModBlocks.GRINDING_STONE, () -> new Item.Properties(), GrindingStoneItem::new);
    public static final RegistryObject<Item> HEAT_RESISTANT_SLATE = fromBlock(ModBlocks.HEAT_RESISTANT_SLATE);
    public static final RegistryObject<Item> FIREWOOD = fromBlock(ModBlocks.FIREWOOD);
    public static final RegistryObject<Item> IRON_PLATE = fromBlock(ModBlocks.IRON_PLATE);

    // 工具
    public static final RegistryObject<Item> IRON_GARNISH_DISHES = fromBlock(ModBlocks.IRON_GARNISH_DISHES);
    public static final RegistryObject<Item> CUTTING_BOARD = fromBlock(ModBlocks.CUTTING_BOARD);
    public static final RegistryObject<Item> IRON_POTS = fromBlock(ModBlocks.IRON_POTS);
    public static final RegistryObject<Item> PLATE_LID = register("plate_lid", () -> new Item(new Item.Properties()));

    // 厨具
    public static final RegistryObject<Item> BREAD_SPATULA = registerFromBlock(ModBlocks.BREAD_SPATULA, () -> new Item.Properties(),
            ((block, settings) -> new ModSharpKitchenwareItem(block, settings, ModSharpKitchenwareItem.SpatulaMaterials.BREAD_SPATULA)));
    public static final RegistryObject<Item> KITCHEN_KNIFE = registerFromBlock(ModBlocks.KITCHEN_KNIFE, () -> new Item.Properties(),
            ((block, settings) -> new ModSharpKitchenwareItem(block, settings, ModSharpKitchenwareItem.SpatulaMaterials.KITCHEN_KNIFE)));

    // 粉尘
    public static final RegistryObject<Item> WHEAT_FLOUR = register("wheat_flour",
            () -> new FlourItem(new Item.Properties(), 0xFFF8E1, FlourItem.FlourType.WHEAT));
    public static final RegistryObject<Item> LAPIS_LAZULI_FLOUR = register("lapis_lazuli_flour",
            () -> new FlourItem(new Item.Properties(), 0x2666FF, FlourItem.FlourType.LAPIS_LAZULI));
    public static final RegistryObject<Item> COCOA_FLOUR = register("cocoa_flour",
            () -> new FlourItem(new Item.Properties(), 0x8B4513, FlourItem.FlourType.COCOA));
    public static final RegistryObject<Item> AMETHYST_FLOUR = register("amethyst_flour",
            () -> new FlourItem(new Item.Properties(), 0x8A2BE2, FlourItem.FlourType.AMETHYST));
    public static final RegistryObject<Item> SUGAR_FLOUR = register("sugar_flour",
            () -> new FlourItem(new Item.Properties().food(ModFoodComponents.SUGAR_FLOUR), 0xFFF5F5F0, FlourItem.FlourType.SUGAR));
    public static final RegistryObject<Item> SALT_FLOUR = register("salt_flour",
            () -> new FlourItem(new Item.Properties(), 0xFFFDFCF5, FlourItem.FlourType.SUGAR));

    // 粉尘袋
    public static final RegistryObject<Item> FLOUR_SACK = register("flour_sack",
            () -> new FlourSackItem(ModBlocks.FLOUR_SACK.get(), new Item.Properties().stacksTo(1)));

    // 奶制品
    public static final RegistryObject<Item> MILK_POTION = registerFromBlock(ModBlocks.MILK_POTION,
            () -> new Item.Properties().food(ModFoodComponents.MILK).stacksTo(16), FoodPotionItem::new);

    // 面食
    public static final RegistryObject<Item> DOUGH = fromBlock(ModBlocks.DOUGH);
    public static final RegistryObject<Item> HARD_BREAD = registerFromBlock(ModBlocks.HARD_BREAD, () -> new Item.Properties().food(ModFoodComponents.HARD_BREAD));
    public static final RegistryObject<Item> SMALL_BREAD_EMBRYO = fromBlock(ModBlocks.SMALL_BREAD_EMBRYO);
    public static final RegistryObject<Item> SMALL_BREAD = registerFromBlock(ModBlocks.SMALL_BREAD, () -> new Item.Properties().food(ModFoodComponents.SMALL_BREAD));
    public static final RegistryObject<Item> BAGUETTE = registerFromBlock(ModBlocks.BAGUETTE, () -> new Item.Properties().food(ModFoodComponents.BAGUETTE));
    public static final RegistryObject<Item> BAGUETTE_EMBRYO = fromBlock(ModBlocks.BAGUETTE_EMBRYO);
    public static final RegistryObject<Item> TOAST_DOUGH = register("toast_dough", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TOAST = registerFromBlock(ModBlocks.TOAST, () -> new Item.Properties().food(ModFoodComponents.TOAST));
    public static final RegistryObject<Item> CAKE_DOUGH = register("cake_dough", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BAKED_CAKE_EMBRYO = fromBlock(ModBlocks.BAKED_CAKE_EMBRYO);
    public static final RegistryObject<Item> HARD_BREAD_BOAT = registerFromBlock(ModBlocks.HARD_BREAD_BOAT,
            () -> new Item.Properties().food(ModFoodComponents.HARD_BREAD_BOAT), BreadBoatItem::new);
    public static final RegistryObject<Item> SALTY_DOUGH = fromBlock(ModBlocks.SALTY_DOUGH);

    // 切片食物
    public static final RegistryObject<Item> CARROT_SLICES = registerFromBlock(ModBlocks.CARROT_SLICES, () -> new Item.Properties().food(ModFoodComponents.CARROT_SLICES));
    public static final RegistryObject<Item> CARROT_HEAD = register("carrot_head", () -> new Item(new Item.Properties().food(ModFoodComponents.CARROT_HEAD)));
    public static final RegistryObject<Item> SEPARATE_POTATO_CUBES = registerFromBlock(ModBlocks.SEPARATE_POTATO_CUBES, () -> new Item.Properties().food(ModFoodComponents.SEPARATE_POTATO_CUBES));
    public static final RegistryObject<Item> POTATO_CUBES = registerFromBlock(ModBlocks.POTATO_CUBES, () -> new Item.Properties().food(ModFoodComponents.POTATO_CUBES));
    public static final RegistryObject<Item> SEPARATE_BAKED_POTATO_CUBES = registerFromBlock(ModBlocks.SEPARATE_BAKED_POTATO_CUBES, () -> new Item.Properties().food(ModFoodComponents.SEPARATE_COOKED_POTATO_CUBES));
    public static final RegistryObject<Item> BAKED_POTATO_CUBES = registerFromBlock(ModBlocks.BAKED_POTATO_CUBES, () -> new Item.Properties().food(ModFoodComponents.COOKED_POTATO_CUBES));
    public static final RegistryObject<Item> APPLE_SLICES = registerFromBlock(ModBlocks.APPLE_SLICES, () -> new Item.Properties().food(Foods.APPLE));
    public static final RegistryObject<Item> COD_CUBES = registerFromBlock(ModBlocks.COD_CUBES, () -> new Item.Properties().food(ModFoodComponents.COD_CUBES));
    public static final RegistryObject<Item> COD_HEAD = register("cod_head", () -> new Item(new Item.Properties().food(ModFoodComponents.COD_HEAD)));
    public static final RegistryObject<Item> COOKED_COD_CUBES = registerFromBlock(ModBlocks.COOKED_COD_CUBES, () -> new Item.Properties().food(ModFoodComponents.COOKED_COD_CUBES));
    public static final RegistryObject<Item> COOKED_COD_HEAD = register("cooked_cod_head", () -> new Item(new Item.Properties().food(ModFoodComponents.COOKED_COD_HEAD)));
    public static final RegistryObject<Item> SALMON_CUBES = registerFromBlock(ModBlocks.SALMON_CUBES, () -> new Item.Properties().food(ModFoodComponents.SALMON_CUBES));
    public static final RegistryObject<Item> COOKED_SALMON_CUBES = registerFromBlock(ModBlocks.COOKED_SALMON_CUBES, () -> new Item.Properties().food(ModFoodComponents.COOKED_SALMON_CUBES));
    public static final RegistryObject<Item> KITCHEN_WASTE = fromBlock(ModBlocks.KITCHEN_WASTE);

    // 模具
    public static final RegistryObject<Item> CAKE_EMBRYO_MOLD = registerFromBlock(ModBlocks.CAKE_EMBRYO_MOLD, () -> new Item.Properties(), MoldItem::new);
    public static final RegistryObject<Item> TOAST_EMBRYO_MOLD = registerFromBlock(ModBlocks.TOAST_EMBRYO_MOLD, () -> new Item.Properties(), MoldItem::new);

    // 调料
    public static final RegistryObject<Item> SALT_CUBES = fromBlock(ModBlocks.SALT_CUBES);

    // 矿物
    public static final RegistryObject<Item> SALT_ORE = fromBlock(ModBlocks.SALT_ORE);
    public static final RegistryObject<Item> DEEPSLATE_SALT_ORE = fromBlock(ModBlocks.DEEPSLATE_SALT_ORE);

    // 园艺联动
    public static final RegistryObject<Item> CLAY_POTS_EMBRYO = fromBlock(ModBlocks.CLAY_POTS_EMBRYO);
    public static final RegistryObject<Item> CLAY_POTS = fromBlock(ModBlocks.CLAY_POTS);

    private static RegistryObject<Item> register(String name, Supplier<Item> supplier) {
        return ITEMS.register(name, supplier);
    }

    private static RegistryObject<Item> fromBlock(RegistryObject<? extends Block> block) {
        return ITEMS.register(block.getId().getPath(),
                () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Item> registerFromBlock(RegistryObject<? extends Block> block,
                                                           Supplier<Item.Properties> propsSupplier) {
        return ITEMS.register(block.getId().getPath(),
                () -> new BlockItem(block.get(), propsSupplier.get()));
    }

    private static RegistryObject<Item> registerFromBlock(RegistryObject<? extends Block> block,
                                                           Supplier<Item.Properties> propsSupplier,
                                                           BiFunction<Block, Item.Properties, Item> itemFactory) {
        return ITEMS.register(block.getId().getPath(),
                () -> itemFactory.apply(block.get(), propsSupplier.get()));
    }

    public static void registerAll(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
