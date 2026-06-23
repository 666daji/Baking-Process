package org.bakingprocess.content;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twcore.content.Content;

public class ShapedDoughContent extends Content {
    private static final Table<ResourceLocation, ResourceLocation, ShapedDoughContent> CACHES = HashBasedTable.create();

    protected final ResourceLocation originalDough;
    protected final ResourceLocation baseMold;

    public ShapedDoughContent(@NotNull String category, ResourceLocation originalDough, ResourceLocation baseMold) {
        super(category);
        this.originalDough = originalDough;
        this.baseMold = baseMold;

        CACHES.put(originalDough, baseMold, this);
    }

    public Block getBaseMold() {
        return BuiltInRegistries.BLOCK.get(baseMold);
    }

    public Item getOriginalDough() {
        return BuiltInRegistries.ITEM.get(originalDough);
    }

    @Nullable
    public static ShapedDoughContent fromBaseGet(ItemStack originalDough, BlockState baseMold) {
        ResourceLocation originalDoughId = BuiltInRegistries.ITEM.getKey(originalDough.getItem());
        ResourceLocation baseMoldId = BuiltInRegistries.BLOCK.getKey(baseMold.getBlock());

        return CACHES.get(originalDoughId, baseMoldId);
    }
}
