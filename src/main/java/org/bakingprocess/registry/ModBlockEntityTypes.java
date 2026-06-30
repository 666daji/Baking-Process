package org.bakingprocess.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.block.entity.*;
import org.bakingprocess.integration.dfood.AssistedBlocks;

import java.util.Arrays;

public class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BakingProcess.MOD_ID);

    // 工作方块
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GrindingStoneBlockEntity>> GRINDING_STONE = register(
            "grinding_stone", GrindingStoneBlockEntity::new, ModBlocks.GRINDING_STONE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PotsBlockEntity>> POTS = register(
            "pots", PotsBlockEntity::new, ModBlocks.IRON_POTS, ModBlocks.CLAY_POTS);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlateBlockEntity>> PLATE = register(
            "plate", PlateBlockEntity::new, ModBlocks.IRON_PLATE);

    // UpPlaceBlock
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeatResistantSlateBlockPileEntity>> HEAT_RESISTANT_SLATE = register(
            "heat_resistant_slate", HeatResistantSlateBlockPileEntity::new, ModBlocks.HEAT_RESISTANT_SLATE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DishesBlockEntity>> GARNISH_DISHES = register(
            "garnish_dishes", DishesBlockEntity::new, ModBlocks.IRON_GARNISH_DISHES);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MoldBlockEntity>> MOLD = register(
            "mold", MoldBlockEntity::new, ModBlocks.CAKE_EMBRYO_MOLD, ModBlocks.TOAST_EMBRYO_MOLD);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CuttingBoardBlockEntity>> CUTTING_BOARD = register(
            "cutting_board", CuttingBoardBlockEntity::new, ModBlocks.CUTTING_BOARD);

    // FoodBlock
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FlourSackBlockEntity>> FLOUR_SACK = register(
            "flour_sack", FlourSackBlockEntity::new, ModBlocks.FLOUR_SACK);

    // 整合
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BakingSuspiciousStewBlockEntity>> SUSPICIOUS_STEW = register(
            "suspicious_stew", BakingSuspiciousStewBlockEntity::new,
            AssistedBlocks.CRIPPLED_SUSPICIOUS_STEW);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BakingComplexFoodBlockEntity>> COMPLEX_FOOD = register(
            "complex_food", BakingComplexFoodBlockEntity::new,
            ModBlocks.BREAD_SPATULA, ModBlocks.KITCHEN_KNIFE);

    // 其他
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CombustionFirewoodBlockEntity>> COMBUSTION_FIREWOOD = register(
            "combustion_firewood", CombustionFirewoodBlockEntity::new, ModBlocks.COMBUSTION_FIREWOOD);

    @SafeVarargs
    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(
            String name, BlockEntityType.BlockEntitySupplier<T> factory, DeferredHolder<Block, ? extends Block>... blocks) {
        return BLOCK_ENTITY_TYPES.register(name, () -> {
            Block[] blockArray = Arrays.stream(blocks).map(DeferredHolder::get).toArray(Block[]::new);
            return BlockEntityType.Builder.of(factory, blockArray).build(null);
        });
    }

    public static void registerAll(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
