package org.bakingprocess.tag;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.bakingprocess.BakingProcess;

public class ItemTags {
    public static final TagKey<Item> FISH = of("fish");
    public static final TagKey<Item> MEAT = of("meat");
    public static final TagKey<Item> VEGETABLES = of("vegetables");
    public static final TagKey<Item> FRUIT = of("fruit");

    private static TagKey<Item> of(String id) {
        return TagKey.of(RegistryKeys.ITEM, new Identifier(BakingProcess.MOD_ID, id));
    }
}
