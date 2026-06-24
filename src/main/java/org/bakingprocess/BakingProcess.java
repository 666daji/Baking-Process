package org.bakingprocess;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.bakingprocess.client.register.RenderRegistry;
import org.bakingprocess.client.render.block.ModBlockColors;
import org.bakingprocess.client.render.block.blockentity.*;
import org.bakingprocess.client.render.gui.tooltip.FlourSackTooltipComponent;
import org.bakingprocess.client.render.model.ModModelLayers;
import org.bakingprocess.client.render.model.ModModelLoader;
import org.bakingprocess.integration.dfood.DFoodInit;
import org.bakingprocess.item.FlourSackItem;
import org.bakingprocess.registry.ModBlockEntityTypes;
import org.bakingprocess.registry.ModItems;
import org.bakingprocess.registry.RegistryInit;
import org.bakingprocess.util.BakingProcessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twcore.api.TwModManager;
import org.twcore.api.event.TwCoreRegisterEvent;
import org.twcore.api.sound.Item2BlockSounds;
import org.twcore.container.AbstractMappedContainer;
import org.twcore.process.playeraction.impl.AddItemPlayerAction;
import org.twcore.registry.ContainerTypes;
import org.twcore.registry.Contents;

@Mod(BakingProcess.MOD_ID)
public class BakingProcess {
    public static final String MOD_ID = "baking_process";
    public static final Logger LOGGER = LoggerFactory.getLogger("TW's Baking Process");

    public BakingProcess() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        DFoodInit.init(modEventBus);
        RegistryInit.init(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("TW`s Baking Process is initializing!");
    }

    @SubscribeEvent
    public static void register(TwCoreRegisterEvent event) {
        TwModManager.IMPL.register(BakingProcess.MOD_ID, 1);

        AddItemPlayerAction.REMAPPING.put(ModItems.SALMON_CUBES.get(), "msa");
        ((AbstractMappedContainer) ContainerTypes.POTION.get()).registerContentMapping(Contents.MILK.get(), ModItems.MILK_POTION.get());
        Item2BlockSounds.registerParser(BakingProcessUtils::getSoundGroupFromItem);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

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
    }
}
