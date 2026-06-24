package org.bakingprocess.client.render.block.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.model.data.ModelData;
import org.bakingprocess.block.CuttingBoardBlock;
import org.bakingprocess.block.entity.CuttingBoardBlockEntity;
import org.bakingprocess.block.process.CuttingProcess;
import org.bakingprocess.client.render.model.ModModelLoader;
import org.twcore.client.api.render.UpPlaceBlockEntityRenderer;

import java.util.HashMap;
import java.util.Map;

public class CuttingBoardBlockEntityRenderer extends UpPlaceBlockEntityRenderer<CuttingBoardBlockEntity> {
    public static final Map<Item, Float> ITEM_ROTATIONS = new HashMap<>();

    static {
        ITEM_ROTATIONS.put(Items.COD, -90f);
        ITEM_ROTATIONS.put(Items.COOKED_COD, -90f);
        ITEM_ROTATIONS.put(Items.SALMON, -90f);
        ITEM_ROTATIONS.put(Items.COOKED_SALMON, -90f);
    }

    private final ModelManager modelManager;
    private final ModelBlockRenderer modelRenderer;

    public CuttingBoardBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
        this.modelManager = ctx.getBlockRenderDispatcher().getBlockModelShaper().getModelManager();
        this.modelRenderer = ctx.getBlockRenderDispatcher().getModelRenderer();
    }

    /**
     * 获取切割模型
     * @param itemStack 正在切割的物品
     * @param cutCount 当前切割次数（从1开始）
     * @param manager BakedModelManager
     * @return 对应的切割模型，如果不存在则返回null
     */
    public static BakedModel getCuttingModel(ItemStack itemStack, int cutCount, ModelManager manager) {
        if (itemStack.isEmpty() || cutCount < 1) {
            return null;
        }

        // 获取物品的完整标识符
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(itemStack.getItem());

        // 构建切割模型ID
        ResourceLocation modelId = ModModelLoader.createCuttingModel(itemId, cutCount);
        BakedModel model = manager.getModel(modelId);

        // 检查是否是有效模型（不是错误模型）
        if (model == manager.getMissingModel()) {
            return null;
        }

        return model;
    }

    @Override
    public void render(CuttingBoardBlockEntity entity, float tickDelta, PoseStack matrices,
                       MultiBufferSource vertexConsumers, int light, int overlay) {
        CuttingProcess<CuttingBoardBlockEntity> cuttingProcess = entity.getCuttingProcess();
        ItemStack currentStack = cuttingProcess.isActive()?
                cuttingProcess.getState().inputStack():
                entity.getItem(0);

        matrices.pushPose();
        matrices.translate(0, 0.1, 0);
        matrices.translate(0.5, 0, 0.5);

        // 物品特定旋转
        if (ITEM_ROTATIONS.containsKey(currentStack.getItem())) {
            matrices.mulPose(Axis.YP.rotationDegrees(ITEM_ROTATIONS.get(currentStack.getItem())));
        }

        // 流程模型旋转
        if (cuttingProcess.isActive()) {
            Direction facing = entity.getBlockState().getValue(CuttingBoardBlock.FACING);
            matrices.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        }

        matrices.translate(-0.5, 0, -0.5);

        // 优先尝试渲染切割模型
        if (cuttingProcess.isActive()) {
            renderCuttingModel(entity, matrices, vertexConsumers);
        } else {
            // 如果没有切割模型，渲染默认物品
            fromStackRender(currentStack, entity, tickDelta, matrices, vertexConsumers, light, overlay);
        }

        matrices.popPose();
    }

    /**
     * 渲染切割模型
     */
    private void renderCuttingModel(CuttingBoardBlockEntity entity, PoseStack matrices, MultiBufferSource vertexConsumers) {
        CuttingProcess<CuttingBoardBlockEntity> process = entity.getCuttingProcess();

        if (process == null || !process.isActive()) {
            return;
        }

        CuttingProcess.CuttingState state = process.getState();
        if (state == null || state.inputStack() == null) {
            return;
        }

        int currentCut = state.currentCut();
        if (currentCut < 1) {
            return;
        }

        BakedModel model = getCuttingModel(state.inputStack(), currentCut, modelManager);

        if (model == null || model == modelManager.getMissingModel()) {
            return;
        }

        // 渲染切割模型
        if (entity.getLevel() != null) {
            modelRenderer.tesselateBlock(
                    entity.getLevel(),
                    model,
                    entity.getBlockState(),
                    entity.getBlockPos(),
                    matrices,
                    vertexConsumers.getBuffer(RenderType.cutout()),
                    true,
                    RandomSource.create(),
                    entity.getBlockState().getSeed(entity.getBlockPos()),
                    OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY,
                    RenderType.cutout()
            );
        }
    }
}