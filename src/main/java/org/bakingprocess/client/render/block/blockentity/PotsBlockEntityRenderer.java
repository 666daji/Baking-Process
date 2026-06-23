package org.bakingprocess.client.render.block.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.bakingprocess.block.entity.PotsBlockEntity;
import org.bakingprocess.block.process.KneadingProcess;
import org.bakingprocess.client.render.model.ModModelLoader;

public class PotsBlockEntityRenderer implements BlockEntityRenderer<PotsBlockEntity> {
    protected final ModelBlockRenderer renderer;
    protected final ModelManager modelManager;

    // 加粉步骤模型
    private static final ResourceLocation MODEL_ADD_FLOUR_1 = ModModelLoader.createProcessModel("knead_add_flour_1");
    private static final ResourceLocation MODEL_ADD_FLOUR_2 = ModModelLoader.createProcessModel("knead_add_flour_2");
    private static final ResourceLocation MODEL_ADD_FLOUR_3 = ModModelLoader.createProcessModel("knead_add_flour_3");

    // 加水步骤模型
    private static final ResourceLocation MODEL_ADD_LIQUID_1 = ModModelLoader.createProcessModel("knead_add_liquid_1");
    private static final ResourceLocation MODEL_ADD_LIQUID_2 = ModModelLoader.createProcessModel("knead_add_liquid_2");
    private static final ResourceLocation MODEL_ADD_LIQUID_3 = ModModelLoader.createProcessModel("knead_add_liquid_3");

    // 揉面步骤模型
    private static final ResourceLocation MODEL_KNEAD_1 = ModModelLoader.createProcessModel("knead_knead_1");
    private static final ResourceLocation MODEL_KNEAD_2 = ModModelLoader.createProcessModel("knead_knead_2");

    public PotsBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        renderer = ctx.getBlockRenderDispatcher().getModelRenderer();
        modelManager = ctx.getBlockRenderDispatcher().getBlockModelShaper().getModelManager();
    }

    @Override
    public void render(PotsBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        matrices.pushPose();

        // 检查盆中是否有物品（流程完成状态）
        if (!entity.getItem(0).isEmpty()) {
            // 盆中有物品，渲染完成状态模型
            renderer.renderModel(matrices.last(),
                    vertexConsumers.getBuffer(ItemBlockRenderTypes.getChunkRenderType(entity.getBlockState())),
                    null,
                    modelManager.getModel(MODEL_KNEAD_2),
                    1.0f, 1.0f, 1.0f, light, overlay);
            matrices.popPose();
            return;
        }

        // 获取揉面流程
        KneadingProcess<PotsBlockEntity> process = entity.getKneadingProcess();
        if (process == null || !process.isActive()) {
            // 没有激活的揉面流程，只渲染基础模型
            matrices.popPose();
            return;
        }

        // 获取渲染状态
        KneadingProcess.KneadingState kneadingState = process.getState();

        // 根据渲染状态选择对应的模型
        ResourceLocation modelId = getModelForRenderState(kneadingState);
        if (modelId != null) {
            renderer.tesselateBlock(
                    entity.getLevel(),
                    modelManager.getModel(modelId),
                    entity.getBlockState(), entity.getBlockPos(),
                    matrices,
                    vertexConsumers.getBuffer(ItemBlockRenderTypes.getChunkRenderType(entity.getBlockState())),
                    true, RandomSource.create(),
                    entity.getBlockState().getSeed(entity.getBlockPos()), OverlayTexture.NO_OVERLAY
            );
        }

        matrices.popPose();
    }

    /**
     * 根据渲染状态获取对应的模型
     */
    private ResourceLocation getModelForRenderState(KneadingProcess.KneadingState state) {
        String currentStepId = state.currentStepId();

        // 特殊处理：该步骤计数为0，使用上一步的完成模型
        switch (currentStepId) {
            case KneadingProcess.STEP_ADD_FLOUR:
                // 加粉步骤：根据面粉数量选择模型
                int flourCount = state.flourCount();
                if (flourCount >= 3) {
                    return MODEL_ADD_FLOUR_3;
                } else if (flourCount == 2) {
                    return MODEL_ADD_FLOUR_2;
                } else {
                    return MODEL_ADD_FLOUR_1;
                }

            case KneadingProcess.STEP_ADD_LIQUID:
                // 加水步骤：如果液体数量为0且上一步是加粉，则使用加粉完成模型
                int liquidCount = state.liquidCount();
                if (liquidCount == 0) {
                    return MODEL_ADD_FLOUR_3;
                } else if (liquidCount >= 3) {
                    return MODEL_ADD_LIQUID_3;
                } else if (liquidCount == 2) {
                    return MODEL_ADD_LIQUID_2;
                } else {
                    return MODEL_ADD_LIQUID_1;
                }

            case KneadingProcess.STEP_ADD_EXTRA:
                // 加额外物品步骤：如果额外物品数量为0且上一步是加水，则使用加水完成模型
                return MODEL_ADD_LIQUID_3;

            case KneadingProcess.STEP_KNEAD:
                // 揉面步骤：如果揉面次数为0，使用上一步的模型
                int kneadCount = state.kneadingCount();
                if (kneadCount == 0) {
                    // 根据上一步决定使用哪个模型
                    if (KneadingProcess.STEP_ADD_EXTRA.equals(state.previousStepId())) {
                        // 来自额外物品步骤，使用加水完成模型
                        return MODEL_ADD_LIQUID_3;
                    } else if (KneadingProcess.STEP_ADD_LIQUID.equals(state.previousStepId())) {
                        // 直接来自加水步骤（跳过额外物品），使用加水完成模型
                        return MODEL_ADD_LIQUID_3;
                    }
                }

                // 根据揉面次数选择模型
                if (kneadCount >= 2) {
                    return MODEL_KNEAD_2;
                } else {
                    return MODEL_KNEAD_1;
                }

            default:
                return null;
        }
    }
}