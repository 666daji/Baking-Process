package org.bakingprocess.client.render.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.client.render.block.blockentity.GrindingStoneBlockEntityRenderer;

public class ModModelLayers {
    public static final ModelLayerLocation GRINDING_STONE = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(BakingProcess.MOD_ID, "grinding_stone"), "main");

    public static void registryAll(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GRINDING_STONE, GrindingStoneBlockEntityRenderer::getTexturedModelData);
    }
}
