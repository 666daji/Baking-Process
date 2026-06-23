package org.bakingprocess.client.render.item.replacer;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.bakingprocess.block.EmptyBreadBoatBlock;
import org.bakingprocess.container.BreadBoatContainer;
import org.bakingprocess.item.BreadBoatItem;
import org.twcore.api.content.ContainerUtil;
import org.twcore.client.api.render.ReplaceItemModel;
import org.twcore.content.Content;

public class BreadBoatModelReplacer {

    public static BakedModel ReplaceModel(ReplaceItemModel.ReplaceContext context) {
        ItemStack stack = context.stack();

        if (stack.getItem() instanceof BreadBoatItem containerItem) {
            BlockState blockState = containerItem.getBlock().defaultBlockState();
            Content content = ContainerUtil.extractContent(stack);
            BreadBoatContainer.BreadBoatSoupType soupType = BreadBoatContainer.BreadBoatSoupType.fromContent(content);
            if (soupType != null) {
                blockState = EmptyBreadBoatBlock.asTargetState(blockState, soupType);
            }
            BlockModelShaper blockModels = context.modelManager().getBlockModelShaper();

            return blockModels.getBlockModel(blockState);
        }

        return context.originalModel();
    }
}
