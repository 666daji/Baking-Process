package org.bakingprocess.client.register;

import org.bakingprocess.client.render.block.blockentity.UpPlaceStackRenderers;
import org.bakingprocess.client.render.item.replacer.ItemModelReplacers;

public class RenderRegistry {
    public static void registryRender() {
        ItemModelReplacers.registry();
        UpPlaceStackRenderers.registerAll();
    }
}
