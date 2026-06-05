package org.bakingprocess.client.register;

import org.bakingprocess.client.render.block.ModBlockColors;
import org.bakingprocess.client.render.block.blockentity.UpPlaceStackRenderers;
import org.bakingprocess.client.render.item.renderer.ItemRenderers;
import org.bakingprocess.client.render.item.replacer.ItemModelReplacers;
import org.bakingprocess.client.render.model.ModModelLayers;
import org.bakingprocess.client.render.model.ModRenderLayers;

public class RenderRegistry {
    public static void registryRender() {
        ModModelLayers.register();
        ItemModelReplacers.registry();
        ModBlockColors.registryColors();
        ModRenderLayers.registryRenderLayer();
        ItemRenderers.registry();
        UpPlaceStackRenderers.registerAll();
    }
}
