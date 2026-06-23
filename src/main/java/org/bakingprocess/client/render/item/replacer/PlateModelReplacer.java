package org.bakingprocess.client.render.item.replacer;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import org.bakingprocess.block.PlateBlock;
import org.dfood.util.DFoodUtils;
import org.twcore.api.content.ContainerUtil;
import org.twcore.client.api.render.ReplaceItemModel;
import org.twcore.content.Content;

import java.util.Objects;

public class PlateModelReplacer {

    public static BakedModel ReplaceModel(ReplaceItemModel.ReplaceContext context) {
        Content content = ContainerUtil.extractContent(context.stack());

        if (content != null) {
            BlockState renderState = Objects.requireNonNull(DFoodUtils.getBlockStateFromItem(context.stack().getItem()))
                    .setValue(PlateBlock.IS_COVERED, true);

            return context.modelManager().getBlockModelShaper().getBlockModel(renderState);
        }

        return context.originalModel();
    }
}
