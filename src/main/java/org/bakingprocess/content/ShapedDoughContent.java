package org.bakingprocess.content;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twcore.content.Content;

public class ShapedDoughContent extends Content {
    private static final Table<Identifier, Identifier, ShapedDoughContent> CACHES = HashBasedTable.create();

    protected final Identifier originalDough;
    protected final Identifier baseMold;

    public ShapedDoughContent(@NotNull String category, Identifier originalDough, Identifier baseMold) {
        super(category);
        this.originalDough = originalDough;
        this.baseMold = baseMold;

        CACHES.put(originalDough, baseMold, this);
    }

    public Block getBaseMold() {
        return Registries.BLOCK.get(baseMold);
    }

    public Item getOriginalDough() {
        return Registries.ITEM.get(originalDough);
    }

    @Nullable
    public static ShapedDoughContent fromBaseGet(ItemStack originalDough, BlockState baseMold) {
        Identifier originalDoughId = Registries.ITEM.getId(originalDough.getItem());
        Identifier baseMoldId = Registries.BLOCK.getId(baseMold.getBlock());

        return CACHES.get(originalDoughId, baseMoldId);
    }
}
