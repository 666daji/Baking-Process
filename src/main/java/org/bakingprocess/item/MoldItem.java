package org.bakingprocess.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.bakingprocess.content.ShapedDoughContent;
import org.twcore.api.content.ContainerUtil;
import org.twcore.content.Content;

public class MoldItem extends BlockItem {

    public MoldItem(Block block, Properties settings) {
        super(block, settings);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        if (ContainerUtil.extractContent(stack) != null) {
            return super.getDescriptionId() + ".dough";
        }

        return super.getDescriptionId(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        Content content = ContainerUtil.extractContent(stack);

        if (content instanceof ShapedDoughContent shapedDough) {
            Component doughName = shapedDough.getDisplayName();
            return Component.translatable(this.getDescriptionId(stack), doughName);
        }

        return super.getName(stack);
    }
}
