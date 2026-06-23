package org.bakingprocess.client.render.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.bakingprocess.client.render.model.ModModelLoader;
import org.bakingprocess.content.ShapedDoughContent;
import org.twcore.api.content.ContainerUtil;
import org.twcore.content.Content;

public class MoldItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final Minecraft CLIENT = Minecraft.getInstance();

    public MoldItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    public static void renderMold(ItemStack stack, ItemDisplayContext mode, PoseStack matrices,
                                  MultiBufferSource vertexConsumers, int light, int overlay) {

        if (!(stack.getItem() instanceof BlockItem blockItem)) return;

        BlockRenderDispatcher blockRenderer = CLIENT.getBlockRenderer();
        ModelManager manager = CLIENT.getModelManager();

        BlockState state = blockItem.getBlock().defaultBlockState();
        Content content = ContainerUtil.extractContent(stack);

        // 渲染方块本身
        blockRenderer.renderSingleBlock(state, matrices, vertexConsumers, light, overlay);

        // 如果有定型面团内容，渲染它
        if (content instanceof ShapedDoughContent shapedDough) {
            BakedModel model = manager.getModel(ModModelLoader.createShapedDoughModel(shapedDough));
            blockRenderer.getModelRenderer().renderModel(matrices.last(),
                    vertexConsumers.getBuffer(ItemBlockRenderTypes.getChunkRenderType(state)), state, model, 1, 1, 1, light, overlay);
        }
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        renderMold(stack, mode, matrices, vertexConsumers, light, overlay);
    }
}