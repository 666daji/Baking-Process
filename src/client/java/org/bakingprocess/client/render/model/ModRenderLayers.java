package org.bakingprocess.client.render.model;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import org.bakingprocess.integration.dfood.AssistedBlocks;
import org.bakingprocess.registry.ModBlocks;

public class ModRenderLayers {
    private static final BlockRenderLayerMap instance = BlockRenderLayerMap.INSTANCE;

    public static void registryRenderLayer() {
        instance.putBlock(AssistedBlocks.CRIPPLED_SUSPICIOUS_STEW, RenderLayer.getCutout());
        instance.putBlock(ModBlocks.COMBUSTION_FIREWOOD, RenderLayer.getCutout());
        instance.putBlock(ModBlocks.MILK_POTION, RenderLayer.getCutout());
    }
}
