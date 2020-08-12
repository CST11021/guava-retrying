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

/**
 * 一种策略，用于确定尝试失败后重试之前要sleep多长时间.
 *
 * @author JB
 */
public interface WaitStrategy {

    /**
     * 返回重试之前进入睡眠状态的时间（以毫秒为单位）。
     *
     * @param failedAttempt the previous failed {@code Attempt}
     * @return the sleep time before next attempt
     */
    long computeSleepTime(Attempt failedAttempt);
}
