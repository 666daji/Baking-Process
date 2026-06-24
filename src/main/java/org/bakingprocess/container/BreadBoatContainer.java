package org.bakingprocess.container;

import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import org.bakingprocess.item.BreadBoatItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twcore.container.ContainerType;
import org.twcore.content.Content;
import org.twcore.content.FoodContent;
import org.twcore.registry.Contents;
import org.twcore.registry.TWRegistries;

import java.util.function.Supplier;

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
            return TWRegistries.CONTENT.get().getValue(ResourceLocation.tryParse(soupKey));
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
            stack.getOrCreateTag().putString(SOUP_KEY, TWRegistries.CONTENT.get().getKey(content).toString());
        }

        return stack;
    }

    /**
     * 允许装入硬面包船的汤类型枚举。
     */
    public enum BreadBoatSoupType implements StringRepresentable {
        BEETROOT_SOUP(Contents.BEETROOT_SOUP),
        MUSHROOM_STEW(Contents.MUSHROOM_STEW);

        private final Supplier<Content> content;
        private final String id;

        BreadBoatSoupType(Supplier<Content> content, String id) {
            this.content = content;
            this.id = id;
        }

        BreadBoatSoupType(RegistryObject<Content> content) {
            this(content, content.getId().getPath());
        }

        /**
         * 获取对应的内容物。
         * @return 对应的内容物
         */
        public Content getContent() {
            return content.get();
        }

        @Override
        public String getSerializedName() {
            return id;
        }

        public FoodProperties getFoodComponent() {
            if (content.get() instanceof FoodContent foodContent) {
                return foodContent.getFoodComponent();
            }
            return null;
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
                if (type.content.get().equals(content)) {
                    return type;
                }
            }
            return null;
        }
    }
}