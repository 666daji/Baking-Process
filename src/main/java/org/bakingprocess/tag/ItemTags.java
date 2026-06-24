package org.bakingprocess.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.bakingprocess.BakingProcess;
import org.twcore.TWCore;

public class ItemTags {
    public static final TagKey<Item> FISH = of("fish");
    public static final TagKey<Item> MEAT = of("meat");
    public static final TagKey<Item> VEGETABLES = of("vegetables");
    public static final TagKey<Item> FRUIT = of("fruit");

    private static TagKey<Item> of(String id) {
        return TagKey.create(Registries.ITEM, TWCore.createResourceLocation(BakingProcess.MOD_ID, id));
    }
}
