package org.bakingprocess.container;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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
        return stack.is(getEmptyItem());
    }

    @Override
    public boolean canContain(Content content) {
        return content.isIn(ModContents.DISHES.get());
    }

    @Override
    public @Nullable Content extractContent(ItemStack stack) {
        ResourceLocation id = ResourceLocation.tryParse(stack.getOrCreateTag().getString(DISHES_KEY));
        if (id != null) {
            return TWRegistries.CONTENT.get(id);
        }

        return null;
    }

    @Override
    public @NotNull ItemStack replaceContent(@NotNull ItemStack stack, @Nullable Content content) {
        validateReplace(stack, content);

        // 娓呯┖瀹瑰櫒
        if (content == null) {
            if (stack.hasTag()) {
                stack.getOrCreateTag().remove(DISHES_KEY);
            }

            return stack;
        }

        // 鏇挎崲鍐呭�圭�?
        stack.getOrCreateTag().putString(DISHES_KEY, Objects.requireNonNull(TWRegistries.CONTENT.getKey(content)).toString());
        return stack;
    }
}
