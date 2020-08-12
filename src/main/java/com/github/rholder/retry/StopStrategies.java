/*
 * Copyright 2012-2015 Ray Holder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rholder.retry;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

/**
 * 用于创建{@link StopStrategy}实例的工厂类
 *
 * @author JB
 */
public final class StopStrategies {

    /** 表示充不停止重试的策略 */
    private static final StopStrategy NEVER_STOP = new NeverStopStrategy();

    private StopStrategies() {
    }

    /**
     * 返回一个从不停止重试任务的策略，即一直重试知道任务执行成功
     *
     * @return a stop strategy which never stops
     */
    public static StopStrategy neverStop() {
        return NEVER_STOP;
    }

    /**
     * 返回停止策略，该策略在N次失败尝试后停止重试
     *
     * @param attemptNumber the number of failed attempts before stopping
     * @return a stop strategy which stops after {@code attemptNumber} attempts
     */
    public static StopStrategy stopAfterAttempt(int attemptNumber) {
        return new StopAfterAttemptStrategy(attemptNumber);
    }

    /**
     * 返回在给定延迟后停止的停止策略.
     * 如果尝试失败，则此{@link StopStrategy}将检查从第一次尝试开始经过的时间是否超过了给定的延迟时间.
     * 如果已超过此延迟，则使用此策略将导致重试停止
     *
     * @param delayInMillis the delay, in milliseconds, starting from first attempt
     * @return a stop strategy which stops after {@code delayInMillis} time in milliseconds
     * @deprecated Use {@link #stopAfterDelay(long, TimeUnit)} instead.
     */
    @Deprecated
    public static StopStrategy stopAfterDelay(long delayInMillis) {
        return stopAfterDelay(delayInMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns a stop strategy which stops after a given delay. If an
     * unsuccessful attempt is made, this {@link StopStrategy} will check if the
     * amount of time that's passed from the first attempt has exceeded the
     * given delay amount. If it has exceeded this delay, then using this
     * strategy causes the retrying to stop.
     *
     * @param duration the delay, starting from first attempt
     * @param timeUnit the unit of the duration
     * @return a stop strategy which stops after {@code delayInMillis} time in milliseconds
     */
    public static StopStrategy stopAfterDelay(long duration, @Nonnull TimeUnit timeUnit) {
        Preconditions.checkNotNull(timeUnit, "The time unit may not be null");
        return new StopAfterDelayStrategy(timeUnit.toMillis(duration));
    }

    /**
     * 从不停止重试任务的策略，即一直重试知道任务执行成功
     */
    @Immutable
    private static final class NeverStopStrategy implements StopStrategy {
        @Override
        public boolean shouldStop(Attempt failedAttempt) {
            return false;
        }
    }

    /**
     * 在N次失败尝试后停止重试
     */
    @Immutable
    private static final class StopAfterAttemptStrategy implements StopStrategy {
        private final int maxAttemptNumber;

        public StopAfterAttemptStrategy(int maxAttemptNumber) {
            Preconditions.checkArgument(maxAttemptNumber >= 1, "maxAttemptNumber must be >= 1 but is %d", maxAttemptNumber);
            this.maxAttemptNumber = maxAttemptNumber;
        }

        @Override
        public boolean shouldStop(Attempt failedAttempt) {
            return failedAttempt.getAttemptNumber() >= maxAttemptNumber;
        }
    }

    /**
     * 根据时间判断是否要停止重试
     */
    @Immutable
    private static final class StopAfterDelayStrategy implements StopStrategy {

        /** 从第一次尝试开始经过的时间是否超过了给定的延迟时间，如果超过了，则停止重试 */
        private final long maxDelay;

        public StopAfterDelayStrategy(long maxDelay) {
            Preconditions.checkArgument(maxDelay >= 0L, "maxDelay must be >= 0 but is %d", maxDelay);
            this.maxDelay = maxDelay;
        }

        @Override
        public boolean shouldStop(Attempt failedAttempt) {
            return failedAttempt.getDelaySinceFirstAttempt() >= maxDelay;
        }
    }
}
