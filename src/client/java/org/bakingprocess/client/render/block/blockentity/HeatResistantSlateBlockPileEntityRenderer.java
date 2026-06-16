package org.bakingprocess.client.render.block.blockentity;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import org.bakingprocess.block.entity.HeatResistantSlateBlockPileEntity;
import org.twcore.api.blockpile.CubeBlockPileReference;
import org.twcore.client.api.render.CubeBlockPileDebugRenderer;

public class HeatResistantSlateBlockPileEntityRenderer extends UpPlaceBlockEntityRenderer<HeatResistantSlateBlockPileEntity> implements CubeBlockPileDebugRenderer<HeatResistantSlateBlockPileEntity> {

    public HeatResistantSlateBlockPileEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(HeatResistantSlateBlockPileEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.isEmpty()) {
            return;
        }

        matrices.push();

        // 调整渲染位置
        matrices.translate(0.0, 0.125, 0.0);
        fromStackRender(entity.getStack(0), entity, tickDelta, matrices, vertexConsumers, light, overlay);

        matrices.pop();
    }

    @Override
    public TextRenderer getTextRenderer() {
        return context.getTextRenderer();
    }

    @Override
    public CubeBlockPileReference getReference(HeatResistantSlateBlockPileEntity entity) {
        return entity.getCubeBlockPileReference();
    }

    @Override
    public void otherDebugRender(HeatResistantSlateBlockPileEntity entity, CubeBlockPileReference reference, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        // 如果炉子结构有效，显示额外信息
        if (entity.isStoveValid()) {
            String stoveType = "Stove: " + entity.getCurrentStoveStructureType();
            int stoveWidth = getTextRenderer().getWidth(stoveType);
            getTextRenderer().draw(
                    stoveType,
                    -stoveWidth / 2f, 10,
                    0xFF00FF00,
                    false,
                    matrices.peek().getPositionMatrix(),
                    vertexConsumers,
                    TextRenderer.TextLayerType.POLYGON_OFFSET,
                    0,
                    light
            );
        }
    }
}