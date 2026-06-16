package org.bakingprocess;

import org.twcore.api.TwCoreRegistrar;
import org.twcore.api.TwModManager;

public class RegistryCore implements TwCoreRegistrar {

    @Override
    public void register() {
        TwModManager.IMPL.register(BakingProcess.MOD_ID, 1);
    }
}
