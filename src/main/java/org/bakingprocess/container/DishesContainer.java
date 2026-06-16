package org.bakingprocess.container;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.bakingprocess.registry.ModContents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twcore.container.ContainerType;
import org.twcore.content.Content;
import org.twcore.registry.TWRegistries;

import java.util.Objects;

public class DishesContainer extends ContainerType {
    public static final String DISHES_KEY = "dishes_type";

    public DishesContainer(ContainerSettings settings) {
        super(settings);
    }

    @Override
    public boolean matches(ItemStack stack) {
        return stack.isOf(getEmptyItem());
    }

    @Override
    public boolean canContain(Content content) {
        return content.isIn(ModContents.DISHES);
    }

    @Override
    public @Nullable Content extractContent(ItemStack stack) {
        Identifier id = Identifier.tryParse(stack.getOrCreateNbt().getString(DISHES_KEY));
        if (id != null) {
            return TWRegistries.CONTENT.get(id);
        }

        return null;
    }

    @Override
    public @NotNull ItemStack replaceContent(@NotNull ItemStack stack, @Nullable Content content) {
        validateReplace(stack, content);

        // 清空容器
        if (content == null) {
            if (stack.hasNbt()) {
                stack.getOrCreateNbt().remove(DISHES_KEY);
            }

            return stack;
        }

        // 替换内容物
        stack.getOrCreateNbt().putString(DISHES_KEY, Objects.requireNonNull(TWRegistries.CONTENT.getId(content)).toString());
        return stack;
    }
}
