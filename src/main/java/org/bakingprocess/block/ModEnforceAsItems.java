package org.bakingprocess.block;

import net.minecraftforge.registries.ForgeRegistries;
import org.bakingprocess.BakingProcess;
import org.dfood.block.FoodBlock;
import org.twcore.TWCore;

public class ModEnforceAsItems {
    public static final FoodBlock.EnforceAsItem HARD_BREAD_BOAT = createAsItem("hard_bread_boat");

    private static FoodBlock.EnforceAsItem createAsItem(String item){
        return () -> ForgeRegistries.ITEMS.getValue(TWCore.createResourceLocation(BakingProcess.MOD_ID, item));
    }
}
