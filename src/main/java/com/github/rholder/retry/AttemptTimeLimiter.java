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

import java.util.concurrent.Callable;

/**
 * 执行一个任务，该任务受时间限制，如果任务执行时间超过了预设值的时间，则抛ExecutionException异常
 *
 * @param <V> V表示Callable执行后的返回值
 * @author Jason Dunkelberger (dirkraft)
 */
public interface AttemptTimeLimiter<V> {

    /**
     * 执行一个任务，该任务受时间限制，如果任务执行时间超过了预设值的时间，则抛ExecutionException异常
     *
     * @param callable 要执行的异步任务
     * @return the return of the given callable
     * @throws Exception any exception from this invocation
     */
    V call(Callable<V> callable) throws Exception;

}
