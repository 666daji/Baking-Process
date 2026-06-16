package org.bakingprocess.client.render.item.replacer;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import org.dfood.util.DFoodUtils;
import org.bakingprocess.block.PlateBlock;
import org.twcore.api.content.ContainerUtil;
import org.twcore.content.Content;

import java.util.Objects;

public class PlateModelReplacer {

    public static BakedModel ReplaceModel(ReplaceItemModel.ReplaceContext context) {
        Content content = ContainerUtil.extractContent(context.stack());

        if (content != null) {
            BlockState renderState = Objects.requireNonNull(DFoodUtils.getBlockStateFromItem(context.stack().getItem()))
                    .with(PlateBlock.IS_COVERED, true);

            return context.modelManager().getBlockModels().getModel(renderState);
        }

        return context.originalModel();
    }
}
