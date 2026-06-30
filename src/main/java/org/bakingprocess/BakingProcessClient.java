package org.bakingprocess;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import org.bakingprocess.client.register.RenderRegistry;
import org.bakingprocess.client.render.block.ModBlockColors;
import org.bakingprocess.client.render.block.blockentity.*;
import org.bakingprocess.client.render.gui.tooltip.FlourSackTooltipComponent;
import org.bakingprocess.client.render.model.ModModelLayers;
import org.bakingprocess.client.render.model.ModModelLoader;
import org.bakingprocess.item.FlourSackItem;
import org.bakingprocess.block.entity.GrindingStoneBlockEntity;
import org.bakingprocess.client.render.item.renderer.ItemRenderers;
import org.bakingprocess.client.render.item.renderer.MoldItemRenderer;
import org.bakingprocess.registry.ModBlocks;
import org.bakingprocess.registry.ModItems;
import org.bakingprocess.registry.ModBlockEntityTypes;

@Mod(value = BakingProcess.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = BakingProcess.MOD_ID, value = Dist.CLIENT)
public class BakingProcessClient {

    @SubscribeEvent
    public static void registerModelLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ModModelLayers.registryAll(event);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RenderRegistry.registryRender();
    }

    @SubscribeEvent
    public static void customModelLoading(ModelEvent.RegisterAdditional event) {
        ModModelLoader.initModels(event);
    }

    @SubscribeEvent
    public static void onBlockColorRegister(RegisterColorHandlersEvent.Block event) {
        ModBlockColors.registryColors(event);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntityTypes.GRINDING_STONE.get(), GrindingStoneBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.GARNISH_DISHES.get(), GarnishDishesBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.HEAT_RESISTANT_SLATE.get(), HeatResistantSlateBlockPileEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.MOLD.get(), MoldBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.CUTTING_BOARD.get(), CuttingBoardBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.POTS.get(), PotsBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.PLATE.get(), PlateBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registryToolTip(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(FlourSackItem.FlourSackTooltipData.class, FlourSackTooltipComponent::new);
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer itemRenderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (itemRenderer == null) {
                    itemRenderer = ItemRenderers.createSimpleBlockEntityRenderer(
                            ModBlocks.GRINDING_STONE.get(), GrindingStoneBlockEntity::new);
                }
                return itemRenderer;
            }
        }, ModItems.GRINDING_STONE.get());

        MoldItemRenderer moldRenderer = new MoldItemRenderer(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
        IClientItemExtensions moldExtensions = new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return moldRenderer;
            }
        };
        event.registerItem(moldExtensions,
                ModItems.CAKE_EMBRYO_MOLD.get(),
                ModItems.TOAST_EMBRYO_MOLD.get());
    }
}
