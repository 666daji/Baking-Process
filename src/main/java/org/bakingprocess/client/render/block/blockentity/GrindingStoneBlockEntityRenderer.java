package org.bakingprocess.client.render.block.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.block.GrindingStoneBlock;
import org.bakingprocess.block.entity.GrindingStoneBlockEntity;
import org.bakingprocess.client.render.model.ModModelLayers;
import org.twcore.client.api.animation.WithAnimationBlockEntityRenderer;

public class GrindingStoneBlockEntityRenderer extends WithAnimationBlockEntityRenderer<GrindingStoneBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(BakingProcess.MOD_ID, "textures/blockentity/grinding_stone.png");
    private static final float MODEL_Y_TRANSLATION = 1.5f;
    private static final float MODEL_X_ROTATION = 180.0f;
    private static final float ITEM_Y_BASE = 0.45f;
    private static final float ITEM_Y_OFFSET_FACTOR = 0.005f;
    private static final float ITEM_SCALE = 0.5f;
    private static final float ITEM_X_ROTATION = 90.0f;

    private final ModelPart base;
    private final ModelPart top;
    private final ModelPart handle;
    private final ItemRenderer itemRenderer;

    public GrindingStoneBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
        ModelPart root = ctx.bakeLayer(ModModelLayers.GRINDING_STONE);
        this.base = root.getChild("base");
        this.top = root.getChild("top");
        this.handle = root.getChild("handle");

        registerModelPart("top", top);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();

        // 基础部分
        root.addOrReplaceChild("base", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-8.0F, -2.0F, -8.0F, 16.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(56, 37).addBox(-8.0F, -4.0F, 6.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(56, 33).addBox(-8.0F, -4.0F, -8.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(42, 55).addBox(6.0F, -4.0F, -6.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(56, 19).addBox(-8.0F, -4.0F, -6.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(64, 0).addBox(-1.5F, -6.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 23.0F, 0.0F));

        // 顶部部分
        root.addOrReplaceChild("top", CubeListBuilder.create()
                        .texOffs(42, 69).addBox(0.0F, -2.0F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(64, 7).addBox(5.0F, -2.0F, -2.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 38).addBox(-7.0F, -1.0F, -7.0F, 14.0F, 3.0F, 14.0F, new CubeDeformation(0.0F))
                        .texOffs(56, 41).addBox(0.0F, -2.0F, -7.0F, 7.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(64, 12).addBox(6.9F, -1.3F, -1.0F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 55).addBox(-7.0F, -2.0F, -7.0F, 7.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
                        .texOffs(56, 47).addBox(0.0F, -2.0F, 2.0F, 7.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        // 手柄部分
        root.addOrReplaceChild("handle", CubeListBuilder.create()
                        .texOffs(0, 19).addBox(-7.0F, -10.0F, -7.0F, 14.0F, 4.5F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(modelData, 128, 128);
    }

    @Override
    public void render(GrindingStoneBlockEntity entity, float tickDelta, PoseStack matrices,
                       MultiBufferSource vertexConsumers, int light, int overlay) {
        Level world = entity.getLevel();

        // 普通渲染（无世界上下文）
        if (world == null) {
            renderStaticModel(matrices, vertexConsumers, light, overlay);
            return;
        }

        BlockState state = entity.getBlockState();
        ItemStack output = new ItemStack(entity.getExpectedOutput());

        // 管理动画状态
        manageAnimationState(entity, tickDelta, state);

        matrices.pushPose();
        try {
            renderAnimatedModel(entity, tickDelta, matrices, vertexConsumers, light, overlay, state, output);
        } finally {
            matrices.popPose();
        }
    }

    /**
     * 渲染静态模型（无世界上下文时使用）
     */
    private void renderStaticModel(PoseStack matrices, MultiBufferSource vertexConsumers,
                                   int light, int overlay) {
        matrices.pushPose();
        resetAllModelParts();
        matrices.translate(0.5, MODEL_Y_TRANSLATION, 0.5);
        matrices.mulPose(Axis.XP.rotationDegrees(MODEL_X_ROTATION));

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.entityCutout(TEXTURE));
        base.render(matrices, vertexConsumer, light, overlay);
        top.render(matrices, vertexConsumer, light, overlay);
        handle.render(matrices, vertexConsumer, light, overlay);

        matrices.popPose();
    }

    /**
     * 渲染带动画的模型
     */
    private void renderAnimatedModel(GrindingStoneBlockEntity entity, float tickDelta, PoseStack matrices,
                                     MultiBufferSource vertexConsumers, int light, int overlay,
                                     BlockState state, ItemStack output) {
        matrices.translate(0.5, MODEL_Y_TRANSLATION, 0.5);
        float facing = state.getValue(GrindingStoneBlock.FACING).toYRot();
        matrices.mulPose(Axis.YP.rotationDegrees(-facing + 90));
        matrices.mulPose(Axis.XP.rotationDegrees(MODEL_X_ROTATION));

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.entityCutout(TEXTURE));
        base.render(matrices, vertexConsumer, light, overlay);
        top.render(matrices, vertexConsumer, light, overlay);
        handle.render(matrices, vertexConsumer, light, overlay);

        // 渲染产出物品
        if (!output.isEmpty()) {
            renderOutputItem(entity, tickDelta, matrices, vertexConsumers, light, overlay, output, state);
        }
    }

    /**
     * 管理动画状态的更新逻辑
     */
    private void manageAnimationState(GrindingStoneBlockEntity entity, float tickDelta, BlockState state) {
        // 更新动画状态
        if (entity.isGrinding()) {
            entity.grindingAnimationState.startIfNotRunning(entity.getAge());
        } else {
            entity.grindingAnimationState.stopAndKeepProgress();
        }

        // 应用动画
        applyAnimation(
                entity.grindingAnimationState,
                BlockAnimations.GRINDING_STONE_SPIN,
                getAnimationProgress(entity.getAge(), tickDelta),
                1.0F,
                1.0F
        );
    }

    /**
     * 渲染产出物品
     * 注意：这里需要先pop再push来重置矩阵状态，与原始代码保持一致
     */
    private void renderOutputItem(GrindingStoneBlockEntity entity, float tickDelta, PoseStack matrices,
                                  MultiBufferSource vertexConsumers, int light, int overlay,
                                  ItemStack output, BlockState state) {
        matrices.popPose();
        matrices.pushPose();

        float grindingProgress = entity.getGrindingProgress();
        matrices.translate(0.5, grindingProgress * ITEM_Y_OFFSET_FACTOR + ITEM_Y_BASE, 0.5);
        matrices.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        float facing = state.getValue(GrindingStoneBlock.FACING).toYRot();
        matrices.mulPose(Axis.YP.rotationDegrees(-facing));
        matrices.mulPose(Axis.XP.rotationDegrees(ITEM_X_ROTATION));

        Level world = entity.getLevel();
        int seed = (int) entity.getBlockPos().asLong();
        itemRenderer.renderStatic(output, ItemDisplayContext.FIXED,
                light, overlay, matrices, vertexConsumers, world, seed);
    }
}