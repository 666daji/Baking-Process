package org.bakingprocess.content;

import net.minecraft.item.FoodComponent;
import org.jetbrains.annotations.NotNull;
import org.twcore.content.FoodContent;

public class DishesContent extends FoodContent {
    /**
     * 表示一个还未烤制的菜肴，
     * 此时玩家不能食用该菜肴。
     */
    public static final FoodComponent RAW_DISHES = new FoodComponent.Builder()
            .hunger(0).saturationModifier(0).build();

    protected final int eatCount;

    public DishesContent(@NotNull String category, FoodComponent foodComponent, int eatCount) {
        super(category, foodComponent);
        this.eatCount = eatCount;
    }

    public DishesContent(@NotNull String category) {
        this(category, RAW_DISHES, 0);
    }

    /**
     * 检查该菜肴是否还未烤制。
     * @return 如果已烤制则返回true
     */
    public boolean canEat() {
        return !getFoodComponent().equals(RAW_DISHES);
    }

    /**
     * 返回菜肴的食用次数。
     * @return 肴的食用次数，不可食用的菜肴返回0
     */
    public int getEatCount() {
        if (!canEat()) {
            return 0;
        }

        return eatCount;
    }
}
