package org.bakingprocess.integration.dfood;

import org.dfood.shape.Shapes;

public class DFoodInit {
    public static void init() {
        FoodBlocksModifier.FoodBlockAdd();
        AssistedBlocks.registerAssistedBlocks();

        // 注册形状
        Shapes.shapeMap.put("baking_process:crippled_rabbit_stew",new int[][]{
                {1, 4, 8}
        });
        Shapes.shapeMap.put("baking_process:crippled_mushroom_stew",new int[][]{
                {1, 4, 8}
        });
        Shapes.shapeMap.put("baking_process:crippled_beetroot_soup",new int[][]{
                {1, 4, 8}
        });
        Shapes.shapeMap.put("baking_process:crippled_suspicious_stew",new int[][]{
                {1, 4, 8}
        });
        Shapes.shapeMap.put("baking_process:crippled_milk_bucket", new int[][]{
                {1, 3, 8}
        });
        Shapes.shapeMap.put("baking_process:crippled_water_bucket", new int[][]{
                {1, 3, 8}
        });
    }
}
