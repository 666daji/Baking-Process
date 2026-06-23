package org.bakingprocess.client.render.block.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.bakingprocess.block.entity.HeatResistantSlateBlockPileEntity;
import org.twcore.api.blockpile.CubeBlockPileReference;
import org.twcore.client.api.render.CubeBlockPileDebugRenderer;
import org.twcore.client.api.render.UpPlaceBlockEntityRenderer;

public class HeatResistantSlateBlockPileEntityRenderer extends UpPlaceBlockEntityRenderer<HeatResistantSlateBlockPileEntity> implements CubeBlockPileDebugRenderer<HeatResistantSlateBlockPileEntity> {

    public HeatResistantSlateBlockPileEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(HeatResistantSlateBlockPileEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        if (entity.isEmpty()) {
            return;
        }

        matrices.pushPose();

        // 调整渲染位置
        matrices.translate(0.0, 0.125, 0.0);
        fromStackRender(entity.getItem(0), entity, tickDelta, matrices, vertexConsumers, light, overlay);

        matrices.popPose();
    }

    @Override
    public Font getTextRenderer() {
        return context.getFont();
    }

    @Override
    public CubeBlockPileReference getReference(HeatResistantSlateBlockPileEntity entity) {
        return entity.getCubeBlockPileReference();
    }

    @Override
    public void otherDebugRender(HeatResistantSlateBlockPileEntity entity, CubeBlockPileReference reference, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        // 如果炉子结构有效，显示额外信息
        if (entity.isStoveValid()) {
            String stoveType = "Stove: " + entity.getCurrentStoveStructureType();
            int stoveWidth = getTextRenderer().width(stoveType);
            getTextRenderer().drawInBatch(
                    stoveType,
                    -stoveWidth / 2f, 10,
                    0xFF00FF00,
                    false,
                    matrices.last().pose(),
                    vertexConsumers,
                    Font.DisplayMode.POLYGON_OFFSET,
                    0,
                    light
            );
        }
    }
}