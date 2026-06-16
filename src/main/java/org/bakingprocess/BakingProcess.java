package org.bakingprocess;

import net.fabricmc.api.ModInitializer;
import org.bakingprocess.registry.*;
import org.bakingprocess.integration.dfood.DFoodInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twcore.api.TwModManager;
import org.twcore.api.event.TwCoreRegisterEvent;
import org.twcore.container.AbstractMappedContainer;
import org.twcore.process.playeraction.impl.AddItemPlayerAction;
import org.twcore.registry.ContainerTypes;
import org.twcore.registry.Contents;

public class BakingProcess implements ModInitializer {
    public static final String MOD_ID = "baking_process";
    public static final Logger LOGGER = LoggerFactory.getLogger("TW`s Baking Process");

    @Override
    public void onInitialize() {
        DFoodInit.init();
        RegistryInit.init();
        TwCoreRegisterEvent.TW_CORE_REGISTRAR.register(BakingProcess::register);

        LOGGER.info("TW`s Baking Process is initializing!");
    }

    public static void register() {
        TwModManager.IMPL.register(BakingProcess.MOD_ID, 1);

        // 注册映射
        AddItemPlayerAction.REMAPPING.put(ModItems.SALMON_CUBES, "msa");
        ((AbstractMappedContainer) ContainerTypes.POTION).registerContentMapping(Contents.MILK, ModItems.MILK_POTION);
    }
}
