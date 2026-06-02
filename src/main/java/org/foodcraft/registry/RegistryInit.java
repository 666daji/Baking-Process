package org.foodcraft.registry;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import org.dfood.shape.Shapes;
import org.foodcraft.block.pile.CubeBlockPileManager;

public class RegistryInit {
    public static void init() {
        // 当前模组注册
        ModContents.registryContents();
        ModContainers.registryContainers();
        PlayerActions.registerDefaults();
        cubeBlockPileInit();
        registerShapes();

        // 原版注册
        ModBlocks.registerAll();
        ModItems.registerAll();
        ModBlockEntityTypes.registerAll();
        ModEntityTypes.registerAll();
        ModRecipeTypes.registerAll();
        ModItemGroups.registerAll();
        ModSounds.registerAll();
        ModOreGeneration.registerAll();
    }

    /**
     * 多方块初始化
     */
    private static void cubeBlockPileInit(){
        // 世界加载时恢复多方块数据
        ServerWorldEvents.LOAD.register((server, world) -> {
            if (!world.isClient()) {
                CubeBlockPileManager.loadWorldCubeBlockPiles(world);
            }
        });
        // 服务器停止时清理
        ServerLifecycleEvents.SERVER_STOPPING.register(CubeBlockPileManager::onServerStopping);
    }

    private static void registerShapes() {
        Shapes.shapeMap.put("foodcraft:flour_sack",new int[][]{
                {1, 2, 8}
        });

        // 奶制品
        Shapes.shapeMap.put("foodcraft:milk_potion",new int[][]{
                {1, 2, 8}, {3, 3, 1}
        });

        // 面包
        Shapes.shapeMap.put("foodcraft:small_bread_embryo",new int[][]{
                {1, 1, 12}
        });
        Shapes.shapeMap.put("foodcraft:small_bread",new int[][]{
                {1, 1, 12}
        });
        Shapes.shapeMap.put("foodcraft:baguette_embryo",new int[][]{
                {1, 1, 2}
        });
        Shapes.shapeMap.put("foodcraft:baguette",new int[][]{
                {1, 1, 2}
        });
        Shapes.shapeMap.put("foodcraft:hard_bread",new int[][]{
                {1, 1, 8}
        });
        Shapes.shapeMap.put("foodcraft:dough",new int[][]{
                {1, 1, 12}
        });
        Shapes.shapeMap.put("foodcraft:cake_embryo",new int[][]{
                {1, 1, 1}
        });
        Shapes.shapeMap.put("foodcraft:hard_bread_boat",new int[][]{
                {1, 1, 8}
        });
        Shapes.shapeMap.put("foodcraft:mushroom_stew_hard_bread_boat",new int[][]{
                {1, 1, 8}
        });
        Shapes.shapeMap.put("foodcraft:beetroot_soup_hard_bread_boat",new int[][]{
                {1, 1, 8}
        });

        //其他
        Shapes.shapeMap.put("foodcraft:firewood",new int[][]{
                {1, 1, 1},{2, 2, 2},{3, 3, 3},{4, 4, 4},{5, 6, 5}
        });

        // 陶艺品胚
        Shapes.shapeMap.put("foodcraft:flower_pot_embryo",new int[][]{
                {1, 1, 7}, {2, 4, 1}
        });
    }
}
