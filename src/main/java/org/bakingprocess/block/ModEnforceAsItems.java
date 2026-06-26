package org.bakingprocess.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.bakingprocess.BakingProcess;
import org.dfood.block.FoodBlock;

public class ModEnforceAsItems {
    public static final FoodBlock.EnforceAsItem HARD_BREAD_BOAT = createAsItem("hard_bread_boat");

    private static FoodBlock.EnforceAsItem createAsItem(String item){
        return () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(BakingProcess.MOD_ID, item));
    }
}
