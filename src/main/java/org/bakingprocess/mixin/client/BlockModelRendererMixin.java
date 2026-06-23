package org.bakingprocess.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.bakingprocess.block.entity.HeatResistantSlateBlockPileEntity;
import org.bakingprocess.client.render.model.ModModelLoader;
import org.dfood.block.FoodBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ModelBlockRenderer.class)
public class BlockModelRendererMixin {

    @ModifyVariable(
            method = "tesselateBlock(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JI)V",
            at = @At("HEAD"),
            argsOnly = true)
    public BakedModel renderCookingModel(BakedModel model, BlockAndTintGetter world, BakedModel bakedModel, BlockState state, BlockPos pos, PoseStack matrices) {
        if (state.getBlock() instanceof FoodBlock foodBlock &&
                world.getBlockEntity(pos) instanceof HeatResistantSlateBlockPileEntity) {
            int foodValue = state.getValue(foodBlock.NUMBER_OF_FOOD);

            if (foodValue > 1) {
                ModelManager manager = Minecraft.getInstance().getModelManager();
                ResourceLocation renderModelId = ModModelLoader.createCookingModel(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath(), foodValue);
                BakedModel model1 = manager.getModel(renderModelId);

                // 只在成功获取到有效模型时才执行旋转并返回新模型
                if (model1 != manager.getMissingModel()) {
                    // 手动旋转模型
                    matrices.translate(0.5, 0.5, 0.5);
                    float facing = state.getValue(FoodBlock.FACING).get2DDataValue();
                    matrices.mulPose(Axis.YP.rotationDegrees(facing));
                    matrices.translate(-0.5, -0.5, -0.5);

                    return model1;
                }
            }
        }

        return model;
    }
}