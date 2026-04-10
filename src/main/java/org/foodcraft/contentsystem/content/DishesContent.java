package org.foodcraft.contentsystem.content;

import net.minecraft.item.FoodComponent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class DishesContent extends FoodContent {
    protected final int eatCount;

    public DishesContent(Identifier id, FoodComponent foodComponent, int eatCount) {
        super(id, foodComponent);
        this.eatCount = eatCount;
    }

    @Override
    public @NotNull String getCategory() {
        return ContentCategories.DISHES;
    }

    public int getEatCount() {
        return eatCount;
    }
}
