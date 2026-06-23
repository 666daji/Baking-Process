package org.bakingprocess.block.process;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bakingprocess.block.PlateBlock;
import org.bakingprocess.block.entity.PlatableBlockEntity;
import org.bakingprocess.content.DishesContent;
import org.bakingprocess.food.SimpleFoodComponent;
import org.twcore.api.process.AbstractProcess;
import org.twcore.process.step.Step;
import org.twcore.process.step.StepExecutionContext;
import org.twcore.process.step.StepResult;

public class EatDishesProcess<T extends BlockEntity & PlatableBlockEntity> extends AbstractProcess<T> {
    public static final String STEP_EAT = "eat";

    private int remainingEats;
    private int totalEats;

    public EatDishesProcess() {
        registerStep(STEP_EAT, new EatStep());
    }

    public int getEatenCount() {
        return totalEats - remainingEats;
    }

    public int getTotalEats() {
        return totalEats;
    }

    @Override
    protected String getInitialStepId() {
        return STEP_EAT;
    }

    @Override
    protected void onStart(Level world, T blockEntity) {
        DishesContent outcome = blockEntity.getOutcome();
        if (outcome == null || !outcome.canEat()) {
            reset();
            return;
        }

        // 如果 remainingEats 为 0（首次启动或重置后），初始化
        if (remainingEats <= 0) {
            totalEats = outcome.getEatCount();
            remainingEats = totalEats;
        }
    }

    @Override
    protected void onReset() {
        remainingEats = 0;
        totalEats = 0;
    }

    @Override
    public void readFromNbt(CompoundTag nbt) {
        super.readFromNbt(nbt);
        remainingEats = nbt.getInt("remaining_eats");
        totalEats = nbt.getInt("total_eats");
    }

    @Override
    public void writeToNbt(CompoundTag nbt) {
        super.writeToNbt(nbt);
        nbt.putInt("remaining_eats", remainingEats);
        nbt.putInt("total_eats", totalEats);
    }

    private class EatStep implements Step<T> {
        @Override
        public StepResult execute(StepExecutionContext<T> context) {
            // 检查条件
            DishesContent outcome = context.blockEntity().getOutcome();
            if (outcome == null || context.blockState().getValue(PlateBlock.IS_COVERED)) {
                return StepResult.complete(InteractionResult.PASS);
            }

            if (!context.getHeldItemStack().isEmpty()) {
                return StepResult.fail(null, InteractionResult.PASS);
            }

            // 计算本次应得的食物比例
            if (totalEats <= 0) {
                return StepResult.fail(null, InteractionResult.FAIL);
            }

            // 检查食用条件
            boolean canEat = (context.player().getAbilities().invulnerable || outcome.getFoodComponent().canAlwaysEat() || context.player().getFoodData().needsFood());
            if (!canEat) {
                if (remainingEats == totalEats) {
                    return StepResult.complete(InteractionResult.PASS);
                }

                return StepResult.fail(STEP_EAT, InteractionResult.PASS);
            }

            // 开始食用
            eat(outcome, context);

            // 播放音效和粒子
            context.playSound(SoundEvents.GENERIC_EAT);

            // 减少剩余次数
            remainingEats--;
            context.blockEntity().setChanged(); // 保存流程状态

            if (remainingEats <= 0) {
                // 吃完了，清空盘子
                context.blockEntity().onEatComplete(context.world(), context.pos(), context.player(), context.hand(), context.hit());
                return StepResult.complete(InteractionResult.SUCCESS);
            } else {
                // 还有剩余，继续停留在当前步骤，等待下一次右键
                return StepResult.continueSameStep(InteractionResult.SUCCESS);
            }
        }

        protected void eat(DishesContent outcome, StepExecutionContext<T> context) {
            // 每次吃一口，比例 = 1 / totalEats
            FoodProperties fullFood = outcome.getFoodComponent();
            SimpleFoodComponent fullSimple = SimpleFoodComponent.fromFoodComponent(fullFood);
            // 计算百分比（整数百分比，最后一口可能多一点点，但可接受）
            int percentPerEat = (int) Math.round(100.0 / totalEats);
            SimpleFoodComponent perEatFood = fullSimple.percent(percentPerEat);
            // 应用饥饿值和饱和度
            perEatFood.eat(context.player());

            // 处理状态效果：每个效果持续时间除以 totalEats，概率不变
            for (Pair<MobEffectInstance, Float> pair : fullFood.getEffects()) {
                MobEffectInstance effect = pair.getFirst();
                float probability = pair.getSecond();
                int newDuration = Math.max(1, effect.getDuration() / totalEats);
                MobEffectInstance newEffect = new MobEffectInstance(
                        effect.getEffect(),
                        newDuration,
                        effect.getAmplifier(),
                        effect.isAmbient(),
                        effect.isVisible(),
                        effect.showIcon()
                );
                // 应用效果时考虑概率
                if (probability >= 1.0f || context.world().random.nextFloat() < probability) {
                    context.player().addEffect(newEffect);
                }
            }
        }
    }

    @Override
    protected String getCustomStatusInfo() {
        return "剩余食用次数: " + remainingEats + "\n";
    }
}