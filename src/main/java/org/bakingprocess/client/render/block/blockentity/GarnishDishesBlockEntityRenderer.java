package org.bakingprocess.client.render.block.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.bakingprocess.block.entity.DishesBlockEntity;
import org.twcore.client.api.render.UpPlaceBlockEntityRenderer;

public class GarnishDishesBlockEntityRenderer extends UpPlaceBlockEntityRenderer<DishesBlockEntity> {

    public GarnishDishesBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(DishesBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        if (!entity.isEmpty()) {
            matrices.pushPose();
            matrices.translate(0.0, 0.1, 0.0);

            fromStackRender(entity.getItem(0), entity, tickDelta, matrices, vertexConsumers, light, overlay);
            matrices.popPose();
        }
    }
}
