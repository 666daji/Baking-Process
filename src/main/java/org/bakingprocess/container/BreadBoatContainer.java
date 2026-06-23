package org.bakingprocess.container;

import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.bakingprocess.item.BreadBoatItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twcore.container.ContainerType;
import org.twcore.content.Content;
import org.twcore.content.FoodContent;
import org.twcore.registry.Contents;
import org.twcore.registry.TWRegistries;

import java.util.Objects;

/**
 * 硬面包船容器类型。
 * <p>
 * 用于承载汤类内容物的硬面包船。
 * </p>
 */
public class BreadBoatContainer extends ContainerType {
    public static final String SOUP_KEY = "soup_type";

    public BreadBoatContainer(ContainerSettings settings) {
        super(settings);
    }

    @Override
    public boolean matches(ItemStack stack) {
        return stack.getItem() instanceof BreadBoatItem;
    }

    @Override
    public boolean canContain(Content content) {
        return BreadBoatSoupType.fromContent(content) != null;
    }

    @Override
    public @Nullable Content extractContent(ItemStack stack) {
        if (stack.getTag() != null && stack.hasTag()
                && stack.getTag().contains(SOUP_KEY, Tag.TAG_STRING)) {
            String soupKey = stack.getTag().getString(SOUP_KEY);
            return TWRegistries.CONTENT.get(ResourceLocation.tryParse(soupKey));
        }

        return null;
    }

    @Override
    public @NotNull ItemStack replaceContent(@NotNull ItemStack stack, @Nullable Content content) {
        validateReplace(stack, content);

        // 清空容器
        if (content == null) {
            if (stack.hasTag()) {
                stack.getOrCreateTag().remove(SOUP_KEY);
            }

            return stack;
        }

        // 替换内容物
        if (canContain(content)) {
            stack.getOrCreateTag().putString(SOUP_KEY, TWRegistries.CONTENT.getKey(content).toString());
        }

        return stack;
    }

    /**
     * 允许装入硬面包船的汤类型枚举。
     */
    public enum BreadBoatSoupType implements StringRepresentable {
        BEETROOT_SOUP((FoodContent) Contents.BEETROOT_SOUP),
        MUSHROOM_STEW((FoodContent) Contents.MUSHROOM_STEW);

        private final FoodContent content;

        BreadBoatSoupType(FoodContent content) {
            if (content.isIn(Contents.SOUP)) {
                this.content = content;
            } else {
                throw new IllegalArgumentException();
            }
        }

        /**
         * 获取对应的内容物。
         * @return 对应的内容物
         */
        public FoodContent getContent() {
            return content;
        }

        @Override
        public String getSerializedName() {
            return Objects.requireNonNull(TWRegistries.CONTENT.getKey(content)).getPath();
        }

        public FoodProperties getFoodComponent() {
            return this.content.getFoodComponent();
        }

        /**
         * 从内容物获取对应的汤类型。
         *
         * @param content 内容物
         * @return 对应的汤类型，如果没有则返回null
         */
        @Nullable
        public static BreadBoatContainer.BreadBoatSoupType fromContent(@Nullable Content content) {
            if (content == null) {
                return null;
            }

            for (BreadBoatSoupType type : values()) {
                if (type.content.equals(content)) {
                    return type;
                }
            }
            return null;
        }
    }
}