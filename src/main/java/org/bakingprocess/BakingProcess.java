package org.bakingprocess;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.bakingprocess.client.render.block.blockentity.*;
import org.bakingprocess.integration.dfood.DFoodInit;
import org.bakingprocess.integration.dfood.FoodBlocksModifier;
import org.bakingprocess.registry.ModItems;
import org.bakingprocess.registry.RegistryInit;
import org.bakingprocess.util.BakingProcessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twcore.api.TwModManager;
import org.twcore.api.event.TwCoreRegisterEvent;
import org.twcore.api.sound.Item2BlockSounds;
import org.twcore.container.AbstractMappedContainer;
import org.twcore.process.playeraction.impl.AddItemPlayerAction;
import org.twcore.registry.ContainerTypes;
import org.twcore.registry.Contents;

@Mod(BakingProcess.MOD_ID)
public class BakingProcess {
    public static final String MOD_ID = "baking_process";
    public static final Logger LOGGER = LoggerFactory.getLogger("TW's Baking Process");

    public BakingProcess(IEventBus modEventBus, ModContainer modContainer) {
        DFoodInit.init(modEventBus);
        RegistryInit.init(modEventBus);
        modEventBus.addListener(BakingProcess::register);

        LOGGER.info("TW`s Baking Process is initializing!");
    }

    public static void register(TwCoreRegisterEvent event) {
        TwModManager.IMPL.register(BakingProcess.MOD_ID, 1);

        AddItemPlayerAction.REMAPPING.put(ModItems.SALMON_CUBES.get(), "msa");
        ((AbstractMappedContainer) ContainerTypes.POTION.get()).registerContentMapping(Contents.MILK.get(), ModItems.MILK_POTION.get());
        Item2BlockSounds.registerParser(BakingProcessUtils::getSoundGroupFromItem);
        FoodBlocksModifier.FoodBlockAdd();
    }
}
