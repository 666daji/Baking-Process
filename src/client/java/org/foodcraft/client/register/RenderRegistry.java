package org.foodcraft.client.register;

import org.foodcraft.client.render.block.ModBlockColors;
import org.foodcraft.client.render.block.blockentity.UpPlaceStackRenderers;
import org.foodcraft.client.render.item.renderer.ItemRenderers;
import org.foodcraft.client.render.item.replacer.ItemModelReplacers;
import org.foodcraft.client.render.model.ModModelLayers;
import org.foodcraft.client.render.model.ModRenderLayers;

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
