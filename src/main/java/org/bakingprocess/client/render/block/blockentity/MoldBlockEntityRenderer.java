package org.bakingprocess.client.render.block.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.bakingprocess.block.entity.MoldBlockEntity;
import org.bakingprocess.client.render.model.ModModelLoader;
import org.bakingprocess.content.ShapedDoughContent;

public class MoldBlockEntityRenderer implements BlockEntityRenderer<MoldBlockEntity> {
    private final ModelManager modelManager;
    private final ModelBlockRenderer modelRenderer;

    public MoldBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.modelManager = ctx.getBlockRenderDispatcher().getBlockModelShaper().getModelManager();
        this.modelRenderer = ctx.getBlockRenderDispatcher().getModelRenderer();
    }

    @Override
    public void render(MoldBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        ShapedDoughContent content = entity.getShapedDough();

        if (content != null) {
            renderContent(content, entity.getBlockState(), entity.getBlockPos(), entity.getLevel(), modelManager, modelRenderer, matrices, vertexConsumers);
        }
    }

    /**
     * 在模具中渲染定型面团。
     *
     * @param content 要渲染的定型面团
     */
    public static void renderContent(ShapedDoughContent content, BlockState state, BlockPos pos, Level world,
                                     ModelManager modelManager, ModelBlockRenderer modelRenderer,
                                     PoseStack matrices, MultiBufferSource vertexConsumers) {
        matrices.pushPose();
        matrices.translate(0, 0.1, 0);
        BakedModel renderModel = modelManager.getModel(ModModelLoader.createShapedDoughModel(content));

        if (renderModel != null) {
            // 渲染切割模型
            modelRenderer.tesselateBlock(
                    world, renderModel, state, pos,
                    matrices, vertexConsumers.getBuffer(RenderType.cutout()),
                    true, RandomSource.create(), state.getSeed(pos), OverlayTexture.NO_OVERLAY
            );
        }

        matrices.popPose();
    }
}
