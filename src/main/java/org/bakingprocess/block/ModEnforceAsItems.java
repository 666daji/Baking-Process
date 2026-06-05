package org.bakingprocess.block;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.dfood.block.FoodBlock;
import org.bakingprocess.BakingProcess;

public class ModEnforceAsItems {
    public static final FoodBlock.EnforceAsItem HARD_BREAD_BOAT = createAsItem("hard_bread_boat");

    private static FoodBlock.EnforceAsItem createAsItem(String item){
        return () -> Registries.ITEM.get(new Identifier(BakingProcess.MOD_ID, item));
    }
}
