package org.bakingprocess.registry;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.fluidsystem.content.AbstractContent;

public class ModRegistries {
    public static Registry<AbstractContent> CONTENT = of("content_type");

    public static <T> Registry<T> of(String id) {
        RegistryKey<Registry<T>> key = RegistryKey.ofRegistry(new Identifier(BakingProcess.MOD_ID, id));

        return FabricRegistryBuilder.createSimple(key)
                .attribute(RegistryAttribute.SYNCED)
                .buildAndRegister();
    }
}
