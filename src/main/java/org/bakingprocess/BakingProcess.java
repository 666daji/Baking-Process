package org.bakingprocess;

import net.fabricmc.api.ModInitializer;
import org.bakingprocess.registry.*;
import org.bakingprocess.integration.dfood.DFoodInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BakingProcess implements ModInitializer {
    public static final String MOD_ID = "baking_process";
    public static final Logger LOGGER = LoggerFactory.getLogger("TW`s Baking Process");

    @Override
    public void onInitialize() {
        DFoodInit.init();
        RegistryInit.init();
        LOGGER.info("TW`s Baking Process is initializing!");
    }
}
