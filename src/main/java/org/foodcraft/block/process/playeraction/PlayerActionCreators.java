package org.foodcraft.block.process.playeraction;

import org.foodcraft.block.process.step.StepExecutionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 提供构建复杂 {@link PlayerActionFactory.PlayerActionCreator} 的工具方法。
 *
 * <p>所有方法均返回 {@link PlayerActionFactory.PlayerActionCreator} 函数式接口的实例，
 * 可以通过组合这些方法来满足各种创建逻辑，而无需编写新的实现类。</p>
 *
 * <h3>常见用例</h3>
 * <pre>{@code
 * // 按顺序尝试多个已注册的 creator
 * PlayerActionCreator creator = PlayerActionCreators.firstNonNull(
 *     PlayerActionFactory.getRegisteredCreator("add_item"),
 *     PlayerActionFactory.getRegisteredCreator("use_tool")
 * );
 *
 * // 添加前置条件
 * PlayerActionCreator conditionalCreator = PlayerActionCreators.withPrecondition(
 *     creator,
 *     ctx -> ctx.player.isSneaking()
 * );
 *
 * // 后置处理
 * PlayerActionCreator decoratedCreator = PlayerActionCreators.map(
 *     conditionalCreator,
 *     action -> action.withExtraData("sneak", true)
 * );
 * }</pre>
 */
public final class PlayerActionCreators {

    private PlayerActionCreators() {
        // 工具类
    }

    /**
     * 总是返回同一个 {@link PlayerAction} 的 creator。
     */
    public static PlayerActionFactory.PlayerActionCreator constant(@NotNull PlayerAction action) {
        return ctx -> action;
    }

    /**
     * 将 {@link Function} 适配为 {@link PlayerActionFactory.PlayerActionCreator}。
     *
     * @apiNote 如果函数返回 null，则 creator 也返回 null。
     */
    public static PlayerActionFactory.PlayerActionCreator fromFunction(@NotNull Function<StepExecutionContext<?>, PlayerAction> function) {
        return function::apply;
    }

    // ==================== 条件控制 ====================

    /**
     * 仅在上下文满足给定谓词时才调用内部 creator，否则返回 null。
     *
     * @param creator      被包装的 creator
     * @param precondition 前置条件谓词
     */
    public static PlayerActionFactory.PlayerActionCreator withPrecondition(@NotNull PlayerActionFactory.PlayerActionCreator creator,
                                                       @NotNull Predicate<StepExecutionContext<?>> precondition) {
        return ctx -> precondition.test(ctx) ? creator.create(ctx) : null;
    }

    /**
     * 如果 creator 返回 null，则使用默认值。
     */
    public static PlayerActionFactory.PlayerActionCreator orElse(@NotNull PlayerActionFactory.PlayerActionCreator creator,
                                                                 @NotNull PlayerAction defaultAction) {
        return ctx -> {
            PlayerAction action = creator.create(ctx);
            return action != null ? action : defaultAction;
        };
    }

    /**
     * 如果 creator 返回 null，则通过 supplier 获取默认值。
     */
    public static PlayerActionFactory.PlayerActionCreator orElseGet(@NotNull PlayerActionFactory.PlayerActionCreator creator,
                                                @NotNull Supplier<PlayerAction> defaultSupplier) {
        return ctx -> {
            PlayerAction action = creator.create(ctx);
            return action != null ? action : defaultSupplier.get();
        };
    }

    // ==================== 链式组合 ====================

    /**
     * 按顺序尝试多个 creator，返回第一个非 null 的结果。
     * 如果所有 creator 都返回 null，则最终返回 null。
     */
    public static PlayerActionFactory.PlayerActionCreator firstNonNull(@NotNull PlayerActionFactory.PlayerActionCreator... creators) {
        return firstNonNull(Arrays.asList(creators));
    }

    /**
     * 按顺序尝试列表中的 creator，返回第一个非 null 的结果。
     */
    public static PlayerActionFactory.PlayerActionCreator firstNonNull(@NotNull List<PlayerActionFactory.PlayerActionCreator> creators) {
        if (creators.isEmpty()) {
            return ctx -> null;
        }
        return ctx -> {
            for (PlayerActionFactory.PlayerActionCreator creator : creators) {
                PlayerAction action = creator.create(ctx);
                if (action != null) {
                    return action;
                }
            }
            return null;
        };
    }

    // ==================== 后置处理 ====================

    /**
     * 在 creator 成功返回非 null 时，应用转换函数。
     */
    public static PlayerActionFactory.PlayerActionCreator map(@NotNull PlayerActionFactory.PlayerActionCreator creator,
                                          @NotNull Function<PlayerAction, PlayerAction> mapper) {
        return ctx -> {
            PlayerAction action = creator.create(ctx);
            return action == null ? null : mapper.apply(action);
        };
    }

    /**
     * 在 creator 成功返回非 null 时，通过转换函数生成新的 action。
     * 如果转换函数返回 null，则最终也返回 null。
     */
    public static PlayerActionFactory.PlayerActionCreator flatMap(@NotNull PlayerActionFactory.PlayerActionCreator creator,
                                              @NotNull Function<PlayerAction, PlayerAction> mapper) {
        return map(creator, mapper);
    }

    // ==================== 条件分支 ====================

    /**
     * 根据上下文条件选择不同的 creator。
     * 条件按添加顺序评估，返回第一个满足条件的 creator 产生的结果。
     * 如果没有条件满足，可以提供一个默认 creator（或 null）。
     *
     * @param cases        条件到 creator 的映射（顺序敏感）
     * @param defaultCreator 当所有条件都不满足时使用的 creator，可为 null
     */
    public static PlayerActionFactory.PlayerActionCreator conditional(
            @NotNull List<Map.Entry<Predicate<StepExecutionContext<?>>, PlayerActionFactory.PlayerActionCreator>> cases,
            @Nullable PlayerActionFactory.PlayerActionCreator defaultCreator) {
        return ctx -> {
            for (Map.Entry<Predicate<StepExecutionContext<?>>, PlayerActionFactory.PlayerActionCreator> entry : cases) {
                if (entry.getKey().test(ctx)) {
                    return entry.getValue().create(ctx);
                }
            }
            return defaultCreator != null ? defaultCreator.create(ctx) : null;
        };
    }

    // ==================== 缓存 ====================

    /**
     * 基于某个上下文键缓存 creator 的结果。
     * 主要用于避免重复计算昂贵的创建逻辑。
     *
     * @param creator    被缓存的 creator
     * @param keyExtractor 从上下文中提取缓存键的函数（键为 null 时不缓存）
     * @return 带缓存的 creator
     */
    public static PlayerActionFactory.PlayerActionCreator cached(@NotNull PlayerActionFactory.PlayerActionCreator creator,
                                             @NotNull Function<StepExecutionContext<?>, ?> keyExtractor) {
        Map<Object, PlayerAction> cache = new ConcurrentHashMap<>();
        return ctx -> {
            Object key = keyExtractor.apply(ctx);
            if (key == null) {
                return creator.create(ctx);
            }
            return cache.computeIfAbsent(key, k -> creator.create(ctx));
        };
    }

    // ==================== 组合多个结果 ====================

    /**
     * 从多个 creator 中收集所有非 null 的结果，并通过组合器函数合并为一个 action。
     * 如果没有任何 creator 返回非 null，则最终返回 null。
     *
     * @param creators     要尝试的多个 creator
     * @param combiner     合并多个 action 的函数
     */
    public static PlayerActionFactory.PlayerActionCreator combine(@NotNull List<PlayerActionFactory.PlayerActionCreator> creators,
                                              @NotNull Function<List<PlayerAction>, PlayerAction> combiner) {
        return ctx -> {
            List<PlayerAction> results = new java.util.ArrayList<>();
            for (PlayerActionFactory.PlayerActionCreator creator : creators) {
                PlayerAction action = creator.create(ctx);
                if (action != null) {
                    results.add(action);
                }
            }
            if (results.isEmpty()) {
                return null;
            }
            return combiner.apply(results);
        };
    }

    /**
     * 两个 creator 的结果通过二元函数合并。
     * 只有当两个 creator 都返回非 null 时才会合并，否则返回 null。
     */
    public static PlayerActionFactory.PlayerActionCreator combineTwo(@NotNull PlayerActionFactory.PlayerActionCreator first,
                                                 @NotNull PlayerActionFactory.PlayerActionCreator second,
                                                 @NotNull BiPredicate<PlayerAction, PlayerAction> validator,
                                                 @NotNull Function<PlayerAction[], PlayerAction> merger) {
        return ctx -> {
            PlayerAction a1 = first.create(ctx);
            if (a1 == null) return null;
            PlayerAction a2 = second.create(ctx);
            if (a2 == null) return null;
            if (!validator.test(a1, a2)) return null;
            return merger.apply(new PlayerAction[]{a1, a2});
        };
    }

    // ==================== 类型转换 ====================

    /**
     * 基于上下文从已注册类型中动态获取 creator 并调用。
     * 例如，根据上下文中的一个标识符从 {@code PlayerActionFactory.CREATORS} 中查找。
     *
     * @param typeExtractor 从上下文提取类型字符串的函数
     * @return 动态 creator
     */
    public static PlayerActionFactory.PlayerActionCreator dynamicFromType(@NotNull Function<StepExecutionContext<?>, String> typeExtractor) {
        Objects.requireNonNull(typeExtractor, "typeExtractor cannot be null");
        return ctx -> {
            String type = typeExtractor.apply(ctx);
            if (type == null) return null;
            PlayerActionFactory.PlayerActionCreator creator = PlayerActionFactory.getRegisteredCreator(type);
            if (creator == null) return null;
            return creator.create(ctx);
        };
    }
}