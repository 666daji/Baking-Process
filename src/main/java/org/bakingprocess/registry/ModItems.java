package org.bakingprocess.registry;

import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.food.ModFoodComponents;
import org.bakingprocess.item.*;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(BakingProcess.MOD_ID);

    // 工作方块
    public static final DeferredHolder<Item, Item> GRINDING_STONE = registerFromBlock(ModBlocks.GRINDING_STONE, Item.Properties::new);
    public static final DeferredHolder<Item, Item> HEAT_RESISTANT_SLATE = fromBlock(ModBlocks.HEAT_RESISTANT_SLATE);
    public static final DeferredHolder<Item, Item> FIREWOOD = fromBlock(ModBlocks.FIREWOOD);
    public static final DeferredHolder<Item, Item> IRON_PLATE = fromBlock(ModBlocks.IRON_PLATE);

    // 工具
    public static final DeferredHolder<Item, Item> IRON_GARNISH_DISHES = fromBlock(ModBlocks.IRON_GARNISH_DISHES);
    public static final DeferredHolder<Item, Item> CUTTING_BOARD = fromBlock(ModBlocks.CUTTING_BOARD);
    public static final DeferredHolder<Item, Item> IRON_POTS = fromBlock(ModBlocks.IRON_POTS);
    public static final DeferredHolder<Item, Item> PLATE_LID = register("plate_lid", () -> new Item(new Item.Properties()));

    // 厨具
    public static final DeferredHolder<Item, Item> BREAD_SPATULA = registerFromBlock(ModBlocks.BREAD_SPATULA,
            () -> new Item.Properties().attributes(SwordItem.createAttributes(
                    ModSharpKitchenwareItem.SpatulaMaterials.BREAD_SPATULA, 4, -3.5F)),
            ((block, settings) -> new ModSharpKitchenwareItem(block, settings, ModSharpKitchenwareItem.SpatulaMaterials.BREAD_SPATULA)));
    public static final DeferredHolder<Item, Item> KITCHEN_KNIFE = registerFromBlock(ModBlocks.KITCHEN_KNIFE,
            () -> new Item.Properties().attributes(SwordItem.createAttributes(
                    ModSharpKitchenwareItem.SpatulaMaterials.KITCHEN_KNIFE, 1, 1)),
            ((block, settings) -> new ModSharpKitchenwareItem(block, settings, ModSharpKitchenwareItem.SpatulaMaterials.KITCHEN_KNIFE)));

    // 粉尘
    public static final DeferredHolder<Item, Item> WHEAT_FLOUR = register("wheat_flour",
            () -> new FlourItem(new Item.Properties(), 0xFFF8E1, FlourItem.FlourType.WHEAT));
    public static final DeferredHolder<Item, Item> LAPIS_LAZULI_FLOUR = register("lapis_lazuli_flour",
            () -> new FlourItem(new Item.Properties(), 0x2666FF, FlourItem.FlourType.LAPIS_LAZULI));
    public static final DeferredHolder<Item, Item> COCOA_FLOUR = register("cocoa_flour",
            () -> new FlourItem(new Item.Properties(), 0x8B4513, FlourItem.FlourType.COCOA));
    public static final DeferredHolder<Item, Item> AMETHYST_FLOUR = register("amethyst_flour",
            () -> new FlourItem(new Item.Properties(), 0x8A2BE2, FlourItem.FlourType.AMETHYST));
    public static final DeferredHolder<Item, Item> SUGAR_FLOUR = register("sugar_flour",
            () -> new FlourItem(new Item.Properties().food(ModFoodComponents.SUGAR_FLOUR), 0xFFF5F5F0, FlourItem.FlourType.SUGAR));
    public static final DeferredHolder<Item, Item> SALT_FLOUR = register("salt_flour",
            () -> new FlourItem(new Item.Properties(), 0xFFFDFCF5, FlourItem.FlourType.SUGAR));

    // 粉尘袋
    public static final DeferredHolder<Item, Item> FLOUR_SACK = register("flour_sack",
            () -> new FlourSackItem(ModBlocks.FLOUR_SACK.get(), new Item.Properties().stacksTo(1)));

    // 奶制品
    public static final DeferredHolder<Item, Item> MILK_POTION = registerFromBlock(ModBlocks.MILK_POTION,
            () -> new Item.Properties().food(ModFoodComponents.MILK).stacksTo(16), FoodPotionItem::new);

    // 面食
    public static final DeferredHolder<Item, Item> DOUGH = fromBlock(ModBlocks.DOUGH);
    public static final DeferredHolder<Item, Item> HARD_BREAD = registerFromBlock(ModBlocks.HARD_BREAD, () -> new Item.Properties().food(ModFoodComponents.HARD_BREAD));
    public static final DeferredHolder<Item, Item> SMALL_BREAD_EMBRYO = fromBlock(ModBlocks.SMALL_BREAD_EMBRYO);
    public static final DeferredHolder<Item, Item> SMALL_BREAD = registerFromBlock(ModBlocks.SMALL_BREAD, () -> new Item.Properties().food(ModFoodComponents.SMALL_BREAD));
    public static final DeferredHolder<Item, Item> BAGUETTE = registerFromBlock(ModBlocks.BAGUETTE, () -> new Item.Properties().food(ModFoodComponents.BAGUETTE));
    public static final DeferredHolder<Item, Item> BAGUETTE_EMBRYO = fromBlock(ModBlocks.BAGUETTE_EMBRYO);
    public static final DeferredHolder<Item, Item> TOAST_DOUGH = register("toast_dough", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> TOAST = registerFromBlock(ModBlocks.TOAST, () -> new Item.Properties().food(ModFoodComponents.TOAST));
    public static final DeferredHolder<Item, Item> CAKE_DOUGH = register("cake_dough", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BAKED_CAKE_EMBRYO = fromBlock(ModBlocks.BAKED_CAKE_EMBRYO);
    public static final DeferredHolder<Item, Item> HARD_BREAD_BOAT = registerFromBlock(ModBlocks.HARD_BREAD_BOAT,
            () -> new Item.Properties().food(ModFoodComponents.HARD_BREAD_BOAT), BreadBoatItem::new);
    public static final DeferredHolder<Item, Item> SALTY_DOUGH = fromBlock(ModBlocks.SALTY_DOUGH);

    // 切片食物
    public static final DeferredHolder<Item, Item> CARROT_SLICES = registerFromBlock(ModBlocks.CARROT_SLICES, () -> new Item.Properties().food(ModFoodComponents.CARROT_SLICES));
    public static final DeferredHolder<Item, Item> CARROT_HEAD = register("carrot_head", () -> new Item(new Item.Properties().food(ModFoodComponents.CARROT_HEAD)));
    public static final DeferredHolder<Item, Item> SEPARATE_POTATO_CUBES = registerFromBlock(ModBlocks.SEPARATE_POTATO_CUBES, () -> new Item.Properties().food(ModFoodComponents.SEPARATE_POTATO_CUBES));
    public static final DeferredHolder<Item, Item> POTATO_CUBES = registerFromBlock(ModBlocks.POTATO_CUBES, () -> new Item.Properties().food(ModFoodComponents.POTATO_CUBES));
    public static final DeferredHolder<Item, Item> SEPARATE_BAKED_POTATO_CUBES = registerFromBlock(ModBlocks.SEPARATE_BAKED_POTATO_CUBES, () -> new Item.Properties().food(ModFoodComponents.SEPARATE_COOKED_POTATO_CUBES));
    public static final DeferredHolder<Item, Item> BAKED_POTATO_CUBES = registerFromBlock(ModBlocks.BAKED_POTATO_CUBES, () -> new Item.Properties().food(ModFoodComponents.COOKED_POTATO_CUBES));
    public static final DeferredHolder<Item, Item> APPLE_SLICES = registerFromBlock(ModBlocks.APPLE_SLICES, () -> new Item.Properties().food(Foods.APPLE));
    public static final DeferredHolder<Item, Item> COD_CUBES = registerFromBlock(ModBlocks.COD_CUBES, () -> new Item.Properties().food(ModFoodComponents.COD_CUBES));
    public static final DeferredHolder<Item, Item> COD_HEAD = register("cod_head", () -> new Item(new Item.Properties().food(ModFoodComponents.COD_HEAD)));
    public static final DeferredHolder<Item, Item> COOKED_COD_CUBES = registerFromBlock(ModBlocks.COOKED_COD_CUBES, () -> new Item.Properties().food(ModFoodComponents.COOKED_COD_CUBES));
    public static final DeferredHolder<Item, Item> COOKED_COD_HEAD = register("cooked_cod_head", () -> new Item(new Item.Properties().food(ModFoodComponents.COOKED_COD_HEAD)));
    public static final DeferredHolder<Item, Item> SALMON_CUBES = registerFromBlock(ModBlocks.SALMON_CUBES, () -> new Item.Properties().food(ModFoodComponents.SALMON_CUBES));
    public static final DeferredHolder<Item, Item> COOKED_SALMON_CUBES = registerFromBlock(ModBlocks.COOKED_SALMON_CUBES, () -> new Item.Properties().food(ModFoodComponents.COOKED_SALMON_CUBES));
    public static final DeferredHolder<Item, Item> KITCHEN_WASTE = fromBlock(ModBlocks.KITCHEN_WASTE);

    // 模具
    public static final DeferredHolder<Item, Item> CAKE_EMBRYO_MOLD = registerFromBlock(ModBlocks.CAKE_EMBRYO_MOLD, Item.Properties::new, MoldItem::new);
    public static final DeferredHolder<Item, Item> TOAST_EMBRYO_MOLD = registerFromBlock(ModBlocks.TOAST_EMBRYO_MOLD, Item.Properties::new, MoldItem::new);

    // 调料
    public static final DeferredHolder<Item, Item> SALT_CUBES = fromBlock(ModBlocks.SALT_CUBES);

    // 矿物
    public static final DeferredHolder<Item, Item> SALT_ORE = fromBlock(ModBlocks.SALT_ORE);
    public static final DeferredHolder<Item, Item> DEEPSLATE_SALT_ORE = fromBlock(ModBlocks.DEEPSLATE_SALT_ORE);

    // 园艺联动
    public static final DeferredHolder<Item, Item> CLAY_POTS_EMBRYO = fromBlock(ModBlocks.CLAY_POTS_EMBRYO);
    public static final DeferredHolder<Item, Item> CLAY_POTS = fromBlock(ModBlocks.CLAY_POTS);

    private static DeferredHolder<Item, Item> register(String name, Supplier<Item> supplier) {
        return ITEMS.register(name, supplier);
    }

    private static DeferredHolder<Item, Item> fromBlock(DeferredHolder<Block, ? extends Block> block) {
        return ITEMS.register(block.getId().getPath(),
                () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static DeferredHolder<Item, Item> registerFromBlock(DeferredHolder<Block, ? extends Block> block,
                                                           Supplier<Item.Properties> propsSupplier) {
        return ITEMS.register(block.getId().getPath(),
                () -> new BlockItem(block.get(), propsSupplier.get()));
    }

    private static DeferredHolder<Item, Item> registerFromBlock(DeferredHolder<Block, ? extends Block> block,
                                                           Supplier<Item.Properties> propsSupplier,
                                                           BiFunction<Block, Item.Properties, Item> itemFactory) {
        return ITEMS.register(block.getId().getPath(),
                () -> itemFactory.apply(block.get(), propsSupplier.get()));
    }

    public static void registerAll(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
