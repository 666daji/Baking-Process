package org.bakingprocess.client.render.block.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.block.PlateBlock;
import org.bakingprocess.block.entity.PlateBlockEntity;
import org.bakingprocess.client.render.model.ModModelLoader;
import org.bakingprocess.client.render.model.PlatingModelManager;
import org.bakingprocess.content.DishesContent;
import org.dfood.block.FoodBlock;
import org.twcore.TWCore;

import java.util.Optional;

public class PlateBlockEntityRenderer implements BlockEntityRenderer<PlateBlockEntity> {
    private final ModelManager modelManager;
    private final ModelBlockRenderer modelRenderer;

    public PlateBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.modelManager = context.getBlockRenderDispatcher().getBlockModelShaper().getModelManager();
        this.modelRenderer = context.getBlockRenderDispatcher().getModelRenderer();
    }

    @Override
    public void render(PlateBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        if (entity.getBlockState().getValue(PlateBlock.IS_COVERED)) {
            return;
        }

        BlockState state = entity.getBlockState();
        Item item = state.getBlock().asItem();
        PlatingModelManager manager = PlatingModelManager.getInstance();
        ResourceLocation renderModelId = manager.getModelForActions(item, entity.getPerformedActions());

        if (entity.getOutcome() != null) {
            renderModelId = ModModelLoader.createDishesModel(item, entity.getOutcome());
        }

        if (entity.getEatProcess().isActive()) {
            DishesContent outcome = entity.getOutcome();
            if (outcome != null) {
                int eaten = entity.getEatProcess().getEatenCount();
                int total = entity.getEatProcess().getTotalEats();
                if (eaten >= 0 && eaten < total) {
                    renderModelId = ModModelLoader.createEatStageModel(item, outcome, eaten);
                }
            }
        }

        // 获取模型
        BakedModel renderModel = modelManager.getModel(Optional.ofNullable(renderModelId).orElse(TWCore.createResourceLocation(BakingProcess.MOD_ID, "missing")));

        // 渲染最终模型
        if (!renderModel.equals(modelManager.getMissingModel())) {
            matrices.pushPose();
            matrices.translate(0.5, 0, 0.5);
            float facing = state.getValue(FoodBlock.FACING).toYRot();
            matrices.mulPose(Axis.YP.rotationDegrees(facing));
            matrices.translate(-0.5, 0, -0.5);

            if (entity.getLevel() != null) {
                modelRenderer.tesselateBlock(
                        entity.getLevel(),
                        renderModel,
                        state,
                        entity.getBlockPos(),
                        matrices,
                        vertexConsumers.getBuffer(RenderType.cutout()),
                        true,
                        RandomSource.create(),
                        state.getSeed(entity.getBlockPos()),
                        OverlayTexture.NO_OVERLAY,
                        ModelData.EMPTY,
                        RenderType.cutout()
                );
            }

            matrices.popPose();
        }
    }
}
