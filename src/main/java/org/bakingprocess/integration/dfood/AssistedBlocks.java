package org.bakingprocess.integration.dfood;

import net.minecraft.world.food.Foods;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.bakingprocess.BakingProcess;
import org.dfood.block.FoodBlocks;
import org.dfood.sound.ModSoundGroups;
import org.twcore.api.util.IntPropertyManager;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class AssistedBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BakingProcess.MOD_ID);

    public static Set<RegistryObject<Block>> assistedBlocks = new HashSet<>();

    // 汤类
    public static final RegistryObject<Block> CRIPPLED_RABBIT_STEW = registerAssistedStewBlock("crippled_rabbit_stew",
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN).strength(0.1F, 0.1F).noOcclusion()
                    .sound(SoundType.DECORATED_POT).pushReaction(PushReaction.DESTROY),
            (settings, maxUse) -> new CrippledStewBlock(settings, maxUse, Foods.RABBIT_STEW, FoodBlocks.RABBIT_STEW));

    public static final RegistryObject<Block> CRIPPLED_MUSHROOM_STEW = registerAssistedStewBlock("crippled_mushroom_stew",
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN).strength(0.1F, 0.1F).noOcclusion()
                    .sound(SoundType.DECORATED_POT).pushReaction(PushReaction.DESTROY),
            (settings, maxUse) -> new CrippledStewBlock(settings, maxUse, Foods.MUSHROOM_STEW, FoodBlocks.MUSHROOM_STEW));

    public static final RegistryObject<Block> CRIPPLED_BEETROOT_SOUP = registerAssistedStewBlock("crippled_beetroot_soup",
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN).strength(0.1F, 0.1F).noOcclusion()
                    .sound(SoundType.DECORATED_POT).pushReaction(PushReaction.DESTROY),
            (settings, maxUse) -> new CrippledStewBlock(settings, maxUse, Foods.BEETROOT_SOUP, FoodBlocks.BEETROOT_SOUP));

    public static final RegistryObject<Block> CRIPPLED_SUSPICIOUS_STEW = registerAssistedStewBlock("crippled_suspicious_stew",
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN).strength(0.1F, 0.1F).noOcclusion()
                    .sound(SoundType.DECORATED_POT).pushReaction(PushReaction.DESTROY),
            (settings, maxUse) -> new CrippledSuspiciousStewBlock(settings, maxUse, Foods.SUSPICIOUS_STEW, FoodBlocks.SUSPICIOUS_STEW));

    // 桶类
    public static final RegistryObject<Block> CRIPPLED_WATER_BUCKET = registerAssistedBlock("crippled_water_bucket",
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE).strength(0.2F, 0.2F).noOcclusion()
                    .sound(ModSoundGroups.WATER_BUCKET).pushReaction(PushReaction.DESTROY),
            (settings, maxUse) -> new CrippledBucketBlock(settings, maxUse, FoodBlocks.WATER_BUCKET, Potions.WATER), 3);
    public static final RegistryObject<Block> CRIPPLED_MILK_BUCKET = registerAssistedBlock("crippled_milk_bucket",
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE).strength(0.2F, 0.2F).noOcclusion()
                    .sound(ModSoundGroups.WATER_BUCKET).pushReaction(PushReaction.DESTROY),
            (settings, maxUse) -> new CrippledBucketBlock(settings, maxUse, FoodBlocks.MILK_BUCKET, null), 3);

    private static RegistryObject<Block> registerAssistedStewBlock(String name,
                                                                    Supplier<BlockBehaviour.Properties> settingsSupplier,
                                                                    BiFunction<BlockBehaviour.Properties, Integer, Block> blockCreator) {
        return registerAssistedBlock(name, settingsSupplier, blockCreator, 4);
    }

    /**
     * 注册残缺的方块
     * @param name 方块id
     * @param settingsSupplier 方块设置供应器
     * @param blockCreator 残缺方块的构造函数
     * @param maxUse 最大使用次数
     * @return 注册后的RegistryObject
     */
    private static RegistryObject<Block> registerAssistedBlock(String name,
                                                                Supplier<BlockBehaviour.Properties> settingsSupplier,
                                                                BiFunction<BlockBehaviour.Properties, Integer, Block> blockCreator,
                                                                int maxUse) {
        return BLOCKS.register(name, () -> {
            IntPropertyManager.preCache("number_of_use", maxUse);
            Block block = blockCreator.apply(settingsSupplier.get(), maxUse);
            return block;
        });
    }

    public static void registerAll(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }

    /**
     * @deprecated 使用 registerAll(IEventBus) 代替
     */
    @Deprecated
    public static void registerAssistedBlocks() {}
}
