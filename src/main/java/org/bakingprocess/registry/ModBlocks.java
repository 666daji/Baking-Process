package org.bakingprocess.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.block.*;
import org.dfood.block.ComplexFoodBlock;
import org.dfood.block.FoodBlock;
import org.dfood.block.SimpleFoodBlock;
import org.dfood.shape.FoodShapeHandle;
import org.dfood.sound.ModSoundGroups;
import org.dfood.util.DFoodUtils;
import org.twcore.api.util.IntPropertyManager;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BakingProcess.MOD_ID);

    // 工作方块
    public static final RegistryObject<Block> GRINDING_STONE = register("grinding_stone",
            () -> new GrindingStoneBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.STONE).strength(1.5F, 6.0F)
                    .requiresCorrectToolForDrops().mapColor(MapColor.STONE)));
    public static final RegistryObject<Block> HEAT_RESISTANT_SLATE = register("heat_resistant_slate",
            () -> new HeatResistantSlateBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.STONE).strength(1.0F, 6.0F)
                    .requiresCorrectToolForDrops().mapColor(MapColor.DEEPSLATE)));
    public static final RegistryObject<Block> COMBUSTION_FIREWOOD = register("combustion_firewood",
            () -> new CombustionFirewoodBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.WOOD).strength(0.5F, 0.5F)
                    .mapColor(MapColor.COLOR_ORANGE).noOcclusion()
                    .noOcclusion().lightLevel(state -> state.getValue(CombustionFirewoodBlock.COMBUSTION_STATE).isBurning()? 15: 0)));
    public static final RegistryObject<Block> FIREWOOD = register("firewood",
            () -> FirewoodBlock.Builder.create()
                    .maxFood(6)
                    .targetBlock(COMBUSTION_FIREWOOD.get())
                    .settings(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BROWN).noOcclusion()
                            .sound(SoundType.WOOD).strength(0.5F, 0.5F))
                    .build());
    public static final RegistryObject<Block> IRON_PLATE = register("iron_plate",
            () -> new PlateBlock(BlockBehaviour.Properties.of().sound(SoundType.METAL)
                    .strength(0.2F, 0.6F).mapColor(MapColor.METAL)));

    // 工具
    public static final RegistryObject<Block> IRON_GARNISH_DISHES = register("iron_garnish_dishes",
            () -> new GarnishDishesBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.METAL).strength(0.5F)
                    .pushReaction(PushReaction.DESTROY)));
    public static final RegistryObject<Block> CUTTING_BOARD = register("cutting_board",
            () -> new CuttingBoardBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.WOOD).sound(SoundType.WOOL).strength(0.2F)
                    .pushReaction(PushReaction.DESTROY)));
    public static final RegistryObject<Block> IRON_POTS = register("iron_pots",
            () -> new PotsBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.METAL).strength(1.5F, 0.6F)));

    // 厨具
    public static final RegistryObject<Block> BREAD_SPATULA = register("bread_spatula",
            () -> ComplexFoodBlock.Builder.create()
                    .maxFood(1)
                    .simpleShape(Block.box(0, 0, 0, 16, 1, 16))
                    .settings(BlockBehaviour.Properties.of()
                            .sound(SoundType.STONE).strength(0.5F, 0.2F)
                            .noOcclusion())
                    .build());
    public static final RegistryObject<Block> KITCHEN_KNIFE = register("kitchen_knife",
            () -> ComplexFoodBlock.Builder.create()
                    .maxFood(1)
                    .simpleShape(Block.box(2, 0, 2, 14, 1, 14))
                    .settings(BlockBehaviour.Properties.of()
                            .sound(ModBlockSoundGroup.KITCHEN_KNIFE.get()).strength(0.5F, 0.2F)
                            .noOcclusion())
                    .build());

    // 粉尘袋
    public static final RegistryObject<Block> FLOUR_SACK = register("flour_sack",
            () -> FlourSackBlock.Builder.create()
                    .maxFood(2)
                    .settings(BlockBehaviour.Properties.of()
                            .sound(SoundType.WOOL).strength(0.5F)
                            .noOcclusion().pushReaction(PushReaction.DESTROY))
                    .build());

    // 奶制品
    public static final RegistryObject<Block> MILK_POTION = register("milk_potion",
            () -> FoodBlock.Builder.create()
                    .maxFood(3)
                    .settings(DFoodUtils.getFoodBlockSettings()
                            .sound(ModSoundGroups.POTION)
                            .mapColor(MapColor.SNOW))
                    .build());

    // 面食
    public static final RegistryObject<Block> DOUGH = register("dough",
            () -> new SimpleFoodBlock(DFoodUtils.getFoodBlockSettings()
                    .sound(SoundType.WOOL)
                    .mapColor(MapColor.SNOW)));
    public static final RegistryObject<Block> HARD_BREAD = register("hard_bread",
            () -> new SimpleFoodBlock(DFoodUtils.getFoodBlockSettings()
                    .sound(SoundType.WOOL)
                    .mapColor(MapColor.COLOR_ORANGE)));
    public static final RegistryObject<Block> SMALL_BREAD_EMBRYO = register("small_bread_embryo",
            () -> FoodBlock.Builder.create()
                    .maxFood(3)
                    .useItemTranslationKey(false)
                    .settings(DFoodUtils.getFoodBlockSettings()
                            .sound(SoundType.WOOL)
                            .mapColor(MapColor.SNOW))
                    .build());
    public static final RegistryObject<Block> SMALL_BREAD = register("small_bread",
            () -> FoodBlock.Builder.create()
                    .maxFood(2)
                    .useItemTranslationKey(false)
                    .settings(DFoodUtils.getFoodBlockSettings()
                            .sound(SoundType.WOOL)
                            .mapColor(MapColor.COLOR_ORANGE))
                    .build());
    public static final RegistryObject<Block> BAGUETTE = register("baguette",
            () -> new SimpleFoodBlock(DFoodUtils.getFoodBlockSettings()
                    .sound(SoundType.WOOL)
                    .mapColor(MapColor.COLOR_ORANGE)));
    public static final RegistryObject<Block> BAGUETTE_EMBRYO = register("baguette_embryo",
            () -> new SimpleFoodBlock(DFoodUtils.getFoodBlockSettings()
                    .sound(SoundType.WOOL)
                    .mapColor(MapColor.SNOW)));
    public static final RegistryObject<Block> TOAST = register("toast",
            () -> new SimpleFoodBlock(DFoodUtils.getFoodBlockSettings()
                    .sound(SoundType.WOOL)
                    .mapColor(MapColor.COLOR_ORANGE)));
    public static final RegistryObject<Block> BAKED_CAKE_EMBRYO = register("baked_cake_embryo",
            () -> new SimpleFoodBlock(DFoodUtils.getFoodBlockSettings()
                    .sound(SoundType.WOOL)
                    .mapColor(MapColor.SNOW)));
    public static final RegistryObject<Block> SALTY_DOUGH = register("salty_dough",
            () -> new SimpleFoodBlock(DFoodUtils.getFoodBlockSettings()
                    .sound(SoundType.WOOL)
                    .mapColor(MapColor.SNOW)));
    public static final RegistryObject<Block> SOUP_HARD_BREAD_BOAT = register("soup_hard_bread_boat",
            () -> {
                IntPropertyManager.preCache("bites", 0, 4);
                return new BreadBoatBlock(DFoodUtils.getFoodBlockSettings()
                        .sound(SoundType.WOOL).mapColor(MapColor.COLOR_ORANGE),
                        FoodShapeHandle.shapes.getShape(8), 4, ModEnforceAsItems.HARD_BREAD_BOAT);
            });
    public static final RegistryObject<Block> HARD_BREAD_BOAT = register("hard_bread_boat",
            () -> new EmptyBreadBoatBlock(DFoodUtils.getFoodBlockSettings()
                    .sound(SoundType.WOOL).mapColor(MapColor.COLOR_ORANGE),
                    (BreadBoatBlock) SOUP_HARD_BREAD_BOAT.get()));

    // 切片食物
    public static final RegistryObject<Block> CARROT_SLICES = register("carrot_slices",
            () -> FoodBlock.Builder.create()
                    .maxFood(3)
                    .useItemTranslationKey(false)
                    .settings(DFoodUtils.getFoodBlockSettings())
                    .build());
    public static final RegistryObject<Block> SEPARATE_POTATO_CUBES = register("separate_potato_cubes",
            () -> FoodBlock.Builder.create()
                    .maxFood(4)
                    .useItemTranslationKey(false)
                    .settings(DFoodUtils.getFoodBlockSettings())
                    .build());
    public static final RegistryObject<Block> POTATO_CUBES = register("potato_cubes",
            () -> FoodBlock.Builder.create()
                    .maxFood(1)
                    .useItemTranslationKey(false)
                    .settings(DFoodUtils.getFoodBlockSettings())
                    .build());
    public static final RegistryObject<Block> SEPARATE_BAKED_POTATO_CUBES = register("separate_baked_potato_cubes",
            () -> FoodBlock.Builder.create()
                    .maxFood(4)
                    .useItemTranslationKey(false)
                    .settings(DFoodUtils.getFoodBlockSettings())
                    .build());
    public static final RegistryObject<Block> BAKED_POTATO_CUBES = register("baked_potato_cubes",
            () -> FoodBlock.Builder.create()
                    .maxFood(1)
                    .useItemTranslationKey(false)
                    .settings(DFoodUtils.getFoodBlockSettings())
                    .build());
    public static final RegistryObject<Block> APPLE_SLICES = register("apple_slices",
            () -> FoodBlock.Builder.create()
                    .maxFood(2)
                    .useItemTranslationKey(false)
                    .settings(DFoodUtils.getFoodBlockSettings())
                    .build());
    public static final RegistryObject<Block> COD_CUBES = register("cod_cubes",
            () -> FoodBlock.Builder.create()
                    .maxFood(4)
                    .useItemTranslationKey(false)
                    .settings(DFoodUtils.getFoodBlockSettings()
                            .sound(ModSoundGroups.FISH))
                    .build());
    public static final RegistryObject<Block> COOKED_COD_CUBES = register("cooked_cod_cubes",
            () -> FoodBlock.Builder.create()
                    .maxFood(4)
                    .useItemTranslationKey(false)
                    .settings(DFoodUtils.getFoodBlockSettings()
                            .sound(ModSoundGroups.FISH))
                    .build());
    public static final RegistryObject<Block> SALMON_CUBES = register("salmon_cubes",
            () -> FoodBlock.Builder.create()
                    .maxFood(3)
                    .useItemTranslationKey(false)
                    .settings(DFoodUtils.getFoodBlockSettings()
                            .sound(ModSoundGroups.FISH))
                    .build());
    public static final RegistryObject<Block> COOKED_SALMON_CUBES = register("cooked_salmon_cubes",
            () -> FoodBlock.Builder.create()
                    .maxFood(3)
                    .useItemTranslationKey(false)
                    .settings(DFoodUtils.getFoodBlockSettings()
                            .sound(ModSoundGroups.FISH))
                    .build());
    public static final RegistryObject<Block> KITCHEN_WASTE = register("kitchen_waste",
            () -> new SimpleFoodBlock(DFoodUtils.getFoodBlockSettings()
                    .sound(ModSoundGroups.FISH)));

    // 模具
    public static final RegistryObject<Block> CAKE_EMBRYO_MOLD = register("cake_embryo_mold",
            () -> new MoldBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.METAL).strength(0.5F).noOcclusion()));
    public static final RegistryObject<Block> TOAST_EMBRYO_MOLD = register("toast_embryo_mold",
            () -> new MoldBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.METAL).strength(0.5F).noOcclusion()));

    // 调料
    public static final RegistryObject<Block> SALT_CUBES = register("salt_cubes",
            () -> FoodBlock.Builder.create()
                    .maxFood(2)
                    .useItemTranslationKey(true)
                    .settings(DFoodUtils.getFoodBlockSettings())
                    .build());

    // 矿物
    public static final RegistryObject<Block> SALT_ORE = register("salt_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.STONE).strength(1.5F, 6.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> DEEPSLATE_SALT_ORE = register("deepslate_salt_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.STONE).strength(2.0F, 6.0F).requiresCorrectToolForDrops()));

    // 园艺联动
    public static final RegistryObject<Block> CLAY_POTS_EMBRYO = register("clay_pots_embryo",
            () -> new SimpleFoodBlock(DFoodUtils.getFoodBlockSettings(), false, PotsBlock.SHAPE));
    public static final RegistryObject<Block> CLAY_POTS = register("clay_pots",
            () -> new PotsBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.STONE).strength(0.5F, 0.6F)));

    private static RegistryObject<Block> register(String name, Supplier<Block> supplier) {
        return BLOCKS.register(name, supplier);
    }

    public static void registerAll(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
