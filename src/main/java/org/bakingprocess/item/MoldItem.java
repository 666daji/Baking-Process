package org.bakingprocess.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.bakingprocess.client.render.item.renderer.MoldItemRenderer;
import org.bakingprocess.content.ShapedDoughContent;
import org.twcore.api.content.ContainerUtil;
import org.twcore.content.Content;

import java.util.function.Consumer;

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

    // =================== Render ===================

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        MoldItemRenderer itemRenderer = new MoldItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

        consumer.accept(new IClientItemExtensions() {

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return itemRenderer;
            }
        });
    }
}
