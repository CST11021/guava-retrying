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

import com.google.common.annotations.Beta;

/**
 * This listener provides callbacks for several events that occur when running
 * code through a {@link Retryer} instance.
 */
@Beta
public interface RetryListener {

    /**
     * 当任务被触发执行后，调用该方法，不管结果是什么，并且在应用拒绝谓词和停止策略之前，此方法都会生效
     *
     * @param attempt the current {@link Attempt}
     * @param <V>     the type returned by the retryer callable
     */
    <V> void onRetry(Attempt<V> attempt);
}
