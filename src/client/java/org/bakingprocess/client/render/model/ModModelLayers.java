package org.bakingprocess.client.render.model;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.client.render.block.blockentity.GrindingStoneBlockEntityRenderer;

public class ModModelLayers {
    public static final EntityModelLayer GRINDING_STONE = registerMain("grinding_stone", GrindingStoneBlockEntityRenderer::getTexturedModelData);

    private static EntityModelLayer registerMain(String id, EntityModelLayerRegistry.TexturedModelDataProvider provider) {
        return register(id, "main", provider);
    }

    private static EntityModelLayer register(String id, String layer, EntityModelLayerRegistry.TexturedModelDataProvider provider) {
        EntityModelLayer entityModelLayer = create(id, layer);
        EntityModelLayerRegistry.registerModelLayer(entityModelLayer, provider);
        return entityModelLayer;
    }

    private static EntityModelLayer create(String id, String layer) {
        return new EntityModelLayer(new Identifier(BakingProcess.MOD_ID, id), layer);
    }

    public static void register() {
    }
}
