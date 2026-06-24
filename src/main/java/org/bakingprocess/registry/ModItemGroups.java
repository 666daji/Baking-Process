package org.bakingprocess.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.block.PlateBlock;
import org.bakingprocess.item.BreadBoatItem;
import org.twcore.api.TwModManager;

public class ModItemGroups {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BakingProcess.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemgroup.baking_process"))
                    .icon(() -> new ItemStack(ModItems.GRINDING_STONE.get()))
                    .displayItems(((displayContext, entries) -> {
                        // 工作方块
                        entries.accept(ModItems.GRINDING_STONE.get());
                        entries.accept(ModItems.HEAT_RESISTANT_SLATE.get());
                        entries.accept(ModItems.FIREWOOD.get());
                        entries.accept(ModItems.IRON_PLATE.get());

                        // 工具
                        entries.accept(ModItems.IRON_GARNISH_DISHES.get());
                        entries.accept(ModItems.CUTTING_BOARD.get());
                        entries.accept(ModItems.IRON_POTS.get());
                        entries.accept(ModItems.PLATE_LID.get());

                        // 厨具
                        entries.accept(ModItems.BREAD_SPATULA.get());
                        entries.accept(ModItems.KITCHEN_KNIFE.get());

                        // 粉尘
                        entries.accept(ModItems.WHEAT_FLOUR.get());
                        entries.accept(ModItems.LAPIS_LAZULI_FLOUR.get());
                        entries.accept(ModItems.COCOA_FLOUR.get());
                        entries.accept(ModItems.AMETHYST_FLOUR.get());
                        entries.accept(ModItems.SUGAR_FLOUR.get());
                        entries.accept(ModItems.SALT_FLOUR.get());

                        // 粉尘袋
                        entries.accept(ModItems.FLOUR_SACK.get());

                        // 奶制品
                        entries.accept(ModItems.MILK_POTION.get());

                        // 面食
                        entries.accept(ModItems.DOUGH.get());
                        entries.accept(ModItems.HARD_BREAD.get());
                        entries.accept(ModItems.SMALL_BREAD_EMBRYO.get());
                        entries.accept(ModItems.SMALL_BREAD.get());
                        entries.accept(ModItems.BAGUETTE.get());
                        entries.accept(ModItems.BAGUETTE_EMBRYO.get());
                        entries.accept(ModItems.TOAST_DOUGH.get());
                        entries.accept(ModItems.TOAST.get());
                        entries.accept(ModItems.CAKE_DOUGH.get());
                        entries.accept(ModItems.BAKED_CAKE_EMBRYO.get());
                        entries.accept(ModItems.SALTY_DOUGH.get());
                        entries.accept(ModItems.HARD_BREAD_BOAT.get());
                        entries.acceptAll(BreadBoatItem.getAll((BreadBoatItem) ModItems.HARD_BREAD_BOAT.get()));

                        // 切片
                        entries.accept(ModItems.CARROT_SLICES.get());
                        entries.accept(ModItems.CARROT_HEAD.get());
                        entries.accept(ModItems.SEPARATE_POTATO_CUBES.get());
                        entries.accept(ModItems.POTATO_CUBES.get());
                        entries.accept(ModItems.SEPARATE_BAKED_POTATO_CUBES.get());
                        entries.accept(ModItems.BAKED_POTATO_CUBES.get());
                        entries.accept(ModItems.APPLE_SLICES.get());
                        entries.accept(ModItems.COD_CUBES.get());
                        entries.accept(ModItems.COD_HEAD.get());
                        entries.accept(ModItems.COOKED_COD_CUBES.get());
                        entries.accept(ModItems.COOKED_COD_HEAD.get());
                        entries.accept(ModItems.SALMON_CUBES.get());
                        entries.accept(ModItems.COOKED_SALMON_CUBES.get());
                        entries.accept(ModItems.KITCHEN_WASTE.get());

                        //模具
                        entries.accept(ModItems.CAKE_EMBRYO_MOLD.get());
                        entries.accept(ModItems.TOAST_EMBRYO_MOLD.get());

                        // 调味料
                        entries.accept(ModItems.SALT_CUBES.get());

                        // 矿物
                        entries.accept(ModItems.SALT_ORE.get());
                        entries.accept(ModItems.DEEPSLATE_SALT_ORE.get());

                        // 园艺联动
                        if (TwModManager.IMPL.isRegistered("gardening")) {
                            entries.accept(ModItems.CLAY_POTS_EMBRYO.get());
                            entries.accept(ModItems.CLAY_POTS.get());
                        }
                    }))
                    .build());

    public static final RegistryObject<CreativeModeTab> ALL_DISH_TAB = CREATIVE_TABS.register("all_dish",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemgroup.baking_process.plating"))
                    .icon(() -> new ItemStack(ModItems.GRINDING_STONE.get()))
                    .displayItems(((displayContext, entries) ->
                            entries.acceptAll(PlateBlock.getAll(ModItems.IRON_PLATE.get()))))
                    .build());

    public static void registerAll(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
