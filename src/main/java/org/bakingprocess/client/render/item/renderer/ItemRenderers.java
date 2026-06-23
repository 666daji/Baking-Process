package org.bakingprocess.client.render.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ItemRenderers {
    private static BlockEntityRenderDispatcher blockEntityRenderer;

    /**
     * 创建一个简单地复用方块实体渲染器的物品渲染器。
     * @return 创建的物品渲染器
     */
    public static BlockEntityWithoutLevelRenderer createSimpleBlockEntityRenderer(Block block, BlockEntityType.BlockEntitySupplier<BlockEntity> factory) {
        BlockState state = block.defaultBlockState();
        BlockEntity entity = factory.create(BlockPos.ZERO, state);

        return new BlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()) {
            @Override
            public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
                if (blockEntityRenderer == null) {
                    blockEntityRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher();
                }

                blockEntityRenderer.renderItem(entity, matrices, vertexConsumers, light, overlay);
            }
        };
    }
}
