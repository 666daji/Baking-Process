package org.bakingprocess.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bakingprocess.BakingProcess;
import org.dfood.block.FoodBlock;
import org.dfood.util.DFoodUtils;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class BakingProcessUtils {
    private static final Logger LOGGER = BakingProcess.LOGGER;

    /**
     * 创建食物方块状态
     */
    public static BlockState createFoodBlockState(BlockState state, int foodCount, Direction facing) {
        if (facing == Direction.UP || facing == Direction.DOWN){
            LOGGER.warn("FoodBlock direction can only be horizontal");
            facing = Direction.EAST;
        }

        if (state.getBlock() instanceof FoodBlock foodBlock) {
            return state
                    .setValue(FoodBlock.FACING, facing)
                    .setValue(foodBlock.NUMBER_OF_FOOD, foodCount);
        }
        return Blocks.AIR.defaultBlockState();
    }

    /**
     * 获取物品对应方块的声音事件组
     * @param itemStack 要获取的物品堆栈
     * @return 获取的声音组
     */
    public static Optional<SoundType> getSoundGroupFromItem(ItemStack itemStack) {
        BlockState state = DFoodUtils.getBlockStateFromItem(itemStack.getItem());
        if (state != null) {
            return Optional.of(state.getSoundType());
        }

        return Optional.empty();
    }

    /**
     * 从BlockPattern.Result中查找符合条件的方块位置
     * @param result BlockPattern匹配结果
     * @param isTargetPositionPredicate 用于判断是否为目标位置的谓词
     * @return 符合条件的方块位置集合
     */
    public static Set<BlockPos> findTargetPositionsFromPattern(BlockPattern.BlockPatternMatch result, Predicate<BlockInWorld> isTargetPositionPredicate) {
        Set<BlockPos> TargetPos = new HashSet<>();
        for (int depth = 0; depth < result.getDepth(); depth++) {
            for (int height = 0; height < result.getHeight(); height++) {
                for (int width = 0; width < result.getWidth(); width++) {
                    // 获取该位置的缓存方块
                    BlockInWorld cachedPos = result.getBlock(width, height, depth);

                    if (isTargetPositionPredicate.test(cachedPos)) {
                        // 找到匹配的位置，返回对应的世界坐标
                        TargetPos.add(cachedPos.getPos());
                    }
                }
            }
        }
        return TargetPos;
    }

    /**
     * 序列化BlockPos为NbtCompound
     */
    public static CompoundTag serializeBlockPos(BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        return tag;
    }

    /**
     * 从NbtCompound反序列化BlockPos
     */
    public static BlockPos deserializeBlockPos(CompoundTag tag) {
        if (tag.contains("x") && tag.contains("y") && tag.contains("z")) {
            return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        }
        return null;
    }

    /**
     * 根据物品堆和朝向创建对应的方块状态
     * @param stack 物品堆栈
     * @param facing 方块朝向
     * @return 对应的方块状态 如果返回值为{@linkplain Blocks#AIR}，则说明该物品没有对应的方块状态
     */
    public static BlockState createCountBlockstate(ItemStack stack, Direction facing) {
        Item item = stack.getItem();
        BlockState blockState = DFoodUtils.getBlockStateFromItem(item);

        if (blockState == null) {
            return Blocks.AIR.defaultBlockState();
        }

        Block block = blockState.getBlock();

        // 处理FoodBlock（包括DoubleBlockItem中的FoodBlock）
        if (block instanceof FoodBlock) {
            return createFoodBlockState(blockState, stack.getCount(), facing);
        }

        // 处理其他方块，检查是否有水平朝向属性
        if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        }

        return blockState;
    }

    /**
     * 按中心点缩放VoxelShape
     * @param shape 待缩放的形状
     * @param scale 缩放比例 只能为正数
     * @return 缩放后的形状
     */
    public static VoxelShape scale(VoxelShape shape, double scale) {
        if (shape.isEmpty()) {
            return Shapes.empty();
        }

        VoxelShape[] result = new VoxelShape[]{Shapes.empty()};

        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            // 计算中心点
            double centerX = (minX + maxX) / 2;
            double centerY = (minY + maxY) / 2;
            double centerZ = (minZ + maxZ) / 2;

            // 相对于中心点进行缩放
            double newMinX = centerX + (minX - centerX) * scale;
            double newMinY = centerY + (minY - centerY) * scale;
            double newMinZ = centerZ + (minZ - centerZ) * scale;
            double newMaxX = centerX + (maxX - centerX) * scale;
            double newMaxY = centerY + (maxY - centerY) * scale;
            double newMaxZ = centerZ + (maxZ - centerZ) * scale;

            VoxelShape scaledBox = Shapes.box(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
            result[0] = Shapes.or(result[0], scaledBox);
        });

        return result[0];
    }

    /**
     * 按原点缩放VoxelShape
     * @param shape 待缩放的形状
     * @param scale 缩放比例
     * @return 缩放后的形状
     */
    public static VoxelShape scaleFromOrigin(VoxelShape shape, double scale) {
        if (shape.isEmpty()) {
            return Shapes.empty();
        }

        VoxelShape[] result = new VoxelShape[]{Shapes.empty()};

        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double newMinX = minX * scale;
            double newMinY = minY * scale;
            double newMinZ = minZ * scale;
            double newMaxX = maxX * scale;
            double newMaxY = maxY * scale;
            double newMaxZ = maxZ * scale;

            VoxelShape scaledBox = Shapes.box(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
            result[0] = Shapes.or(result[0], scaledBox);
        });

        return result[0];
    }
}
