package org.bakingprocess.client;

import net.fabricmc.api.ClientModInitializer;
import org.bakingprocess.client.register.*;

public class BakingProcessClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        RenderRegistry.registryRender();
        ModFabricEvent.registerFabricEvents();
    }
}
