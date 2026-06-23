package org.bakingprocess.client.render.model;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.ModelEvent;
import org.dfood.block.FoodBlocks;
import org.bakingprocess.BakingProcess;
import org.bakingprocess.content.DishesContent;
import org.bakingprocess.content.ShapedDoughContent;
import org.bakingprocess.registry.ModContents;
import org.bakingprocess.item.FlourItem;
import org.bakingprocess.registry.ModItems;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.twcore.TWCore;
import org.twcore.api.process.PlayerAction;
import org.twcore.content.Content;
import org.twcore.process.playeraction.impl.AddContentPlayerAction;
import org.twcore.process.playeraction.impl.AddItemPlayerAction;
import org.twcore.registry.Contents;
import org.twcore.registry.TWRegistries;

import java.util.*;

public class ModModelLoader {
    private static final Logger LOGGER = BakingProcess.LOGGER;

    /**
     * 存储所有需要加载的模型标识符。
     */
    private static final List<ResourceLocation> MODELS_TO_LOAD = new ArrayList<>();

    /** 菜刀插在案板上的效果 。*/
    public static final ResourceLocation BOARD_KITCHEN_KNIFE = TWCore.createResourceLocation(BakingProcess.MOD_ID, "other/on_board_kitchen_knife");

    public static void initModels(ModelEvent.RegisterAdditional event) {
        MODELS_TO_LOAD.clear();

        // 注册所有模型
        registerAllFlourSackModels();
        registerAllCookingModels();
        registerDoughKneadingModel();
        registerCuttingModels();
        registryDishesModels();
        registerPlatingProcessModels();
        registryEatModels();
        registerShapedDoughAll();
        MODELS_TO_LOAD.add(BOARD_KITCHEN_KNIFE);

        // 将所有模型添加到加载上下文
        for (ResourceLocation modelId : MODELS_TO_LOAD) {
            event.register(modelId);
        }
    }

    // =========== 食物烘烤模型 ===========

    /**
     * 注册所有需要加载烘烤模型的食物方块。
     */
    private static void registerAllCookingModels() {
        registerCookingModelsForBlock(FoodBlocks.POTATO, 4);
        registerCookingModelsForBlock(FoodBlocks.BAKED_POTATO, 4);
        registerCookingModelsForBlock(FoodBlocks.BEEF, 2);
        registerCookingModelsForBlock(FoodBlocks.COOKED_BEEF, 2);
        registerCookingModelsForBlock(FoodBlocks.MUTTON, 2);
        registerCookingModelsForBlock(FoodBlocks.COOKED_MUTTON, 2);
        registerCookingModelsForBlock(FoodBlocks.PORKCHOP, 2);
        registerCookingModelsForBlock(FoodBlocks.COOKED_PORKCHOP, 2);
    }

    /**
     * 为指定的FoodBlock注册所有烘烤模型。
     * @param block FoodBlock实例
     * @param maxFood 最大食物数量
     */
    public static void registerCookingModelsForBlock(Block block, int maxFood) {
        if (maxFood < 2) {
            LOGGER.warn("Max food value {} is less than 2 for block {}, skipping cooking models",
                    maxFood, BuiltInRegistries.BLOCK.getKey(block));
            return;
        }

        String blockPath = BuiltInRegistries.BLOCK.getKey(block).getPath();

        for (int foodValue = 2; foodValue <= maxFood; foodValue++) {
            ResourceLocation modelId = createCookingModel(blockPath, foodValue);
            MODELS_TO_LOAD.add(modelId);
            LOGGER.debug("Registered cooking model: {} for food value {}",
                    modelId, foodValue);
        }
    }

    /**
     * 创建烘烤食物模型的标识符。
     */
    public static ResourceLocation createCookingModel(String blockPath, int foodValue) {
        String modelPath = blockPath + "_cooking_" + foodValue;
        return TWCore.createResourceLocation(BakingProcess.MOD_ID, "other/" + modelPath);
    }

    // =========== 粉尘袋模型 ===========

    /**
     * 注册所有粉尘袋模型。
     */
    private static void registerAllFlourSackModels() {
        for (FlourItem flourItem : FlourItem.FLOURS) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(flourItem);

            String flourSackModelName = itemId.getPath() + "_sack";
            ModelResourceLocation modelId = createItemModel(flourSackModelName);
            MODELS_TO_LOAD.add(modelId);
            LOGGER.debug("Dynamically registered flour sack model: {}", modelId);
        }
    }

    // =========== 揉面流程 ===========

    public static void registerDoughKneadingModel() {
        MODELS_TO_LOAD.add(createProcessModel("knead_add_flour_1"));
        MODELS_TO_LOAD.add(createProcessModel("knead_add_flour_2"));
        MODELS_TO_LOAD.add(createProcessModel("knead_add_flour_3"));

        MODELS_TO_LOAD.add(createProcessModel("knead_add_liquid_1"));
        MODELS_TO_LOAD.add(createProcessModel("knead_add_liquid_2"));
        MODELS_TO_LOAD.add(createProcessModel("knead_add_liquid_3"));

        MODELS_TO_LOAD.add(createProcessModel("knead_knead_1"));
        MODELS_TO_LOAD.add(createProcessModel("knead_knead_2"));
    }

    // =========== 切割流程 ===========

    /**
     * 注册所有切割模型。
     */
    private static void registerCuttingModels() {
        registerCuttingModelsForItem(new ResourceLocation("carrot"), 12);
        registerCuttingModelsForItem(new ResourceLocation("apple"), 6);
        registerCuttingModelsForItem(new ResourceLocation("cod"), 9);
        registerCuttingModelsForItem(new ResourceLocation("cooked_cod"), 9);
        registerCuttingModelsForItem(new ResourceLocation("salmon"), 7);
        registerCuttingModelsForItem(new ResourceLocation("cooked_salmon"), 7);
        registerCuttingModelsForItem(new ResourceLocation("potato"), 1);
        registerCuttingModelsForItem(new ResourceLocation("baked_potato"), 1);
        registerCuttingModelsForItem(TWCore.createResourceLocation(BakingProcess.MOD_ID, "hard_bread"), 1);
    }

    /**
     * 为指定物品注册所有切割模型。
     */
    public static void registerCuttingModelsForItem(ResourceLocation itemId, int maxCuts) {
        if (maxCuts < 1) {
            LOGGER.warn("Max cuts {} is less than 1 for item {}, skipping cutting models",
                    maxCuts, itemId);
            return;
        }

        // 为每个切割次数注册模型
        for (int cutCount = 1; cutCount <= maxCuts; cutCount++) {
            ResourceLocation modelId = createCuttingModel(itemId, cutCount);
            MODELS_TO_LOAD.add(modelId);
            LOGGER.debug("Registered cutting model: {} for item {} at cut {}",
                    modelId, itemId, cutCount);
        }
    }

    /**
     * 创建切割模型标识符。
     * <p>格式：baking_process:process/cut_{namespace}_{itemPath}_{cutCount}</p>
     */
    public static ResourceLocation createCuttingModel(ResourceLocation itemId, int cutCount) {
        String modelPath = String.format("cut_%s_%s_%d",
                itemId.getNamespace(), itemId.getPath(), cutCount);
        return TWCore.createResourceLocation(BakingProcess.MOD_ID, "process/" + modelPath);
    }

    // =========== 摆盘菜肴 ===========

    /**
     * 注册所有菜肴的放置模型。
     */
    private static void registryDishesModels() {
        MODELS_TO_LOAD.add(createDishesModel(ModItems.IRON_PLATE.get(), ModContents.BEEF_BERRIES.get()));
        MODELS_TO_LOAD.add(createDishesModel(ModItems.IRON_PLATE.get(), ModContents.COOKED_BEEF_BERRIES.get()));
        MODELS_TO_LOAD.add(createDishesModel(ModItems.IRON_PLATE.get(), ModContents.ROASTED_MUSHROOMS.get()));
        MODELS_TO_LOAD.add(createDishesModel(ModItems.IRON_PLATE.get(), ModContents.COOKED_ROASTED_MUSHROOMS.get()));
        MODELS_TO_LOAD.add(createDishesModel(ModItems.IRON_PLATE.get(), ModContents.HONEY_ROASTED_BEEF.get()));
        MODELS_TO_LOAD.add(createDishesModel(ModItems.IRON_PLATE.get(), ModContents.COOKED_HONEY_ROASTED_BEEF.get()));
        MODELS_TO_LOAD.add(createDishesModel(ModItems.IRON_PLATE.get(), ModContents.FRY_SALMON_CUBES.get()));
        MODELS_TO_LOAD.add(createDishesModel(ModItems.IRON_PLATE.get(), ModContents.COOKED_FRY_SALMON_CUBES.get()));
        MODELS_TO_LOAD.add(createDishesModel(ModItems.IRON_PLATE.get(), ModContents.GRILLED_FISH_POTATOES.get()));
        MODELS_TO_LOAD.add(createDishesModel(ModItems.IRON_PLATE.get(), ModContents.COOKED_GRILLED_FISH_POTATOES.get()));
        MODELS_TO_LOAD.add(createDishesModel(ModItems.IRON_PLATE.get(), ModContents.DELUXE_ROASTED_RABBIT.get()));
        MODELS_TO_LOAD.add(createDishesModel(ModItems.IRON_PLATE.get(), ModContents.COOKED_DELUXE_ROASTED_RABBIT.get()));
        MODELS_TO_LOAD.add(createDishesModel(ModItems.IRON_PLATE.get(), ModContents.HONEY_ROASTED_MUTTON.get()));
        MODELS_TO_LOAD.add(createDishesModel(ModItems.IRON_PLATE.get(), ModContents.COOKED_HONEY_ROASTED_MUTTON.get()));
        MODELS_TO_LOAD.add(createDishesModel(ModItems.IRON_PLATE.get(), ModContents.DELUXE_ROAST_CHICKEN.get()));
        MODELS_TO_LOAD.add(createDishesModel(ModItems.IRON_PLATE.get(), ModContents.COOKED_DELUXE_ROAST_CHICKEN.get()));
    }

    /**
     * 注册所有食用过程模型。
     */
    private static void registryEatModels() {
        registerEatStageModels(ModItems.IRON_PLATE.get(), ModContents.COOKED_BEEF_BERRIES.get());
        registerEatStageModels(ModItems.IRON_PLATE.get(), ModContents.COOKED_ROASTED_MUSHROOMS.get());
        registerEatStageModels(ModItems.IRON_PLATE.get(), ModContents.COOKED_HONEY_ROASTED_BEEF.get());
        registerEatStageModels(ModItems.IRON_PLATE.get(), ModContents.COOKED_FRY_SALMON_CUBES.get());
        registerEatStageModels(ModItems.IRON_PLATE.get(), ModContents.COOKED_GRILLED_FISH_POTATOES.get());
        registerEatStageModels(ModItems.IRON_PLATE.get(), ModContents.COOKED_DELUXE_ROASTED_RABBIT.get());
        registerEatStageModels(ModItems.IRON_PLATE.get(), ModContents.COOKED_HONEY_ROASTED_MUTTON.get());
        registerEatStageModels(ModItems.IRON_PLATE.get(), ModContents.COOKED_DELUXE_ROAST_CHICKEN.get());
    }

    /**
     * 注册摆盘流程模型
     */
    private static void registerPlatingProcessModels() {
        registerPlatingSequenceModels(
                ModItems.IRON_PLATE.get(),
                Arrays.asList(
                        new AddItemPlayerAction(Items.BEEF),
                        new AddItemPlayerAction(Items.SWEET_BERRIES)
                ),
                ModContents.BEEF_BERRIES.get());

        registerPlatingSequenceModels(
                ModItems.IRON_PLATE.get(),
                Arrays.asList(
                        new AddItemPlayerAction(Items.RED_MUSHROOM),
                        new AddItemPlayerAction(Items.BROWN_MUSHROOM),
                        new AddItemPlayerAction(Items.BROWN_MUSHROOM),
                        new AddItemPlayerAction(Items.BROWN_MUSHROOM),
                        new AddItemPlayerAction(ModItems.SALT_FLOUR.get())
                ),
                ModContents.ROASTED_MUSHROOMS.get());

        registerPlatingSequenceModels(
                ModItems.IRON_PLATE.get(),
                Arrays.asList(
                        new AddItemPlayerAction(Items.BEEF),
                        new AddItemPlayerAction(ModItems.SALT_FLOUR.get()),
                        new AddContentPlayerAction(Contents.HONEY.get()),
                        new AddItemPlayerAction(ModItems.CARROT_SLICES.get()),
                        new AddItemPlayerAction(ModItems.CARROT_SLICES.get())
                ),
                ModContents.HONEY_ROASTED_BEEF.get());

        registerPlatingSequenceModels(
                ModItems.IRON_PLATE.get(),
                Arrays.asList(
                        new AddItemPlayerAction(ModItems.SALMON_CUBES.get()),
                        new AddItemPlayerAction(ModItems.SALMON_CUBES.get()),
                        new AddItemPlayerAction(ModItems.SALT_FLOUR.get()),
                        new AddItemPlayerAction(Items.GLOW_BERRIES),
                        new AddItemPlayerAction(Items.GLOW_BERRIES),
                        new AddItemPlayerAction(Items.GLOW_BERRIES),
                        new AddItemPlayerAction(Items.GLOW_BERRIES)
                ),
                ModContents.FRY_SALMON_CUBES.get());

        registerPlatingSequenceModels(
                ModItems.IRON_PLATE.get(),
                Arrays.asList(
                        new AddItemPlayerAction(ModItems.POTATO_CUBES.get()),
                        new AddItemPlayerAction(ModItems.POTATO_CUBES.get()),
                        new AddItemPlayerAction(Items.COD)
                ),
                ModContents.GRILLED_FISH_POTATOES.get());

        registerPlatingSequenceModels(
                ModItems.IRON_PLATE.get(),
                Arrays.asList(
                        new AddItemPlayerAction(Items.RABBIT),
                        new AddItemPlayerAction(ModItems.SALT_FLOUR.get()),
                        new AddContentPlayerAction(Contents.HONEY.get()),
                        new AddItemPlayerAction(ModItems.CARROT_SLICES.get()),
                        new AddItemPlayerAction(ModItems.CARROT_SLICES.get()),
                        new AddItemPlayerAction(Items.SWEET_BERRIES)
                ),
                ModContents.DELUXE_ROASTED_RABBIT.get());

        registerPlatingSequenceModels(
                ModItems.IRON_PLATE.get(),
                Arrays.asList(
                        new AddItemPlayerAction(Items.MUTTON),
                        new AddItemPlayerAction(ModItems.SALT_FLOUR.get()),
                        new AddContentPlayerAction(Contents.HONEY.get()),
                        new AddItemPlayerAction(ModItems.CARROT_SLICES.get()),
                        new AddItemPlayerAction(ModItems.CARROT_SLICES.get()),
                        new AddItemPlayerAction(ModItems.CARROT_HEAD.get())
                ),
                ModContents.HONEY_ROASTED_MUTTON.get());

        registerPlatingSequenceModels(
                ModItems.IRON_PLATE.get(),
                Arrays.asList(
                        new AddItemPlayerAction(Items.CHICKEN),
                        new AddItemPlayerAction(Items.SWEET_BERRIES),
                        new AddItemPlayerAction(ModItems.CARROT_SLICES.get()),
                        new AddItemPlayerAction(ModItems.CARROT_SLICES.get()),
                        new AddItemPlayerAction(ModItems.SALT_FLOUR.get()),
                        new AddContentPlayerAction(Contents.HONEY.get()),
                        new AddItemPlayerAction(ModItems.CARROT_HEAD.get())
                ),
                ModContents.DELUXE_ROAST_CHICKEN.get());
    }

    /**
     * 注册一个摆盘配方的所有模型（包括所有前缀步骤）
     *
     * @param container 容器物品
     * @param actionSequence 完整的操作序列
     * @param dish 最终菜肴（可选，用于注册配方映射）
     */
    public static void registerPlatingSequenceModels(Item container,
                                                     List<PlayerAction> actionSequence,
                                                     @Nullable Content dish) {
        PlatingModelManager modelManager = PlatingModelManager.getInstance();

        // 注册配方映射和食用过程
        if (dish != null) {
            modelManager.registerRecipeModel(container, actionSequence, dish);
        }

        // 生成并注册所有前缀模型
        List<ResourceLocation> prefixModels = modelManager.generateAllPrefixModels(container, actionSequence);

        for (ResourceLocation modelId : prefixModels) {
            if (modelId != null && !MODELS_TO_LOAD.contains(modelId)) {
                MODELS_TO_LOAD.add(modelId);
            }
        }
    }

    /**
     * 注册一个菜肴在容器中的所有食用过程模型。
     * @param container 容器
     * @param content 菜肴
     */
    public static void registerEatStageModels(Item container, Content content) {
        if (!(content instanceof DishesContent dish)) {
            return;
        }

        if (!dish.canEat()) {
            LOGGER.warn("{} is inedible", dish);
            return;
        }

        for (int eaten = 1; eaten < dish.getEatCount(); eaten++) {
            ResourceLocation modelId = createEatStageModel(container, dish, eaten);
            if (!MODELS_TO_LOAD.contains(modelId)) {
                MODELS_TO_LOAD.add(modelId);
            }
        }
    }

    /**
     * 创建菜肴的放置模型标识符。
     * @param baseContainer 基础容器
     * @param dishes 菜肴
     * @return 对应的放置模型标识符
     */
    public static ResourceLocation createDishesModel(Item baseContainer, Content dishes) {
        String containerId = BuiltInRegistries.ITEM.getKey(baseContainer).getPath();
        String dishesId = Objects.requireNonNull(TWRegistries.CONTENT.get().getKey(dishes)).getPath();

        return TWCore.createResourceLocation(BakingProcess.MOD_ID, "dishes/" + containerId + "_" + dishesId);
    }

    /**
     * 创建已食用菜肴的放置模型标识符。
     * @param container 基础容器
     * @param dish 菜肴
     * @param eatenCount 已食用次数
     * @return 对应的放置模型标识符
     */
    public static ResourceLocation createEatStageModel(Item container, DishesContent dish, int eatenCount) {
        String containerPath = BuiltInRegistries.ITEM.getKey(container).getPath();
        String dishPath = Objects.requireNonNull(TWRegistries.CONTENT.get().getKey(dish)).getPath();
        return TWCore.createResourceLocation(BakingProcess.MOD_ID, "dishes/eat/" + containerPath + "_" + dishPath + "_" + eatenCount);
    }

    // =========== 定型面团 ===========

    public static void registerShapedDoughAll() {
        MODELS_TO_LOAD.add(createShapedDoughModel((ShapedDoughContent) ModContents.TOAST_EMBRYO.get()));
        MODELS_TO_LOAD.add(createShapedDoughModel((ShapedDoughContent) ModContents.TOAST.get()));
        MODELS_TO_LOAD.add(createShapedDoughModel((ShapedDoughContent) ModContents.CAKE_EMBRYO.get()));
        MODELS_TO_LOAD.add(createShapedDoughModel((ShapedDoughContent) ModContents.BAKED_CAKE_EMBRYO.get()));
    }

    /**
     * 创建定型面团的模型标识符。
     *
     * @param content 定型面团
     * @return 对应的模型标识符
     */
    public static ResourceLocation createShapedDoughModel(ShapedDoughContent content) {
        ResourceLocation contentId = Objects.requireNonNull(TWRegistries.CONTENT.get().getKey(content));
        return TWCore.createResourceLocation(contentId.getNamespace(), "block/" + contentId.getPath());
    }

    // =========== 辅助方法 ===========

    /**
     * 创建物品模型的标识符。
     */
    public static ModelResourceLocation createItemModel(String itemPath) {
        return new ModelResourceLocation(TWCore.createResourceLocation(BakingProcess.MOD_ID, itemPath), "inventory");
    }

    /**
     * 创建步骤所需的额外模型的标识符。
     */
    public static ResourceLocation createProcessModel(String blockPath) {
        return TWCore.createResourceLocation(BakingProcess.MOD_ID, "process/" + blockPath);
    }

    /**
     * 获取已注册的所有模型。
     */
    public static List<ResourceLocation> getRegisteredModels() {
        return new ArrayList<>(MODELS_TO_LOAD);
    }

    /**
     * 获取已注册的模型数量。
     */
    public static int getRegisteredModelCount() {
        return MODELS_TO_LOAD.size();
    }
}