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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 用于创建{@link WaitStrategy}实例的工厂类，包括创建一下几种类型的重试等待策略：
 * 1、固定时长的重试等待策略
 * 2、随机式的重试等待策略
 * 3、递增式的重试等待策略
 * 4、指数级重试等待策略
 * 5、斐波纳契策略
 * 6、根据异常类型来自定义等待时长的策略
 * 7、复合式的重试等待策略
 *
 * @author JB
 */
public final class WaitStrategies {

    /** 表示无需等待的重试策略，即立即重试 */
    private static final WaitStrategy NO_WAIT_STRATEGY = new FixedWaitStrategy(0L);

    private WaitStrategies() {
    }

    /**
     * 返回一个立即重试策略
     *
     * @return a wait strategy that doesn't wait between retries
     */
    public static WaitStrategy noWait() {
        return NO_WAIT_STRATEGY;
    }

    /**
     * 返回一个固定等待时长的重试策略
     *
     * @param sleepTime 等待时间
     * @param timeUnit  等待时间单位
     * @return a wait strategy that sleeps a fixed amount of time
     * @throws IllegalStateException if the sleep time is &lt; 0
     */
    public static WaitStrategy fixedWait(long sleepTime, @Nonnull TimeUnit timeUnit) throws IllegalStateException {
        Preconditions.checkNotNull(timeUnit, "The time unit may not be null");
        return new FixedWaitStrategy(timeUnit.toMillis(sleepTime));
    }

    /**
     * 返回一个随机等待时间的重试策略
     *
     * @param maximumTime 随机的最长睡眠时间
     * @param timeUnit    时间单位
     * @return a wait strategy with a random wait time
     * @throws IllegalStateException if the maximum sleep time is &lt;= 0.
     */
    public static WaitStrategy randomWait(long maximumTime, @Nonnull TimeUnit timeUnit) {
        Preconditions.checkNotNull(timeUnit, "The time unit may not be null");
        return new RandomWaitStrategy(0L, timeUnit.toMillis(maximumTime));
    }

    /**
     * 返回一个随机等待时间的重试策略
     *
     * @param minimumTime     最少等待多长时间
     * @param minimumTimeUnit minimumTime的时间单位
     * @param maximumTime     最长等待多长时间
     * @param maximumTimeUnit maximumTime的时间单位
     * @return a wait strategy with a random wait time
     * @throws IllegalStateException if the minimum sleep time is &lt; 0, or if the
     *                               maximum sleep time is less than (or equals to) the minimum.
     */
    public static WaitStrategy randomWait(long minimumTime,
                                          @Nonnull TimeUnit minimumTimeUnit,
                                          long maximumTime,
                                          @Nonnull TimeUnit maximumTimeUnit) {
        Preconditions.checkNotNull(minimumTimeUnit, "The minimum time unit may not be null");
        Preconditions.checkNotNull(maximumTimeUnit, "The maximum time unit may not be null");
        return new RandomWaitStrategy(minimumTimeUnit.toMillis(minimumTime),
                maximumTimeUnit.toMillis(maximumTime));
    }

    /**
     * 返回一个策略，该策略在第一次尝试失败后休眠固定的时间，并在每次其他尝试失败后增加睡眠的时间.
     *
     * @param initialSleepTime     重试第一次之前的睡眠时间
     * @param initialSleepTimeUnit initialSleepTime的时间单位
     * @param increment            每次尝试失败后添加到上一个睡眠时间的增量
     * @param incrementTimeUnit    increment的时间单位
     * @return a wait strategy that incrementally sleeps an additional fixed time after each failed attempt
     */
    public static WaitStrategy incrementingWait(long initialSleepTime,
                                                @Nonnull TimeUnit initialSleepTimeUnit,
                                                long increment,
                                                @Nonnull TimeUnit incrementTimeUnit) {
        Preconditions.checkNotNull(initialSleepTimeUnit, "The initial sleep time unit may not be null");
        Preconditions.checkNotNull(incrementTimeUnit, "The increment time unit may not be null");
        return new IncrementingWaitStrategy(initialSleepTimeUnit.toMillis(initialSleepTime),
                incrementTimeUnit.toMillis(increment));
    }

    /**
     * 返回一个策略，该策略在第一次失败尝试后将睡眠一段指数的时间，并在每次失败尝试后以指数形式递增，直至Long.MAX_VALUE。
     *
     * @return a wait strategy that increments with each failed attempt using exponential backoff
     */
    public static WaitStrategy exponentialWait() {
        return new ExponentialWaitStrategy(1, Long.MAX_VALUE);
    }

    /**
     * Returns a strategy which sleeps for an exponential amount of time after the first failed attempt,
     * and in exponentially incrementing amounts after each failed attempt up to the maximumTime.
     *
     * @param maximumTime     最长睡眠时间
     * @param maximumTimeUnit 最长时间的单位
     * @return a wait strategy that increments with each failed attempt using exponential backoff
     */
    public static WaitStrategy exponentialWait(long maximumTime, @Nonnull TimeUnit maximumTimeUnit) {
        Preconditions.checkNotNull(maximumTimeUnit, "The maximum time unit may not be null");
        return new ExponentialWaitStrategy(1, maximumTimeUnit.toMillis(maximumTime));
    }

    /**
     * Returns a strategy which sleeps for an exponential amount of time after the first failed attempt,
     * and in exponentially incrementing amounts after each failed attempt up to the maximumTime.
     * The wait time between the retries can be controlled by the multiplier.
     * nextWaitTime = exponentialIncrement * {@code multiplier}.
     *
     * @param multiplier      乘以由此计算的等待时间
     * @param maximumTime     最长睡眠时间
     * @param maximumTimeUnit 最长时间的单位
     * @return a wait strategy that increments with each failed attempt using exponential backoff
     */
    public static WaitStrategy exponentialWait(long multiplier, long maximumTime, @Nonnull TimeUnit maximumTimeUnit) {
        Preconditions.checkNotNull(maximumTimeUnit, "The maximum time unit may not be null");
        return new ExponentialWaitStrategy(multiplier, maximumTimeUnit.toMillis(maximumTime));
    }

    /**
     * 斐波纳契策略，例如这样一个数列：0、1、1、2、3、5、8、13、21、34、……
     * 后面一个值等于前面两个的值之和
     *
     * Returns a strategy which sleeps for an increasing amount of time after the first failed attempt,
     * and in Fibonacci increments after each failed attempt up to {@link Long#MAX_VALUE}.
     *
     * @return a wait strategy that increments with each failed attempt using a Fibonacci sequence
     */
    public static WaitStrategy fibonacciWait() {
        return new FibonacciWaitStrategy(1, Long.MAX_VALUE);
    }

    /**
     * Returns a strategy which sleeps for an increasing amount of time after the first failed attempt,
     * and in Fibonacci increments after each failed attempt up to the {@code maximumTime}.
     *
     * @param maximumTime     the maximum time to sleep
     * @param maximumTimeUnit the unit of the maximum time
     * @return a wait strategy that increments with each failed attempt using a Fibonacci sequence
     */
    public static WaitStrategy fibonacciWait(long maximumTime, @Nonnull TimeUnit maximumTimeUnit) {
        Preconditions.checkNotNull(maximumTimeUnit, "The maximum time unit may not be null");
        return new FibonacciWaitStrategy(1, maximumTimeUnit.toMillis(maximumTime));
    }

    /**
     * Returns a strategy which sleeps for an increasing amount of time after the first failed attempt,
     * and in Fibonacci increments after each failed attempt up to the {@code maximumTime}.
     * The wait time between the retries can be controlled by the multiplier.
     * nextWaitTime = fibonacciIncrement * {@code multiplier}.
     *
     * @param multiplier      multiply the wait time calculated by this
     * @param maximumTime     the maximum time to sleep
     * @param maximumTimeUnit the unit of the maximum time
     * @return a wait strategy that increments with each failed attempt using a Fibonacci sequence
     */
    public static WaitStrategy fibonacciWait(long multiplier, long maximumTime, @Nonnull TimeUnit maximumTimeUnit) {
        Preconditions.checkNotNull(maximumTimeUnit, "The maximum time unit may not be null");
        return new FibonacciWaitStrategy(multiplier, maximumTimeUnit.toMillis(maximumTime));
    }

    /**
     * 返回一种特定的异常等待策略，如果上一次任务异常是我们关心的异常，根据Function函数计算等待时间，如果异常不匹配，则返回等待时间0，立即重试
     *
     * @param function       计算睡眠时间的函数
     * @param exceptionClass 用于计算睡眠时间的异常类
     * @return a wait strategy calculated from the failed attempt
     */
    public static <T extends Throwable> WaitStrategy exceptionWait(@Nonnull Class<T> exceptionClass,
                                                                   @Nonnull Function<T, Long> function) {
        Preconditions.checkNotNull(exceptionClass, "exceptionClass may not be null");
        Preconditions.checkNotNull(function, "function may not be null");
        return new ExceptionWaitStrategy<T>(exceptionClass, function);
    }

    /**
     * 复合式的重试等待策略，将每个等待的时长相加，即为最终的等待时长
     *
     * Joins one or more wait strategies to derive a composite wait strategy.
     * The new joined strategy will have a wait time which is total of all wait times computed one after another in order.
     *
     * @param waitStrategies Wait strategies that need to be applied one after another for computing the sleep time.
     * @return A composite wait strategy
     */
    public static WaitStrategy join(WaitStrategy... waitStrategies) {
        Preconditions.checkState(waitStrategies.length > 0, "Must have at least one wait strategy");
        List<WaitStrategy> waitStrategyList = Lists.newArrayList(waitStrategies);
        Preconditions.checkState(!waitStrategyList.contains(null), "Cannot have a null wait strategy");
        return new CompositeWaitStrategy(waitStrategyList);
    }

    /**
     * 固定时长的重试等待策略
     */
    @Immutable
    private static final class FixedWaitStrategy implements WaitStrategy {
        private final long sleepTime;

        public FixedWaitStrategy(long sleepTime) {
            Preconditions.checkArgument(sleepTime >= 0L, "sleepTime must be >= 0 but is %d", sleepTime);
            this.sleepTime = sleepTime;
        }

        @Override
        public long computeSleepTime(Attempt failedAttempt) {
            return sleepTime;
        }
    }

    /**
     * 随机式的重试等待策略
     */
    @Immutable
    private static final class RandomWaitStrategy implements WaitStrategy {
        private static final Random RANDOM = new Random();
        private final long minimum;
        private final long maximum;

        public RandomWaitStrategy(long minimum, long maximum) {
            Preconditions.checkArgument(minimum >= 0, "minimum must be >= 0 but is %d", minimum);
            Preconditions.checkArgument(maximum > minimum, "maximum must be > minimum but maximum is %d and minimum is", maximum, minimum);

            this.minimum = minimum;
            this.maximum = maximum;
        }

        @Override
        public long computeSleepTime(Attempt failedAttempt) {
            long t = Math.abs(RANDOM.nextLong()) % (maximum - minimum);
            return t + minimum;
        }
    }

    /**
     * 递增式的重试等待策略
     */
    @Immutable
    private static final class IncrementingWaitStrategy implements WaitStrategy {
        private final long initialSleepTime;
        private final long increment;

        public IncrementingWaitStrategy(long initialSleepTime,
                                        long increment) {
            Preconditions.checkArgument(initialSleepTime >= 0L, "initialSleepTime must be >= 0 but is %d", initialSleepTime);
            this.initialSleepTime = initialSleepTime;
            this.increment = increment;
        }

        @Override
        public long computeSleepTime(Attempt failedAttempt) {
            long result = initialSleepTime + (increment * (failedAttempt.getAttemptNumber() - 1));
            return result >= 0L ? result : 0L;
        }
    }

    /**
     * 指数级重试等待策略
     */
    @Immutable
    private static final class ExponentialWaitStrategy implements WaitStrategy {

        /** 表示一个乘数 */
        private final long multiplier;
        /** 表示最长等待时间 */
        private final long maximumWait;

        public ExponentialWaitStrategy(long multiplier, long maximumWait) {
            Preconditions.checkArgument(multiplier > 0L, "multiplier must be > 0 but is %d", multiplier);
            Preconditions.checkArgument(maximumWait >= 0L, "maximumWait must be >= 0 but is %d", maximumWait);
            Preconditions.checkArgument(multiplier < maximumWait, "multiplier must be < maximumWait but is %d", multiplier);
            this.multiplier = multiplier;
            this.maximumWait = maximumWait;
        }

        @Override
        public long computeSleepTime(Attempt failedAttempt) {
            // 2的n次方，比如：2的3方等于8
            double exp = Math.pow(2, failedAttempt.getAttemptNumber());
            // Math.round：返回一个数字四舍五入后最接近的整数
            long result = Math.round(multiplier * exp);
            if (result > maximumWait) {
                result = maximumWait;
            }
            return result >= 0L ? result : 0L;
        }
    }

    /**
     * 斐波纳契策略，例如这样一个数列：0、1、1、2、3、5、8、13、21、34、……
     * 后面一个值等于前面两个的值之和
     */
    @Immutable
    private static final class FibonacciWaitStrategy implements WaitStrategy {
        private final long multiplier;
        private final long maximumWait;

        public FibonacciWaitStrategy(long multiplier, long maximumWait) {
            Preconditions.checkArgument(multiplier > 0L, "multiplier must be > 0 but is %d", multiplier);
            Preconditions.checkArgument(maximumWait >= 0L, "maximumWait must be >= 0 but is %d", maximumWait);
            Preconditions.checkArgument(multiplier < maximumWait, "multiplier must be < maximumWait but is %d", multiplier);
            this.multiplier = multiplier;
            this.maximumWait = maximumWait;
        }

        @Override
        public long computeSleepTime(Attempt failedAttempt) {
            long fib = fib(failedAttempt.getAttemptNumber());
            long result = multiplier * fib;

            if (result > maximumWait || result < 0L) {
                result = maximumWait;
            }

            return result >= 0L ? result : 0L;
        }

        private long fib(long n) {
            if (n == 0L) return 0L;
            if (n == 1L) return 1L;

            long prevPrev = 0L;
            long prev = 1L;
            long result = 0L;

            for (long i = 2L; i <= n; i++) {
                result = prev + prevPrev;
                prevPrev = prev;
                prev = result;
            }

            return result;
        }
    }

    /**
     * 复合式的重试等待策略，将每个等待的时长相加，即为最终的等待时长
     */
    @Immutable
    private static final class CompositeWaitStrategy implements WaitStrategy {
        private final List<WaitStrategy> waitStrategies;

        public CompositeWaitStrategy(List<WaitStrategy> waitStrategies) {
            Preconditions.checkState(!waitStrategies.isEmpty(), "Need at least one wait strategy");
            this.waitStrategies = waitStrategies;
        }

        @Override
        public long computeSleepTime(Attempt failedAttempt) {
            long waitTime = 0L;
            for (WaitStrategy waitStrategy : waitStrategies) {
                waitTime += waitStrategy.computeSleepTime(failedAttempt);
            }
            return waitTime;
        }
    }

    /**
     * 根据异常类型来自定义等待时长的策略
     *
     * @param <T>
     */
    @Immutable
    private static final class ExceptionWaitStrategy<T extends Throwable> implements WaitStrategy {
        private final Class<T> exceptionClass;
        private final Function<T, Long> function;

        public ExceptionWaitStrategy(@Nonnull Class<T> exceptionClass, @Nonnull Function<T, Long> function) {
            this.exceptionClass = exceptionClass;
            this.function = function;
        }

        @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "ConstantConditions", "unchecked"})
        @Override
        public long computeSleepTime(Attempt lastAttempt) {
            // 判断任务上一次指定有没有发生异常
            if (lastAttempt.hasException()) {
                Throwable cause = lastAttempt.getExceptionCause();
                if (exceptionClass.isAssignableFrom(cause.getClass())) {
                    return function.apply((T) cause);
                }
            }
            return 0L;
        }
    }
}
