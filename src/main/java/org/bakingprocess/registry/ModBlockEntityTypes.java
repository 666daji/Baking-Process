package org.bakingprocess.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.integration.dfood.AssistedBlocks;
import org.bakingprocess.block.entity.*;
import org.bakingprocess.block.entity.BakingComplexFoodBlockEntity;
import org.bakingprocess.block.entity.BakingSuspiciousStewBlockEntity;

import java.util.Arrays;

public class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BakingProcess.MOD_ID);

    // 工作方块
    public static final RegistryObject<BlockEntityType<GrindingStoneBlockEntity>> GRINDING_STONE = register(
            "grinding_stone", GrindingStoneBlockEntity::new, ModBlocks.GRINDING_STONE);
    public static final RegistryObject<BlockEntityType<PotsBlockEntity>> POTS = register(
            "pots", PotsBlockEntity::new, ModBlocks.IRON_POTS, ModBlocks.CLAY_POTS);
    public static final RegistryObject<BlockEntityType<PlateBlockEntity>> PLATE = register(
            "plate", PlateBlockEntity::new, ModBlocks.IRON_PLATE);

    // UpPlaceBlock
    public static final RegistryObject<BlockEntityType<HeatResistantSlateBlockPileEntity>> HEAT_RESISTANT_SLATE = register(
            "heat_resistant_slate", HeatResistantSlateBlockPileEntity::new, ModBlocks.HEAT_RESISTANT_SLATE);
    public static final RegistryObject<BlockEntityType<DishesBlockEntity>> GARNISH_DISHES = register(
            "garnish_dishes", DishesBlockEntity::new, ModBlocks.IRON_GARNISH_DISHES);
    public static final RegistryObject<BlockEntityType<MoldBlockEntity>> MOLD = register(
            "mold", MoldBlockEntity::new, ModBlocks.CAKE_EMBRYO_MOLD, ModBlocks.TOAST_EMBRYO_MOLD);
    public static final RegistryObject<BlockEntityType<CuttingBoardBlockEntity>> CUTTING_BOARD = register(
            "cutting_board", CuttingBoardBlockEntity::new, ModBlocks.CUTTING_BOARD);

    // FoodBlock
    public static final RegistryObject<BlockEntityType<FlourSackBlockEntity>> FLOUR_SACK = register(
            "flour_sack", FlourSackBlockEntity::new, ModBlocks.FLOUR_SACK);

    // 整合
    public static final RegistryObject<BlockEntityType<BakingSuspiciousStewBlockEntity>> SUSPICIOUS_STEW = register(
            "suspicious_stew", BakingSuspiciousStewBlockEntity::new,
            AssistedBlocks.CRIPPLED_SUSPICIOUS_STEW);
    public static final RegistryObject<BlockEntityType<BakingComplexFoodBlockEntity>> COMPLEX_FOOD = register(
            "complex_food", BakingComplexFoodBlockEntity::new,
            ModBlocks.BREAD_SPATULA, ModBlocks.KITCHEN_KNIFE);

    // 其他
    public static final RegistryObject<BlockEntityType<CombustionFirewoodBlockEntity>> COMBUSTION_FIREWOOD = register(
            "combustion_firewood", CombustionFirewoodBlockEntity::new, ModBlocks.COMBUSTION_FIREWOOD);

    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(
            String name, BlockEntityType.BlockEntitySupplier<T> factory, RegistryObject<? extends Block>... blocks) {
        return BLOCK_ENTITY_TYPES.register(name, () -> {
            Block[] blockArray = Arrays.stream(blocks).map(RegistryObject::get).toArray(Block[]::new);
            return BlockEntityType.Builder.of(factory, blockArray).build(null);
        });
    }

    public static void registerAll(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
