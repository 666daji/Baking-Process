package org.bakingprocess;

import org.bakingprocess.block.process.playeraction.impl.AddItemPlayerAction;
import org.bakingprocess.registry.ModItems;
import org.twcore.api.TwCoreRegistrar;
import org.twcore.api.TwModManager;
import org.twcore.container.AbstractMappedContainer;
import org.twcore.registry.ContainerTypes;
import org.twcore.registry.Contents;

public class RegistryCore implements TwCoreRegistrar {

    @Override
    public void register() {
        TwModManager.IMPL.register(BakingProcess.MOD_ID, 1);

        // 注册映射
        AddItemPlayerAction.REMAPPING.put(ModItems.SALMON_CUBES, "msa");
        ((AbstractMappedContainer) ContainerTypes.POTION).registerContentMapping(Contents.MILK, ModItems.MILK_POTION);
    }
}
