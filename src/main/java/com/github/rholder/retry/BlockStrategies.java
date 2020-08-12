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

import javax.annotation.concurrent.Immutable;

/**
 * 用于创建{@link BlockStrategy}实例的工厂类，
 */
public final class BlockStrategies {

    /** 表示一个用线程sleep的阻塞策略 */
    private static final BlockStrategy THREAD_SLEEP_STRATEGY = new ThreadSleepStrategy();

    private BlockStrategies() {
    }

    /**
     * 返回一个阻止策略，该策略使当前线程在重试之间进入睡眠状态。
     *
     * @return a block strategy that puts the current thread to sleep between retries
     */
    public static BlockStrategy threadSleepStrategy() {
        return THREAD_SLEEP_STRATEGY;
    }

    @Immutable
    private static class ThreadSleepStrategy implements BlockStrategy {

        @Override
        public void block(long sleepTime) throws InterruptedException {
            Thread.sleep(sleepTime);
        }
    }

}