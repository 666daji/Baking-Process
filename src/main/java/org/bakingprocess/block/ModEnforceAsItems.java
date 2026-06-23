package org.bakingprocess.block;

import org.bakingprocess.BakingProcess;
import org.dfood.block.FoodBlock;

public class ModEnforceAsItems {
    public static final FoodBlock.EnforceAsItem HARD_BREAD_BOAT = createAsItem("hard_bread_boat");

    private static FoodBlock.EnforceAsItem createAsItem(String item){
        return () -> BuiltInRegistries.field_41178.get(new ResourceLocation(BakingProcess.MOD_ID, item));
    }
}
