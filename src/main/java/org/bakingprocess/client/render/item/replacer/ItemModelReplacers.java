package org.bakingprocess.client.render.item.replacer;

import org.bakingprocess.registry.ModItems;
import org.twcore.client.api.render.ReplaceItemModel;

public class ItemModelReplacers {
    public static void registry() {
        ReplaceItemModel.registry(ModItems.FLOUR_SACK.get(), FlourSackModelReplacer::ReplaceModel);
        ReplaceItemModel.registry(ModItems.HARD_BREAD_BOAT.get(), BreadBoatModelReplacer::ReplaceModel);
        ReplaceItemModel.registry(ModItems.IRON_PLATE.get(), PlateModelReplacer::ReplaceModel);
    }
}
