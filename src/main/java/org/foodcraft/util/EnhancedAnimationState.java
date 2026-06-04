package org.foodcraft.util;

import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

/**
 * 客户端动画状态管理器（精简增强版）。
 * <p>
 * 核心功能：
 * <ul>
 *   <li>基于游戏刻的动画时间追踪（1 tick = 50 ms）</li>
 *   <li>支持停止并保留进度 / 从保留进度恢复</li>
 *   <li>提供完全重置方法 {@link #reset()}</li>
 *   <li>记录是否曾启动过 {@link #hasProcess()}</li>
 *   <li>不包含任何服务端同步或 NBT 序列化</li>
 * </ul>
 * </p>
 */
public class EnhancedAnimationState {
    private static final long STOPPED = Long.MAX_VALUE;

    // 核心计时
    private long updatedAt = STOPPED;   // 最近更新时间戳（毫秒），STOPPED 表示完全停止
    private long timeRunning = 0L;      // 当前动画已运行毫秒数（受速度影响）

    // 进度保存
    private long savedProgress = 0L;
    private boolean hasSaved = false;

    // 元数据
    private boolean everStarted = false; // 是否曾经启动过

    // 倒放标记
    public boolean reversed = false;

    /**
     * 从头开始动画，清除所有保存的进度。
     * @param age 当前实体年龄（刻）
     */
    public void start(int age) {
        this.updatedAt = age * 1000L / 20L;
        this.timeRunning = 0L;
        this.everStarted = true;
        clearSavedProgress();
        this.reversed = false;
    }

    /**
     * 从上次保存的进度恢复动画（如果存在保存进度），否则等同于 start。
     * @param age 当前实体年龄（刻）
     */
    public void startFromSavedProgress(int age) {
        if (hasSaved) {
            this.updatedAt = age * 1000L / 20L;
            this.timeRunning = this.savedProgress;
            this.everStarted = true;
            clearSavedProgress();
        } else {
            start(age);
        }
    }

    public void startIfNotRunning(int age) {
        if (!this.isRunning()) {
            this.startFromSavedProgress(age);
        }
    }

    /**
     * 停止动画并保留当前进度，以便后续通过 {@link #startFromSavedProgress(int)} 恢复。
     * 停止后 isRunning() 返回 false。
     */
    public void stopAndKeepProgress() {
        if (isRunning()) {
            this.savedProgress = this.timeRunning;
            this.hasSaved = true;
        }
        this.updatedAt = STOPPED;
        this.timeRunning = 0L;
    }

    /**
     * 完全重置动画状态（清除所有时间、保存进度、启动标记）。
     * 重置后效果等同于新实例。
     */
    public void reset() {
        this.updatedAt = STOPPED;
        this.timeRunning = 0L;
        this.everStarted = false;
        clearSavedProgress();
        this.reversed = false;
    }

    /**
     * 更新动画计时器。
     * @param progress 当前连续刻数（通常为 entity.age + tickDelta）
     * @param speedMultiplier 速度倍率
     */
    public void update(float progress, float speedMultiplier) {
        if (!isRunning()) return;
        long now = MathHelper.lfloor(progress * 1000.0F / 20.0F);
        long delta = now - this.updatedAt;
        if (delta > 0) {
            long step = (long)((float)delta * speedMultiplier);
            if (reversed) {
                // 倒放：减少 timeRunning，但不超过 0
                if (step >= this.timeRunning) {
                    this.timeRunning = 0L;
                    this.updatedAt = STOPPED;
                    return;
                } else {
                    this.timeRunning -= step;
                }
            } else {
                // 正放：增加
                this.timeRunning += step;
            }
            this.updatedAt = now;
        }
    }

    /**
     * 动画是否正在运行（未停止）。
     */
    public boolean isRunning() {
        return updatedAt != STOPPED;
    }

    /**
     * 动画是否曾经启动过（至少调用过一次 start 或 startFromSavedProgress 且未被 reset）。
     */
    public boolean hasProcess() {
        return everStarted;
    }

    /**
     * 获取当前动画已运行毫秒数（若停止但有保存进度，返回保存的进度）。
     */
    public long getTimeRunning() {
        if (!isRunning() && hasSaved) return savedProgress;
        return timeRunning;
    }

    /**
     * 如果动画正在运行，则执行回调。
     */
    public void run(Consumer<EnhancedAnimationState> consumer) {
        if (isRunning()) {
            consumer.accept(this);
        }
    }

    @Override
    public String toString() {
        return String.format(
                "EnhancedAnimationState{ running=%s, hasProcess=%s, timeRunning=%d ms, savedProgress=%d ms, hasSaved=%s }",
                isRunning(), hasProcess(), getTimeRunning(), savedProgress, hasSaved
        );
    }

    private void clearSavedProgress() {
        this.savedProgress = 0L;
        this.hasSaved = false;
    }
}