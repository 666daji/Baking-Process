package org.bakingprocess.item;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.bakingprocess.block.entity.GrindingStoneBlockEntity;
import org.bakingprocess.client.render.item.renderer.ItemRenderers;

import java.util.function.Consumer;

public class GrindingStoneItem extends BlockItem {
    public GrindingStoneItem(Block block, Properties settings) {
        super(block, settings);
    }

    // =================== Render ===================

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            BlockEntityWithoutLevelRenderer itemRenderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (itemRenderer == null) {
                    itemRenderer = ItemRenderers.createSimpleBlockEntityRenderer(GrindingStoneItem.this.getBlock(), GrindingStoneBlockEntity::new);
                }

                return itemRenderer;
            }
        });
    }
}
